package com.deguitard.otakrawl.services.downloader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Deque;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deguitard.otakrawl.model.Chapter;
import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;
import com.deguitard.otakrawl.services.tools.PropertyService;
import com.google.inject.Inject;

/**
 * Implentation of a simple chapter downloader. It will look for
 * all the page images, and then store it on a local folder.
 *
 * @author Vianney Dupoy de Guitard
 *
 */
public class ChaptersDownloaderImpl implements IChaptersDownloader {

	/** Applicative logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ChaptersDownloaderImpl.class);

	/** Max attemps the crawler must issue for a request. */
	private static final int MAX_TRIES = 5;

	/** Property service used to retrieve the download folder path. */
	@Inject private PropertyService propertyService;

	/** The path of the folder in which files are downloaded. */
	private String downloadPath;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void download(Deque<Manga> mangaList) {
		for (Manga manga : mangaList) {
			download(manga);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void download(Manga manga) {
		if (downloadPath == null) {
			setDownloadPath();
		}
		LOGGER.info("Downloading images of {}.", manga.getTitle());
		downloadThumbnail(manga);
		for (Chapter chapter : manga.getChapters()) {
			downloadChapter(manga, chapter);
		}
	}

	/**
	 * Sets the download path with the properties.
	 */
	private void setDownloadPath() {
		try {
			downloadPath = propertyService.getValue("download.directory");
		} catch (NoResultFoundException e) {
			LOGGER.error("Could not read 'download directory' property value.", e);
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Downloads the manga's thumbnail.
	 * @param manga : the manga.
	 */
	private void downloadThumbnail(Manga manga) {
		if (StringUtils.isBlank(manga.getThumbnail())) {
			return;
		}

		try {
			URL urlObj = new URL(manga.getThumbnail());
			String path = getThumbnailFilePath(manga);
			File file = new File(path);
			FileUtils.copyURLToFile(urlObj, file);
		} catch (MalformedURLException e) {
			LOGGER.error("Thumbnail URL seems invalid.", e);
		} catch (IOException e) {
			LOGGER.error("An error happened while creating the image.", e);
		}
	}

	/**
	 * Downloads all the pages of a chapter.
	 * @param manga : the manga this chapter is from.
	 * @param chapter : the chapter to download.
	 */
	private void downloadChapter(Manga manga, Chapter chapter) {
		int pageCount = 0;
		for (String url : chapter.getImagesUrls()) {
			int tries = 0;
			boolean done = false;
			while (!done && tries < MAX_TRIES) {
				try {
					URL urlObj = new URL(url);
					String path = getChapterFilePath(manga, chapter, pageCount, url);
					File file = new File(path);
					FileUtils.copyURLToFile(urlObj, file);
					done = true;
				} catch (MalformedURLException e) {
					tries++;
					LOGGER.error("Chapter page URL seems invalid.", e);
				} catch (IOException e) {
					tries++;
					LOGGER.error("Error creating the image.", e);
				}
			}
			pageCount++;
		}
	}

	/**
	 * Creates the path of a chapter page.
	 * @param manga : the manga the chapter is from.
	 * @param chapter : the chapter the page is from.
	 * @param pageCount : the page number.
	 * @param url : the original file URL.
	 * @return the file path.
	 */
	public String getChapterFilePath(Manga manga, Chapter chapter, int pageCount, String url) {
		StringBuilder path = new StringBuilder(downloadPath);
		path.append(manga.getId().toHexString());
		path.append("/");
		path.append(chapter.getNumber());
		path.append("/");
		path.append(pageCount);
		path.append(getFileExtension(url));
		LOGGER.debug("Chapter path: {}", path.toString());
		return path.toString();
	}

	/**
	 * Creates the path of a manga thumbnail.
	 * @param manga : the manga the thumbnail is from.
	 * @return the path to use for the thumbnail.
	 */
	public String getThumbnailFilePath(Manga manga) {
		StringBuilder path = new StringBuilder(downloadPath);
		path.append(manga.getId().toHexString());
		path.append("/");
		path.append("thumbnail");
		path.append(getFileExtension(manga.getThumbnail()));
		LOGGER.debug("Thumbnail path: {}", path.toString());
		return path.toString();
	}

	/**
	 * Returns the extension of the supplied URL, with the final dot.
	 * ex: ".png"
	 * @param url : the url to use.
	 * @return the extracted extension.
	 */
	private String getFileExtension(String url) {
		String ext = url.subSequence(url.lastIndexOf("."), url.length()).toString();
		while (ext.contains("?")) {
			ext = ext.subSequence(0, ext.indexOf("?")).toString();
		}
		return ext;
	}
}
