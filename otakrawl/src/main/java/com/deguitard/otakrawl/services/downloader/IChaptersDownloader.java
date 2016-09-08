package com.deguitard.otakrawl.services.downloader;

import java.util.Deque;

import com.deguitard.otakrawl.model.Manga;

public interface IChaptersDownloader {

	/**
	 * Downloads all the chapter pages and stores them.
	 *
	 * @param mangaList : the list of mangas to download.
	 */
	void download(Deque<Manga> mangaList);

	/**
	 * Downloads all the chapter pages and stores them.
	 *
	 * @param mangaList : the manga to download.
	 */
	void download(Manga manga);
}
