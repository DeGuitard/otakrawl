package com.deguitard.otakrawl.services.imprt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deguitard.otakrawl.model.Chapter;
import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.services.crawler.AnimePlanetSuggestionCrawler;
import com.deguitard.otakrawl.services.crawler.StarKanaCrawler;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;
import com.deguitard.otakrawl.services.persistence.dao.MangaDao;
import com.google.inject.Inject;

public class ImportServiceImpl implements ImportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImportServiceImpl.class);

	@Inject
	private StarKanaCrawler crawler;

	@Inject
	private AnimePlanetSuggestionCrawler suggest;

	@Inject
	private MangaDao mangaDao;

	@Override
	public void fullImport() {
		Deque<Manga> mangaList = crawler.crawlMangaList();
		crawlAllMangas(mangaList);
		crawlAllChapters(mangaList);
	}

	/**
	 * Crawls the recently updated mangas, then updates the database.
	 */
	@Override
	public void updateImport() {
		LOGGER.info("Updating mangas.");
		Deque<Manga> recentMangas = crawler.getUpdatedMangaList();
		Deque<Manga> mangasToUpdate = mergeMangaCrawlDb(recentMangas, mangaDao.findByUrl(recentMangas));
		Deque<Manga> mangasUpdated = new ArrayDeque<>();
		int chapterUpdateCount = 0;

		for (Manga mangaToUpdate : mangasToUpdate) {
			try {
				crawler.crawlManga(mangaToUpdate);
				for (Chapter chapter : mangaToUpdate.getChapters()) {
					if (chapter.getImagesUrls() == null || chapter.getImagesUrls().isEmpty()) {
						crawler.crawlChapter(mangaToUpdate, chapter);
						chapterUpdateCount++;
					}
				}
				mangasUpdated.add(mangaToUpdate);
				// TODO: notify followers.
			} catch (NoResultFoundException e) {
				LOGGER.error("Could not update manga named '{}'.", mangaToUpdate.getTitle());
			}
		}

		mangaDao.saveOrUpdate(mangasToUpdate);
		LOGGER.info("{} chapters were added.", chapterUpdateCount);
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
	 * Crawl all information related to the supplied collection of mangas.
	 * @param mangaList : the mangas to crawl.
	 * @return the list of the crawled mangas.
	 */
	private Deque<Manga> crawlAllMangas(Deque<Manga> mangaList) {
		Deque<Manga> failures = new ArrayDeque<>();
		for (Manga manga : mangaList) {
			try {
				crawler.crawlManga(manga);
			} catch (NoResultFoundException e) {
				LOGGER.error("The following manga could not be crawled: {}.", manga.getTitle());
				failures.add(manga);
			}
		}
		listMangaFailures(failures);
		mangaList.removeAll(failures);
		return mangaList;
	}

	/**
	 * Crawl all chapters and all information related to it for all mangas given.
	 * @param mangaList : the list of mangas. 
	 * @return the mangas filled with fully crawled chapters.
	 */
	private Deque<Manga> crawlAllChapters(Deque<Manga> mangaList) {
		Deque<Chapter> failures = new ArrayDeque<>();
		for (Manga manga : mangaList) {
			for (Chapter chapter : manga.getChapters()) {
				try {
					crawler.crawlChapter(manga, chapter);
				} catch (NoResultFoundException e) {
					LOGGER.error("Chapter '{}' from the manga '{}' could not be found.", chapter.getNumber(), manga.getTitle());
					failures.add(chapter);
				}
			}
			mangaDao.saveOrUpdate(manga);
		}
		listChapterFailures(failures);
		return mangaList;
	}

	/**
	 * Lists in the logger the mangas that couldn't be imported.
	 * @param failures : list of mangas that weren't imported.
	 */
	private void listMangaFailures(Collection<Manga> failures) {
		if (failures.size() > 0) {
			LOGGER.info("The following mangas could not be imported:");
			for (Manga manga : failures) {
				LOGGER.info(manga.getTitle());
			}
			LOGGER.info("[End of failures list].");
		}
	}

	/**
	 * Lists in the logger the mangas that couldn't be imported.
	 * @param failures : list of mangas that weren't imported.
	 */
	private void listChapterFailures(Collection<Chapter> failures) {
		if (failures.size() > 0) {
			LOGGER.info("The following chapters could not be imported:");
			for (Chapter chapter : failures) {
				LOGGER.info("Chapter no. {}.", chapter.getNumber());
			}
			LOGGER.info("[End of failures list].");
		}
	}
}
