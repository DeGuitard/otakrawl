package com.deguitard.otakrawl.services.imprt;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deguitard.otakrawl.Otakrawl;
import com.deguitard.otakrawl.model.Chapter;
import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.services.crawler.ICrawler;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;
import com.deguitard.otakrawl.services.persistence.DBTypedCursor;
import com.deguitard.otakrawl.services.persistence.dao.MangaDao;
import com.google.inject.Inject;

/**
 * Runnable used for multi-threaded full import.
 *
 * @author Vianney Dupoy de Guitard
 */
public class ImportServiceImplRunnable implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImportServiceImplRunnable.class);

	@Inject private MangaDao mangaDao;

	/** The crawler to use. */
	private ICrawler crawler;

	/** The manga list assigned to this thread. */
	private final Deque<ObjectId> mangaIds = new ArrayDeque<>();

	/**
	 * Constructor of a import runnable.
	 * This is only used for full imports.
	 *
	 * @param pCrawler : the crawler to use.
	 * @param pMangaList : the manga list assigned to this thread.
	 */
	public ImportServiceImplRunnable(ICrawler pCrawler, Deque<Manga> pMangaList) {
		this.crawler = pCrawler;
		for (Manga manga : pMangaList) {
			this.mangaIds.add(manga.getId());
		}
		Otakrawl.getInjector().injectMembers(this);
	}

	/**
	 * Grabs the data available for all mangas, and then downloads all the chapters.
	 */
	@Override
	public void run() {
		crawlAllMangas(crawler);
		crawlAllChapters(crawler);
		LOGGER.info("Thread #{} has finished crawling.", Thread.currentThread().getName());
	}

	/**
	 * Crawl all information related to the collection of mangas the thread is working on.
	 */
	private void crawlAllMangas(ICrawler crawler) {
		Deque<Manga> successes = new ArrayDeque<>();
		Deque<Manga> failures = new ArrayDeque<>();
		for (Manga manga : mangaDao.browseByField(mangaIds, "_id")) {
			// Prevents from crawling already crawled mangas.
			if (manga.getChapters().isEmpty()) {
				try {
					crawler.crawlManga(manga);
					successes.add(manga);
				} catch (NoResultFoundException e) {
					LOGGER.error("The following manga could not be crawled: {}.", manga.getTitle());
					failures.add(manga);
				}
			}
		}
		listMangaFailures(failures);
		mangaDao.saveOrUpdate(successes);;
	}

	/**
	 * Crawl all chapters and all information related to it for all mangas given. 
	 */
	private void crawlAllChapters(ICrawler crawler) {
		DBTypedCursor<Manga> cursor = mangaDao.browseByField(mangaIds, "_id");
		try {
			for (Manga manga : cursor) {
				LOGGER.debug("Crawling chapters from '{}'.", manga.getTitle());
				Deque<Chapter> failures = new ArrayDeque<>();
				for (Chapter chapter : manga.getChapters()) {
					try {
						crawler.crawlChapter(manga, chapter);
					} catch (NoResultFoundException e) {
						LOGGER.error("Chapter '{}' from the manga '{}' could not be found.", chapter.getNumber(), manga.getTitle());
						failures.add(chapter);
					}
				}
				listChapterFailures(failures, manga);
				mangaDao.saveOrUpdate(manga);
				LOGGER.info("Finished crawling chapters from '{}'.", manga.getTitle());
			}
		} finally {
			cursor.close();
		}
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
	 * Lists in the logger the chapters that couldn't be imported.
	 * @param failures : list of chapters that weren't imported.
	 * @param manga : the manga of these chapters
	 */
	private void listChapterFailures(Collection<Chapter> failures, Manga manga) {
		if (failures.size() > 0) {
			LOGGER.info("The following chapters from '{}' could not be imported:", manga.getTitle());
			for (Chapter chapter : failures) {
				LOGGER.info("Chapter no. {}.", chapter.getNumber());
			}
			LOGGER.info("[End of failures list].");
		}
	}
}
