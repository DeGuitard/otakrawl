package com.deguitard.otakrawl.services.crawler.mangafox;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deguitard.otakrawl.model.Chapter;
import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.model.builders.ChapterBuilder;
import com.deguitard.otakrawl.model.builders.MangaBuilder;
import com.deguitard.otakrawl.services.crawler.CrawlUtils;
import com.deguitard.otakrawl.services.crawler.ICrawler;
import com.deguitard.otakrawl.services.crawler.provider.CrawlSource;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;

/**
 * Implementation of the {@link ICrawler} interface.
 * This crawler is implemented for the http://mangafox.me website.
 *
 * @author Vianney Dupoy de Guitard
 */
public class MangaFoxCrawler implements ICrawler {

	/** Applicative logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MangaFoxCrawler.class);

	/** URL of the website. */
	private static final String WEBSITE_URL = "http://mangafox.me/";
	/** URL of the page listing all available mangas. */
	private static final String LIST_URL = WEBSITE_URL + "manga";
	/** URL of the page listing recent updates. */
	private static final String RECENT_URL = WEBSITE_URL + "releases";

	/** {@inheritDoc} */
	@Override
	public Deque<Manga> crawlMangaList() {
		LOGGER.info("Crawling MangaFox manga list.");
		Deque<Manga> mangaList = new ArrayDeque<Manga>();

		try {
			LOGGER.debug("Downloading page '{}'", LIST_URL);
			Document page = Jsoup.connect(LIST_URL).maxBodySize(0).timeout(600000).get();

			// All links to mangas are in a list, and the link is basically the manga name.
			Elements mangaLinks = page.select(".manga_list li > a");
			for (int i = 0; i < mangaLinks.size(); i ++) {
				Element mangaLink = mangaLinks.get(i);
				String mangaFoxId = mangaLink.attr("rel");
				Manga manga = new MangaBuilder().title(mangaLink.text()).mangaFoxId(mangaFoxId).url(mangaLink.attr("abs:href")).source(CrawlSource.MANGA_FOX).createManga();
				mangaList.add(manga);
				LOGGER.debug("Manga named '" + manga.getTitle() + "' crawled.");
			}

			LOGGER.info("Manga list crawled.");
		} catch (IOException e) {
			LOGGER.error("Could not get manga list page!", e);
		}

		return mangaList;
	}

	/** {@inheritDoc} */
	@Override
	public Manga crawlManga(Manga manga) throws NoResultFoundException {
		try {
			LOGGER.debug("Crawling manga named {} at {}.", manga.getTitle(), manga.getUrl());

			// Downloading page and parsing most elements.
			Document page = Jsoup.connect(manga.getUrl()).maxBodySize(0).timeout(600000).get();
			Element altTitles = page.select("#title > h3:nth-child(2)").first();
			int i = 3;
			if (altTitles == null) {
				i = 2;
			}

			Element year = page.select("#title > table:nth-child(" + i + ") > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)").first();
			Elements authors = page.select("#title > table:nth-child(" + i + ") > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(2)");
			Elements artists = page.select("#title > table:nth-child(" + i + ") > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(3)");
			Elements genres = page.select("#title > table:nth-child(" + i + ") > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(4)");
			Element thumbnail = page.select(".cover > img:nth-child(1)").first();
			Element ongoing = page.select("div.data:nth-child(5) > span:nth-child(2)").first();
			Element summary = page.select(".summary").first();

			// Setting the manga with the parsed elements.
			manga.setThumbnail(thumbnail.attr("abs:src"));
			manga.setAuthors(toList(authors.text(), ", "));
			manga.setArtists(toList(artists.text(), ", "));
			manga.setGenres(toList(genres.text(), ", "));
			manga.setChapters(new ArrayDeque<Chapter>());
			manga.setIsOngoing(ongoing.text().contains("Ongoing"));

			// Summary can be null sometimes, so we do a quick check about it.
			if (summary != null) {
				manga.setSummary(summary.text());
			}

			// Sometimes there are no alt titles, hence the check!
			if (altTitles != null) {
				manga.setAltTitles(toList(altTitles.text(), "; "));
			}

			// Safe conversion of year to integer.
			try {
				manga.setYear(Integer.valueOf(year.text()));
			} catch (NumberFormatException e) {
				LOGGER.warn("Could not fetch start date for manga '{}', incorrect format: '{}'", manga.getTitle(), year.text());
			}

			// Gets the chapter links and detect new ones / old ones (so we don't erase image urls already crawled).
			Deque<Chapter> crawledChapters = crawlChapterList(manga, page);
			Deque<Chapter> mergedChapters = CrawlUtils.mergeChapterList(manga.getChapters(), crawledChapters);
			manga.setChapters(mergedChapters);
		} catch (IOException | IllegalArgumentException e) {
			LOGGER.debug("Could not get manga '{}' page at '{}'!", manga.getTitle(), manga.getUrl(), e);
			throw new NoResultFoundException();
		}
		return manga;
	}

	/**
	 * Finds the list of the chapter links.
	 * @param manga : the manga this is about.
	 * @param page : the manga details page.
	 * @return the list of chapters found.
	 */
	private Deque<Chapter> crawlChapterList(Manga manga, Document page) {
		// Initializes the list and gets the chapter links.
		Deque<Chapter> chaptersCrawled = new ArrayDeque<>();
		Elements chapterLinks = page.select(".chlist .tips");

		// Browsing and adding all chapter links.
		for (Element chapterLink : chapterLinks) {
			String chapterNumber = chapterLink.text().replace(manga.getTitle(), "").trim();
			Chapter chapter = new ChapterBuilder().name(chapterLink.text()).number(chapterNumber).url(chapterLink.attr("abs:href")).createChapter();
			chaptersCrawled.add(chapter);
		}
		return chaptersCrawled;
	}

	/**
	 * Transforms a concatenated string into a string list.
	 * @param str : the concatenated string.
	 * @param separator : the separator.
	 * @return the list of the string elements.
	 */
	private List<String> toList(String str, String separator) {
		return Arrays.asList(str.split(separator));
	}

	/** {@inheritDoc} */
	@Override
	public Chapter crawlChapter(Manga manga, Chapter chapter) throws NoResultFoundException {
		try {
			LOGGER.debug("Crawling chapter no. {} from {}", chapter.getNumber(), manga.getTitle());
			Document page = Jsoup.connect(chapter.getUrl()).timeout(600000).get();

			// Getting the amount of pages (minus 1, because of comment page).
			int pageCount = page.select("#top_bar .l .m option").size() - 1;
			LOGGER.debug("The chapter has {} pages.", pageCount);

			// If the chapter already has the images… no need to crawl it again.
			if (chapter.getImagesUrls().size() > 0) {
				return chapter;
			}

			// Initializes tries & page counter.
			int currentPage = 2; // 2, not 1, because 1 is already downloaded.
			int tries = 0;

			// Getting the image URLs.
			do {
				tries = 0;
				// Getting the image URL and the next chapter link.
				Element imageElement = page.select("#viewer img").first();

				if (imageElement != null) {
					// Adding the image URL.
					String imageUrl = imageElement.attr("abs:src");
					chapter.getImagesUrls().add(imageUrl);
				} else {
					LOGGER.warn("Image no. {}, from chapter {} of {} not found, attempt #{}.", currentPage, chapter.getNumber(), manga.getTitle(), tries);
					tries++;
					Thread.sleep(500);
				}

				// Getting the next page.
				do {
					String nextUrl = chapter.getUrl().replaceAll("[0-9]+\\.html", currentPage + ".html");
					page = getNextChapterPage(nextUrl);
					currentPage++;
				} while (page == null && currentPage <= pageCount);
			} while (currentPage <= pageCount && tries < 5);
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Could not crawl chapter page!", e);
		}
		chapter.updatePageCount();
		return chapter;
	}

	/**
	 * Tries up to 5 times to get the next chapter page.
	 * @param page : the document
	 * @param nextUrl
	 * @return
	 * @throws InterruptedException
	 */
	private Document getNextChapterPage(String nextUrl) throws InterruptedException {
		Document page = null;
		int tries = 0;
		boolean ok = false;
		do {
			try {
				page = Jsoup.connect(nextUrl).timeout(600000).get();
				ok = true;
			} catch (IOException e) {
				LOGGER.warn("Could not load url '{}', attempt #{}", nextUrl, tries);
				ok = false;
				tries++;
				Thread.sleep(100);
			}
		} while (!ok && tries < 5);
		return page;
	}

	/** {@inheritDoc} */
	@Override
	public Deque<Manga> getUpdatedMangaList() {
		Deque<Manga> updateList = new ArrayDeque<>();
		try {
			Document page = Jsoup.connect(RECENT_URL).get();

			// Gather all the update links.
			Elements updateLinks = page.select("#updates li");
			for (Element updateLink : updateLinks) {
				// Gets the title from the link.
				Element title = updateLink.select(".title a").first();
				Manga manga = new MangaBuilder().title(title.text()).url(title.attr("abs:href")).source(CrawlSource.MANGA_FOX).createManga();
				updateList.add(manga);
				LOGGER.debug("Manga named '{}' found.", manga.getTitle());
			}
		} catch (IOException e) {
			LOGGER.error("Could not get recent updates page!", e);
		}
		return updateList;
	}
}
