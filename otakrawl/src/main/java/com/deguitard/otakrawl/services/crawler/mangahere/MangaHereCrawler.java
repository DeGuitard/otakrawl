package com.deguitard.otakrawl.services.crawler.mangahere;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import org.apache.commons.lang.StringUtils;
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
 * This crawler is implemented for the http://mangahere.co website.
 *
 * @author Vianney Dupoy de Guitard
 */
public class MangaHereCrawler implements ICrawler {

	/** Applicative logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MangaHereCrawler.class);

	/** URL of the website. */
	private static final String WEBSITE_URL = "http://mangahere.co/";
	/** URL of the page listing all available mangas. */
	private static final String LIST_URL = WEBSITE_URL + "mangalist/";
	/** URL of the page listing all recent updates. */
	private static final String LATEST_URL = WEBSITE_URL + "latest/";

	/** {@inheritDoc} */
	@Override
	public Deque<Manga> crawlMangaList() {
		LOGGER.info("Crawling MangaHere manga list.");
		Deque<Manga> mangaList = new ArrayDeque<Manga>();

		try {
			LOGGER.debug("Downloading page '{}'", LIST_URL);
			Document page = Jsoup.connect(LIST_URL).maxBodySize(0).timeout(600000).get();

			Elements mangaLinks = page.select(".manga_info");
			for (Element mangaLink : mangaLinks) {
				Manga manga = new MangaBuilder().title(mangaLink.text()).url(mangaLink.attr("abs:href")).source(CrawlSource.MANGA_HERE).createManga();
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
		if (StringUtils.isBlank(manga.getTitle())) {
			throw new IllegalArgumentException("The manga title was not set.");
		}

		LOGGER.info("Crawling data of manga named '{}'.", manga.getTitle());

		try {
			// Downloads the webpage.
			LOGGER.debug("Downloading page '{}'.", manga.getUrl());
			Document page = Jsoup.connect(manga.getUrl()).timeout(600000).get();

			// Extracts all the interesting elements.
			Elements authorsElts = page.select(".detail_topText > li:nth-child(5) > a");
			Elements artistsElts = page.select(".detail_topText > li:nth-child(6) > a");
			Element thumbnail = page.select(".img").first();
			Element summary = page.select("#show").first();
			Element altTitles = page.select(".detail_topText > li:nth-child(3)").first();
			Element genres = page.select(".detail_topText > li:nth-child(4)").first();

			// Adds extracted elements values to the manga.
			manga.setThumbnail(thumbnail.attr("abs:src"));
			manga.setSummary(summary.ownText());

			// Converts and store lists.
			manga.setAuthors(CrawlUtils.getEltsText(authorsElts));
			manga.setArtists(CrawlUtils.getEltsText(artistsElts));
			manga.setGenres(new ArrayList<String>());
			manga.setAltTitles(new ArrayList<String>());
			for (String genre : genres.ownText().split(", ")) {
				manga.getGenres().add(genre);
			}
			for (String altTitle : altTitles.ownText().split("; ")) {
				manga.getAltTitles().add(altTitle);
			}

			// Grabs the chapter list.
			LOGGER.debug("Crawling chapter list for {}.", manga.getTitle());
			Deque<Chapter> crawledChapters = crawlChapterList(page, manga.getTitle());
			Deque<Chapter> mergedChapters = CrawlUtils.mergeChapterList(manga.getChapters(), crawledChapters);
			manga.setChapters(mergedChapters);
			LOGGER.info("{} chapters crawled.", manga.getChapters().size());
		} catch (IOException e) {
			LOGGER.error("Could not get manga details page!", e);
			throw new NoResultFoundException();
		}

		return manga;
	}

	private Deque<Chapter> crawlChapterList(Document page, String title) {
		Deque<Chapter> chapterList = new ArrayDeque<Chapter>();

		Elements chaptersLinks = page.select(".detail_list a.color_0077");
		for (Element chapterLink : chaptersLinks) {
			String chapterNumber = chapterLink.text().replace(title, "").trim();
			Chapter chapter = new ChapterBuilder().number(chapterNumber).url(chapterLink.attr("abs:href")).createChapter();
			chapterList.add(chapter);
		}

		return chapterList;
	}

	/** {@inheritDoc} */
	@Override
	public Chapter crawlChapter(Manga manga, Chapter chapter) throws NoResultFoundException {
		LOGGER.info("Crawling chapter no. {} from {}.", chapter.getNumber(), manga.getTitle());

		try {
			// Downloads the first chapter page
			Document page = Jsoup.connect(chapter.getUrl()).timeout(600000).get();

			// Gets the page selector and the first image.
			Element pageSelector = page.select("div.go_page select.wid60").first();
			int pageCount = pageSelector.select("option").size();
			int pageNumber = 1;

			do {
				Element image = page.select("#image").first();
				String url = image.attr("abs:src");
				chapter.getImagesUrls().add(url);
				LOGGER.debug("URL '{}' added.", url);
				pageNumber++;
				page = Jsoup.connect(chapter.getUrl() + pageNumber + ".html").timeout(600000).get();
			} while (pageNumber <= pageCount);

		} catch (IOException e) {
			LOGGER.error("Could not get chapter list page!", e);
		}

		return chapter;
	}

	/** {@inheritDoc} */
	@Override
	public Deque<Manga> getUpdatedMangaList() {
		LOGGER.info("Crawling recently updated manga list.");
		Deque<Manga> mangaList = new ArrayDeque<>();
		try {
			Document page = Jsoup.connect(LATEST_URL).timeout(600000).get();
			Elements updateBlocks = page.select(".manga_updates dl");
			for (Element updateBlock : updateBlocks) {
				// Only consider recent updates.
				String date = updateBlock.select(".time").first().text();
				if (!date.contains("Today")) {
					continue;
				}

				Element mangaLink = updateBlock.select(".manga_info").first();
				String title = mangaLink.text();
				String url = mangaLink.attr("abs:href");

				Manga manga = new MangaBuilder().title(title).url(url).source(CrawlSource.MANGA_HERE).createManga();
				mangaList.add(manga);
				LOGGER.debug("Manga named '{}' found.", manga.getTitle());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mangaList;
	}
}
