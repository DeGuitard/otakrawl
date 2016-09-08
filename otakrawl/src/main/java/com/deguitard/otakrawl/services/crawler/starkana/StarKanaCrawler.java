package com.deguitard.otakrawl.services.crawler.starkana;

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
 * This crawler is implemented for the http://starkana.com website.
 *
 * @author Vianney Dupoy de Guitard
 */
public class StarKanaCrawler implements ICrawler {

	/** Applicative logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(StarKanaCrawler.class);

	/** URL of the website. */
	private static final String WEBSITE_URL = "http://starkana.com/";
	/** URL of the page listing all available mangas. */
	private static final String LIST_URL = WEBSITE_URL + "manga/list";

	/** Label of the titles row. */
	private static final String TITLE_LABEL = "Title(s):";
	/** Label of the authors row. */
	private static final String AUTHOR_LABEL = "Author(s):";
	/** Label of the artists row. */
	private static final String ARTIST_LABEL = "Artist(s):";
	/** Label of the genres row. */
	private static final String GENRE_LABEL = "Genres:";
	/** Label of the year row. */
	private static final String YEAR_LABEL = "Start Date:";
	/** Label of the summary row. */
	private static final String SUMMARY_LABEL = "Summary:";

	/** {@inheritDoc} */
	public Deque<Manga> crawlMangaList() {
		LOGGER.info("Crawling StarKana manga list.");
		Deque<Manga> mangaList = new ArrayDeque<Manga>();

		try {
			LOGGER.debug("Downloading page '{}'", LIST_URL);
			Document page = Jsoup.connect(LIST_URL).maxBodySize(0).timeout(600000).get();

			// All links to mangas are in div with style "c_h2" or "c_h2b", and the link is basically the manga name.
			Elements mangaLinks = page.select(".c_h2 a, .c_h2b a");
			for (Element mangaLink : mangaLinks) {
				if (mangaLink.text().equals("&")) {
					continue;
				}
				Manga manga = new MangaBuilder().title(mangaLink.text()).url(mangaLink.attr("abs:href")).source(CrawlSource.STARKANA).createManga();
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
	public Manga crawlManga(Manga manga) throws NoResultFoundException {
		if (StringUtils.isBlank(manga.getTitle())) {
			throw new IllegalArgumentException("The manga title was not set.");
		}

		LOGGER.info("Crawling data of manga named '{}'.", manga.getTitle());

		try {
			// Downloads the webpage.
			if (StringUtils.isBlank(manga.getUrl())) {
				manga.setUrl(StarKanaUrlFormatter.getMangaUrl(manga.getTitle()));
			}
			LOGGER.debug("Downloading page '{}'.", manga.getUrl());
			Document page = Jsoup.connect(manga.getUrl()).get();

			// Initializes all the elements describing the manga.
			Elements authorsElts = new Elements();
			Elements artistsElts = new Elements();
			Elements genresElts = new Elements();

			// StarKana has empty pages, check this one.
			if (page.select("#inner_page > div:nth-child(2) > span:nth-child(1)").first() != null) {
				LOGGER.error("The manga named '{}' has been removed!", manga.getTitle());
				throw new NoResultFoundException();
			}

			// Extracts all the interesting elements.
			Element thumbnail = page.select(".olol > img:nth-child(1)").first();
			Elements descRows = page.select("#inner_page > div:nth-child(5) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > table:nth-child(1) tr");
			manga.setThumbnail(thumbnail.attr("abs:src"));
			for (Element el : descRows) {
				String rowTitle = el.select("td:nth-child(1)").first().text();
				Element rowValue = el.select("td:nth-child(2)").first();
				switch (rowTitle) {
				case TITLE_LABEL:
					String[] titlesArray = rowValue.html().split("<br />");
					manga.setAltTitles(new ArrayList<String>());
					for (String title : titlesArray) {
						manga.getAltTitles().add(title.trim());
					}
					break;
				case AUTHOR_LABEL:
					authorsElts = rowValue.select("a");
					break;
				case ARTIST_LABEL:
					artistsElts = rowValue.select("a");
					break;
				case GENRE_LABEL:
					genresElts = rowValue.select("a");
					break;
				case YEAR_LABEL:
					String yearStr = rowValue.text();
					try {
						manga.setYear(Integer.valueOf(yearStr));
					} catch (NumberFormatException e) {
						LOGGER.warn("Could not fetch start date for manga '{}', incorrect format: '{}'", manga.getTitle(), yearStr);
					}
					break;
				case SUMMARY_LABEL:
					manga.setSummary(rowValue.text());
					break;
				}
			}

			// Converts and store lists.
			manga.setAuthors(CrawlUtils.getEltsText(authorsElts));
			manga.setArtists(CrawlUtils.getEltsText(artistsElts));
			manga.setGenres(CrawlUtils.getEltsText(genresElts));

			// Grabs the chapter list.
			LOGGER.debug("Crawling chapter list for {}.", manga.getTitle());
			Deque<Chapter> crawledChapters = crawlChapterList(page);
			Deque<Chapter> mergedChapters = CrawlUtils.mergeChapterList(manga.getChapters(), crawledChapters);
			manga.setChapters(mergedChapters);
			LOGGER.info("{} chapters crawled.", manga.getChapters().size());
		} catch (IOException e) {
			LOGGER.error("Could not get manga details page!", e);
			throw new NoResultFoundException();
		}

		return manga;
	}

	private Deque<Chapter> crawlChapterList(Document page) {
		Deque<Chapter> chapterList = new ArrayDeque<Chapter>();

		Elements chaptersLinks = page.select(".download-link");
		for (Element chapterLink : chaptersLinks) {
			String chapterNumber = chapterLink.select("strong").text();
			Chapter chapter = new ChapterBuilder().number(chapterNumber).url(chapterLink.attr("abs:href")).createChapter();
			chapterList.add(chapter);
		}

		return chapterList;
	}

	/** {@inheritDoc} */
	public Chapter crawlChapter(Manga manga, Chapter chapter) throws NoResultFoundException {
		LOGGER.info("Crawling chapter no. {} from {}.", chapter.getNumber(), manga.getTitle());

		try {
			// Downloads the chapter page
			String chapterUrl = chapter.getUrl() + "?scroll";
			LOGGER.debug("Downloading page '{}'.", chapterUrl);
			Document page = Jsoup.connect(chapterUrl).get();

			// Gets the images
			Elements imgElements = page.select(".dyn");
			for (Element img : imgElements) {
				String url = img.attr("abs:src");
				chapter.getImagesUrls().add(url);
				LOGGER.debug("URL '{}' added.", url);
			}

			// Updates the page count.
			chapter.updatePageCount();
		} catch (IOException e) {
			LOGGER.error("Could not get chapter list page!", e);
		}

		return chapter;
	}

	@Override
	public Deque<Manga> getUpdatedMangaList() {
		LOGGER.info("Crawling recently updated manga list.");
		Deque<Manga> mangaList = new ArrayDeque<>();
		try {
			Document page = Jsoup.connect(WEBSITE_URL).get();
			// All links to manga are either in c_h2 class, either in c_h2b class.
			Elements mangaLinks = page.select(".c_h2 a, .c_h2b a");
			for (Element mangaLink : mangaLinks) {
				// Title is set like this : <a>Title <i>Chapter</i> <b>400</b>, <b>402</b></a>, so we need to clean it.
				String title = mangaLink.html();
				title = title.substring(0, title.indexOf("<"));
				title = title.trim();

				String url = mangaLink.attr("abs:href");
				int offset = url.indexOf("/chapter/");
				if (offset != -1) {
					url = url.substring(0, url.indexOf("/chapter"));
				}

				Manga manga = new MangaBuilder().title(title).url(url).source(CrawlSource.STARKANA).createManga();
				mangaList.add(manga);
				LOGGER.debug("Manga named '{}' found.", manga.getTitle());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mangaList;
	}
}
