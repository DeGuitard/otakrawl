package com.deguitard.otakrawl.services.crawler.animeplanet;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.services.crawler.SuggestionCrawler;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;
import com.deguitard.otakrawl.services.persistence.dao.MangaDao;
import com.google.inject.Inject;

public class AnimePlanetSuggestionCrawler implements SuggestionCrawler {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnimePlanetSuggestionCrawler.class);
	private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
	private static final String SEARCH_URL = "http://www.anime-planet.com/manga/all?name=";

	@Inject
	private MangaDao mangaDao;

	@Override
	public Collection<Manga> findSuggestions(Manga manga) throws NoResultFoundException {
		Deque<Manga> suggestions = new ArrayDeque<>();
		LOGGER.info("Looking for suggestions for {}.", manga.getTitle());
		try {
			Document page = Jsoup.connect(SEARCH_URL + manga.getTitle()).userAgent(USER_AGENT).maxBodySize(0).get();
			Element resultLink = page.select("tr td a").first();
			if (resultLink == null) {
				LOGGER.warn("Could not find suggestions for {}.", manga.getTitle());
				throw new NoResultFoundException();
			}
			page = Jsoup.connect(resultLink.attr("abs:href")).userAgent(USER_AGENT).get();
			Element recommendContainer = page.select(".recommendations").first();
			Elements recommendLinks = recommendContainer.select("h4");
			List<String> recommendedTitles = new ArrayList<>();
			for (Element recommendLink : recommendLinks) {
				recommendedTitles.add(recommendLink.text());
			}
			suggestions = mangaDao.findByTitles(recommendedTitles);
			LOGGER.info("{} suggestions found.", suggestions.size());
		} catch (IOException e) {
			LOGGER.error("Could not find suggestions for {}.", manga.getTitle(), e);
			throw new NoResultFoundException();
		}
		return suggestions;
	}

}
