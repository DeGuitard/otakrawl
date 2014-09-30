package com.deguitard.otakrawl.services.crawler;

import java.util.Deque;

import com.deguitard.otakrawl.model.Chapter;
import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;

/**
 * Define a crawler, that can gather information about a manga, and
 * crawl the different chapters of the manga.
 *
 * @author Vianney Dupoy de Guitard
 *
 */
public interface ICrawler {

	/**
	 * Crawl the mangas list, with no distinction.
	 * It just gets the title of the manga.
	 *
	 * @return the list of all the mangas crawled.
	 */
	Deque<Manga> crawlMangaList();

	/**
	 * Crawl a specific manga, given its name.
	 * Grabs all information available, including the urls of the chapters.
	 *
	 * @param manga : the manga to crawl (its name must be set).
	 * @return the crawled manga.
	 * @throws NoResultFoundException when no manga was found.
	 */
	Manga crawlManga(Manga manga) throws NoResultFoundException;

	/**
	 * Crawl a specific chapter of a manga, given its number.
	 *
	 * @param manga : the manga the chapter is from.
	 * @param chapter : the chapter to crawl (the number must be set).
	 * @return the chapter with all the information.
	 * @throws NoResultFoundException when no chapter was found.
	 */
	Chapter crawlChapter(Manga manga, Chapter chapter) throws NoResultFoundException;

	/**
	 * Crawl the list of recently updated mangas.
	 *
	 * @return the list of recently updated mangas.
	 */
	Deque<Manga> getUpdatedMangaList();
}
