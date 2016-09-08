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
import com.deguitard.otakrawl.services.crawler.mangahere.MangaHereCrawler;
import com.deguitard.otakrawl.services.crawler.provider.CrawlSource;
import com.deguitard.otakrawl.services.crawler.provider.MangaHere;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;
import com.google.inject.Inject;

@RunWith(OtakrawlTestRunner.class)
@GuiceModules({OtakrawlModule.class})
public class MangaHereCrawlerTest {

	/** Crawler implementation to test. */
	@Inject @MangaHere
	private ICrawler crawler;

	/**
	 * Test of the {@link MangaHereCrawler#crawlMangaList()} method.
	 */
	@Test
	public void crawlAllMangasTest() {
		// Method call.
		Deque<Manga> mangaList = crawler.crawlMangaList();

		// Checking the list.
		Assert.assertNotNull("The crawler returned null value.", mangaList);
		Assert.assertFalse("The manga list was empty.", mangaList.isEmpty());

		// "Naruto test", it would be strange not to find Naruto.
		Manga mangaTest = new MangaBuilder().title("Naruto").url("http://www.mangahere.co/manga/naruto/").source(CrawlSource.MANGA_HERE).createManga();
		Assert.assertTrue("Manga list seems to be incomplete or corrupted.", mangaList.contains(mangaTest));
	}

	/**
	 * Test of the {@link MangaHereCrawler#crawlManga(String)} method.
	 */
	@Test
	public void crawlMangaTest() {
		try {
			Manga manga = new MangaBuilder().title("Chobits").url("http://www.mangahere.co/manga/chobits/").createManga();
			crawler.crawlManga(manga);
			Assert.assertEquals("Incorrect manga title.", "Chobits", manga.getTitle());
			Assert.assertTrue("At least one alt title is missing.", manga.getAltTitles().contains("人形电脑天使心"));
			Assert.assertTrue("At least one author is missing.", manga.getAuthors().contains("Clamp"));
			Assert.assertTrue("At least one artist is missing.", manga.getArtists().contains("Clamp"));
			Assert.assertTrue("At least one genre is missing.", manga.getGenres().contains("Romance"));
			Assert.assertTrue("The summary is incomplete.", manga.getSummary().contains("a few embarrassing situations"));
			Assert.assertNotNull("No chapter were found.", manga.getChapters());
			Assert.assertFalse("The chapter list was empty.", manga.getChapters().isEmpty());
			Assert.assertTrue("Some chapters were missing.", manga.getChapters().size() > 80);
			Assert.assertEquals("The thumbnail found is incorrect.", "http://m.mhcdn.net/store/manga/26/cover.jpg?v=1369646798", manga.getThumbnail());
		} catch (NoResultFoundException e) {
			Assert.fail("Test manga could not be found.");
		}
	}

	/**
	 * Test of the {@link MangaHereCrawler#crawlManga(String)} method.
	 */
	@Test
	public void crawlLicensedMangaTest() {
		try {
			Manga manga = new MangaBuilder().title("Naruto").url("http://www.mangahere.co/manga/naruto/").createManga();
			crawler.crawlManga(manga);
			Assert.assertEquals("Incorrect manga title.", "Naruto", manga.getTitle());
			Assert.assertTrue("At least one alt title is missing.", manga.getAltTitles().contains("นินจาคาถาโอ้โฮเฮะ"));
			Assert.assertTrue("At least one author is missing.", manga.getAuthors().contains("Kishimoto Masashi"));
			Assert.assertTrue("At least one artist is missing.", manga.getArtists().contains("Kishimoto Masashi"));
			Assert.assertTrue("At least one genre is missing.", manga.getGenres().contains("Shounen"));
			Assert.assertTrue("The summary is incomplete.", manga.getSummary().contains("become the Hokage"));
			Assert.assertTrue("The chapter list isn't empty.", manga.getChapters().isEmpty());
		} catch (NoResultFoundException e) {
			Assert.fail("Test manga could not be found.");
		}
	}

	/**
	 * Test of the {@link MangaHereCrawler#crawlChapter(Manga, Chapter)} method.
	 */
	@Test
	public void crawlChapterTest() {
		try {
			Manga manga = new MangaBuilder().title("Negative Happy Chain Saw Edge").createManga();
			Chapter chapter = new ChapterBuilder().number("4").url("http://www.mangahere.co/manga/negative_happy_chain_saw_edge/v01/c004/").createChapter();
			chapter = crawler.crawlChapter(manga, chapter);

			Assert.assertNotNull("The crawler returned null value.", chapter);
			Assert.assertFalse("The chapter images list was empty.", chapter.getImagesUrls().isEmpty());
			Assert.assertEquals("The amount of gathered image URLs is incorrect.", 42, chapter.getImagesUrls().size());
			Assert.assertTrue("Some image URLs are missing.", chapter.getImagesUrls().contains("http://z.mhcdn.net/store/manga/1247/01-004.0/compressed/01_152.jpg?v=11214019003"));
		} catch (NoResultFoundException e) {
			Assert.fail("Test chapter could not be found.");
		}
	}

	@Test
	public void getUpdatedMangaListTest() {
		Deque<Manga> updatedMangas = crawler.getUpdatedMangaList();
		Assert.assertFalse("There should be at least one updated manga.", updatedMangas.isEmpty());
		Manga testManga = new MangaBuilder().title("Wildcard").url("http://www.mangahere.co/manga/wildcard/").source(CrawlSource.MANGA_HERE).createManga();
		Assert.assertTrue("Some manga are missing.", updatedMangas.contains(testManga));
	}
}
