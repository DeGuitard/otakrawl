package com.deguitard.otakrawl.builders;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.deguitard.otakrawl.model.Chapter;
import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.model.builders.MangaBuilder;
import com.deguitard.otakrawl.services.crawler.provider.CrawlSource;

/**
 * Tests the mapping done by {@link MangaBuilder}.
 *
 * @author Vianney Dupoy de Guitard
 */
public class MangaBuilderTest {

	/** Int test value. */
	private final Integer testInt = 10;

	/** String test value. */
	private final String testString = "Test";

	/** String list test value. */
	private final List<String> testStringList = Arrays.asList(testString);

	/** Chapter list test value. */
	private final Deque<Chapter> testChapters = new ArrayDeque<>(Arrays.asList(new Chapter()));

	/** Crawler source test value. */
	private final CrawlSource testSource = CrawlSource.MANGA_HERE;

	/**
	 * Tests all the fields.
	 */
	@Test
	public void testAllFields() {
		// Initializes a builder.
		MangaBuilder builder = new MangaBuilder().title(testString).url(testString).thumbnail(testString).summary(testString);
		builder.altTitles(testStringList).authors(testStringList).artists(testStringList).genres(testStringList).suggestions(testStringList);
		builder.source(testSource).year(testInt).chapters(testChapters);

		// Creates the result.
		Manga result = builder.createManga();

		// Checks the string values.
		Assert.assertEquals(testString, result.getTitle());
		Assert.assertEquals(testString, result.getUrl());
		Assert.assertEquals(testString, result.getThumbnail());
		Assert.assertEquals(testString, result.getSummary());

		// Checks the lists.
		Assert.assertEquals(testStringList, result.getAltTitles());
		Assert.assertEquals(testStringList, result.getAuthors());
		Assert.assertEquals(testStringList, result.getArtists());
		Assert.assertEquals(testStringList, result.getGenres());
		Assert.assertEquals(testStringList, result.getSuggestions());
		Assert.assertEquals(testChapters, result.getChapters());

		// Checks the other values.
		Assert.assertEquals(testSource, result.getSource());
		Assert.assertEquals(testInt, result.getYear());
	}
}
