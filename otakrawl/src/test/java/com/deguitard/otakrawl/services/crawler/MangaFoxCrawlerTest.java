package com.deguitard.otakrawl.services.crawler;

import java.util.Deque;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.deguitard.otakrawl.OtakrawlTestRunner;
import com.deguitard.otakrawl.OtakrawlTestRunner.GuiceModules;
import com.deguitard.otakrawl.model.Chapter;
import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.model.builders.ChapterBuilder;
import com.deguitard.otakrawl.model.builders.MangaBuilder;
import com.deguitard.otakrawl.services.OtakrawlModule;
import com.deguitard.otakrawl.services.crawler.mangafox.MangaFoxCrawler;
import com.deguitard.otakrawl.services.crawler.provider.CrawlSource;
import com.deguitard.otakrawl.services.crawler.provider.MangaFox;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;
import com.google.inject.Inject;

@RunWith(OtakrawlTestRunner.class)
@GuiceModules({OtakrawlModule.class})
public class MangaFoxCrawlerTest {

	/** Crawler implementation to test. */
	@Inject @MangaFox
	private ICrawler crawler;

	/**
	 * Test of the {@link MangaFoxCrawler#crawlMangaList()} method.
	 */
	@Test
	public void crawlAllMangasTest() {
		// Method call.
		Deque<Manga> mangaList = crawler.crawlMangaList();

		// Checking the list.
		Assert.assertNotNull("The crawler returned null value.", mangaList);
		Assert.assertFalse("The manga list was empty.", mangaList.isEmpty());

		// "Naruto test": it would be strange not to find Naruto.
		Manga mangaTest = new MangaBuilder().title("Naruto").mangaFoxId("8").url("http://mangafox.me/manga/naruto/").source(CrawlSource.MANGA_FOX).createManga();
		Assert.assertTrue("Manga list seems to be incomplete or corrupted.", mangaList.contains(mangaTest));
	}

	/**
	 * Test of the {@link MangaFoxCrawler#crawlManga(String)} method.
	 */
	@Test
	public void crawlMangaTest() {
		try {
			Manga manga = new MangaBuilder().title("Naruto").mangaFoxId("8").url("http://mangafox.me/manga/naruto/").createManga();
			crawler.crawlManga(manga);
			Assert.assertEquals("Incorrect manga title.", "Naruto", manga.getTitle());
			Assert.assertEquals("Incorrect manga year.", new Integer(1999), manga.getYear());
			Assert.assertTrue("At least one author is missing.", manga.getAuthors().contains("Kishimoto Masashi"));
			Assert.assertTrue("At least one artist is missing.", manga.getArtists().contains("Kishimoto Masashi"));
			Assert.assertTrue("At least one genre is missing.", manga.getGenres().contains("Shounen"));
			Assert.assertTrue("The summary is incomplete.", manga.getSummary().contains("Naruto Uzumaki"));
			Assert.assertNotNull("No chapter were found.", manga.getChapters());
			Assert.assertFalse("The chapter list was empty.", manga.getChapters().isEmpty());
			Assert.assertTrue("Some chapters were missing.", manga.getChapters().size() >= 700);
			Assert.assertTrue("The thumbnail found is incorrect.", manga.getThumbnail().contains("mfcdn.net"));
			Assert.assertFalse("Naruto is NOT ongoing!", manga.isOngoing());
		} catch (NoResultFoundException e) {
			Assert.fail("Test manga could not be found.");
		}
	}

	/**
	 * Test of the {@link MangaFoxCrawler#crawlManga(String)} method with an incomplete manga.
	 */
	@Test
	public void crawlIncompleteMangaTest() {
		try {
			Manga manga = new MangaBuilder().title("A Cup Of...").url("http://mangafox.me/manga/a_cup_of/").mangaFoxId("a-cup-of---").createManga();
			crawler.crawlManga(manga);
			Assert.assertEquals("Incorrect manga title.", "A Cup Of...", manga.getTitle());
			Assert.assertEquals("Incorrect manga year.", new Integer(2009), manga.getYear());
			Assert.assertTrue("At least one author is missing.", manga.getAuthors().contains("Hamako Shiori"));
			Assert.assertTrue("At least one artist is missing.", manga.getArtists().contains("Hamako Shiori"));
			Assert.assertTrue("At least one genre is missing.", manga.getGenres().contains("Shoujo"));
			Assert.assertNull("The summary should be null.", manga.getSummary());
			Assert.assertNotNull("No chapter were found.", manga.getChapters());
			Assert.assertFalse("The chapter list was empty.", manga.getChapters().isEmpty());
			Assert.assertTrue("Some chapters were missing.", manga.getChapters().size() == 1);
			Assert.assertTrue("The thumbnail found is incorrect.", manga.getThumbnail().contains("mfcdn.net"));
			Assert.assertTrue("It should be ongoing.", manga.isOngoing());
		} catch (NoResultFoundException e) {
			Assert.fail("Test manga could not be found.");
		}
	}

	/**
	 * Test of the {@link MangaFoxCrawler#crawlManga(String)} method with an incomplete manga #2.
	 */
	@Test
	public void crawlIncompleteMangaTest2() {
		try {
			Manga manga = new MangaBuilder().title("-Rain-").url("http://mangafox.me/manga/rain/").mangaFoxId("rain").createManga();
			crawler.crawlManga(manga);
			Assert.assertEquals("Incorrect manga title.", "-Rain-", manga.getTitle());
			Assert.assertEquals("Incorrect manga year.", new Integer(2008), manga.getYear());
			Assert.assertTrue("At least one author is missing.", manga.getAuthors().contains("Hayase Hashiba"));
			Assert.assertTrue("At least one artist is missing.", manga.getArtists().contains("Hayase Hashiba"));
			Assert.assertTrue("At least one genre is missing.", manga.getGenres().contains("Drama"));
			Assert.assertTrue("The summary should be null.", manga.getSummary().contains("On a rainy day"));
			Assert.assertNotNull("No chapter were found.", manga.getChapters());
			Assert.assertFalse("The chapter list was empty.", manga.getChapters().isEmpty());
			Assert.assertTrue("Some chapters were missing.", manga.getChapters().size() == 1);
			Assert.assertTrue("The thumbnail found is incorrect.", manga.getThumbnail().contains("mfcdn.net"));
		} catch (NoResultFoundException e) {
			Assert.fail("Test manga could not be found.");
		}
	}

	/**
	 * Test of the {@link MangaFoxCrawler#crawlChapter(Manga, Chapter)} method.
	 */
	@Test
	public void crawlChapterTest() {
		try {
			Manga manga = new MangaBuilder().createManga();
			Chapter chapter = new ChapterBuilder().url("http://mangafox.me/manga/rain/v00/c000/1.html").number("0").createChapter();
			chapter = crawler.crawlChapter(manga, chapter);

			Assert.assertNotNull("The crawler returned null value.", chapter);
			Assert.assertEquals("The chapter name is invalid.", "0", chapter.getNumber());
			Assert.assertFalse("The chapter images list was empty.", chapter.getImagesUrls().isEmpty());
			Assert.assertEquals("The amount of gathered image URLs is incorrect.", 16, chapter.getImagesUrls().size());
		} catch (NoResultFoundException e) {
			Assert.fail("Test chapter could not be found.");
		}
	}

	/**
	 * Test of the {@link MangaFoxCrawler#crawlChapter(Manga, Chapter)} method with a corrupt chapter.
	 */
	@Test
	public void crawlCorruptChapterTest() {
		try {
			Manga manga = new MangaBuilder().createManga();
			Chapter chapter = new ChapterBuilder().url("http://mangafox.me/manga/accel_world/v02/c015/1.html").number("15").createChapter();
			chapter = crawler.crawlChapter(manga, chapter);

			Assert.assertNotNull("The crawler returned null value.", chapter);
			Assert.assertEquals("The chapter name is invalid.", "15", chapter.getNumber());
			Assert.assertFalse("The chapter images list was empty.", chapter.getImagesUrls().isEmpty());
			Assert.assertEquals("The amount of gathered image URLs is incorrect.", 58, chapter.getImagesUrls().size());
		} catch (NoResultFoundException e) {
			Assert.fail("Test chapter could not be found.");
		}
	}

	/**
	 * Test of the {@link MangaFoxCrawler#crawlChapter(Manga, Chapter)} method with a corrupt chapter.
	 */
	@Test
	public void crawlCorruptChapterTest2() {
		try {
			Manga manga = new MangaBuilder().createManga();
			Chapter chapter = new ChapterBuilder().url("http://mangafox.me/manga/arachnid/v09/c042/1.html").number("42").createChapter();
			chapter = crawler.crawlChapter(manga, chapter);

			Assert.assertNotNull("The crawler returned null value.", chapter);
			Assert.assertEquals("The chapter name is invalid.", "42", chapter.getNumber());
			Assert.assertFalse("The chapter images list was empty.", chapter.getImagesUrls().isEmpty());
			Assert.assertEquals("The amount of gathered image URLs is incorrect.", 44, chapter.getImagesUrls().size());
		} catch (NoResultFoundException e) {
			Assert.fail("Test chapter could not be found.");
		}
	}

	/**
	 * Test of the {@link MangaFoxCrawler#getUpdatedMangaList()} method.
	 */
	@Test
	public void getUpdatedMangaListTest() {
		Deque<Manga> updatedMangas = crawler.getUpdatedMangaList();
		Assert.assertFalse("There should be at least one updated manga.", updatedMangas.isEmpty());
		for (Manga manga : updatedMangas) {
			Assert.assertNotNull("URL should be specified.", manga.getUrl());
			Assert.assertNotNull("Title should be specified.", manga.getTitle());
		}
	}
}
