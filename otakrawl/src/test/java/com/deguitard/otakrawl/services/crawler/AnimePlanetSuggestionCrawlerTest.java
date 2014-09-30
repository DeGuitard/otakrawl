package com.deguitard.otakrawl.services.crawler;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.deguitard.otakrawl.OtakrawlTestRunner;
import com.deguitard.otakrawl.OtakrawlTestRunner.GuiceModules;
import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.model.builders.MangaBuilder;
import com.deguitard.otakrawl.services.OtakrawlModule;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;
import com.google.inject.Inject;

@RunWith(OtakrawlTestRunner.class)
@GuiceModules(OtakrawlModule.class)
public class AnimePlanetSuggestionCrawlerTest {

	@Inject
	private AnimePlanetSuggestionCrawler crawler;

	@Test
	public void getSuggestionsTest() {
		try {
			Manga manga = new MangaBuilder().title("Naruto").createManga();
			Collection<Manga> suggestions = crawler.findSuggestions(manga);
			Assert.assertFalse("Some suggestions are missing.", suggestions.isEmpty());
		} catch (NoResultFoundException e) {
			e.printStackTrace();
		}
	}
}
