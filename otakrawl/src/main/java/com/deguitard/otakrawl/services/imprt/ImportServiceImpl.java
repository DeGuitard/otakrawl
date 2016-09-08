package com.deguitard.otakrawl.services.imprt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deguitard.otakrawl.model.Activity;
import com.deguitard.otakrawl.model.Activity.ActivityType;
import com.deguitard.otakrawl.model.Chapter;
import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.model.builders.ActivityBuilder;
import com.deguitard.otakrawl.services.crawler.ICrawler;
import com.deguitard.otakrawl.services.crawler.animeplanet.AnimePlanetSuggestionCrawler;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;
import com.deguitard.otakrawl.services.persistence.dao.ActivityDao;
import com.deguitard.otakrawl.services.persistence.dao.MangaDao;
import com.google.inject.Inject;

public class ImportServiceImpl implements ImportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImportServiceImpl.class);
	private static final int THREAD_COUNT = 32;

	@Inject private Set<ICrawler> crawlers;
	@Inject private AnimePlanetSuggestionCrawler suggest;
	@Inject private MangaDao mangaDao;
	@Inject private ActivityDao activityDao;

	@Override
	public void fullImport() {
		long start = System.currentTimeMillis();
		mangaDao.dropAll();
		for (ICrawler crawler : crawlers) {
			Deque<Manga> mangaList = crawler.crawlMangaList();
			List<Deque<Manga>> chunks = split(mangaList, THREAD_COUNT);
			mangaDao.saveOrUpdate(mangaList);
			LOGGER.info("Found {} mangas.", mangaList.size());

			// Starts the threads.
			ExecutorService es = Executors.newFixedThreadPool(THREAD_COUNT);
			for (Deque<Manga> chunk : chunks) {
				Runnable runnable = new ImportServiceImplRunnable(crawler, chunk);
				es.execute(runnable);
			}
			es.shutdown();
			try {
				es.awaitTermination(72, TimeUnit.HOURS);
			} catch (InterruptedException e) {
				LOGGER.error("Thread aborption.", e);
			}
		}
		LOGGER.info("Import finished after " + ((System.currentTimeMillis() - start) / 1000) + "s.");
	}

	@Override
	public void merge() {
		long start = System.currentTimeMillis();
		Deque<Manga> mangaList = mangaDao.findAllIds();
		for (ICrawler crawler : crawlers) {
			List<Deque<Manga>> chunks = split(mangaList, THREAD_COUNT);
			LOGGER.info("Found {} mangas.", mangaList.size());

			// Starts the threads.
			ExecutorService es = Executors.newFixedThreadPool(THREAD_COUNT);
			for (Deque<Manga> chunk : chunks) {
				Runnable runnable = new ImportServiceImplRunnable(crawler, chunk);
				es.execute(runnable);
			}
			es.shutdown();
			try {
				es.awaitTermination(72, TimeUnit.HOURS);
			} catch (InterruptedException e) {
				LOGGER.error("Thread aborption.", e);
			}
		}
		LOGGER.info("Import finished after " + ((System.currentTimeMillis() - start) / 1000) + "s.");
	}

	/**
	 * Crawls the recently updated mangas, then updates the database.
	 */
	@Override
	public void updateImport() {
		for (ICrawler crawler : crawlers) {
			LOGGER.info("Updating mangas.");
			Deque<Manga> recentMangas = crawler.getUpdatedMangaList();
			Deque<Manga> mangasToUpdate = mergeMangaCrawlDb(recentMangas, mangaDao.findByUrl(recentMangas));
			Deque<Manga> mangasUpdated = new ArrayDeque<>();
			Deque<Activity> activities = new ArrayDeque<>();
			int chapterUpdateCount = 0;

			for (Manga mangaToUpdate : mangasToUpdate) {
				try {
					crawler.crawlManga(mangaToUpdate);
					mangaToUpdate.generateIdIfNew();
					for (Chapter chapter : mangaToUpdate.getChapters()) {
						if (chapter.getImagesUrls() == null || chapter.getImagesUrls().isEmpty()) {
							crawler.crawlChapter(mangaToUpdate, chapter);
							chapterUpdateCount++;
							activities.add(new ActivityBuilder().type(ActivityType.NEW_CHAPTER).manga(mangaToUpdate).chapterNumber(chapter.getNumber()).createActivity());
						}
					}
					mangasUpdated.add(mangaToUpdate);
				} catch (NoResultFoundException e) {
					LOGGER.error("Could not update manga named '{}'.", mangaToUpdate.getTitle());
				}
			}

			mangaDao.saveOrUpdate(mangasToUpdate);
			LOGGER.info("{} chapters were added.", chapterUpdateCount);

			activityDao.saveOrUpdate(activities);
			LOGGER.info("{} activities saved.", activities.size());
		}
	}

	@Override
	public void updateSuggestions() {
		LOGGER.info("Updating suggestions.");
		for (Manga manga : mangaDao.findAll()) {
			try {
				Collection<Manga> suggestions = suggest.findSuggestions(manga);
				List<String> titles = new ArrayList<>();
				for (Manga suggestion : suggestions) {
					titles.add(suggestion.getTitle());
				}
				manga.setSuggestions(titles);
				mangaDao.saveOrUpdate(manga);
			} catch (NoResultFoundException e) {
				LOGGER.error("Could not find suggestions for {}.", manga.getTitle());
			}
		}
	}

	/**
	 * Merge the list of crawled manga with the list of already saved mangas.
	 * @param mangasFromCrawl : the list of crawled mangas.
	 * @param mangasFromDB : the list of already saved mangas.
	 * @return the merged list.
	 */
	private Deque<Manga> mergeMangaCrawlDb(Deque<Manga> mangasFromCrawl, Deque<Manga> mangasFromDB) {
		Deque<Manga> mangasMerged = new ArrayDeque<>();
		for (Manga crawlManga : mangasFromCrawl) {
			boolean found = false;
			for (Manga mangaDb : mangasFromDB) {
				if (crawlManga.getUrl().equals(mangaDb.getUrl())) {
					found = true;
					mangasMerged.add(mangaDb);
					break;
				}
			}
			if (!found) {
				mangasMerged.add(crawlManga);
			}
		}
		return mangasMerged;
	}

	/**
	 * Splits a list into a subset of lists.
	 * @param list : the list to split.
	 * @param size : the amount of lists to return.
	 * @return the split list.
	 */
	private <T> List<Deque<T>> split(Collection<T> list, int size) {
	    List<Deque<T>> result = new ArrayList<Deque<T>>(size);

	    for (int i = 0; i < size; i++) {
	        result.add(new ArrayDeque<T>());
	    }

	    int index = 0;
	    for (T t : list) {
	        result.get(index).add(t);
	        index = (index + 1) % size;
	    }

	    return result;
	}
}
