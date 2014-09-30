package com.deguitard.otakrawl.services.crawler;

import java.util.Deque;

import org.junit.Assert;
import org.junit.Test;

import com.deguitard.otakrawl.model.Chapter;
import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.model.builders.ChapterBuilder;
import com.deguitard.otakrawl.model.builders.MangaBuilder;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;

public class StarKanaCrawlerTest {

	/** Crawler implementation to test. */
	private StarKanaCrawler crawler = new StarKanaCrawler();

	/**
	 * Test of the {@link StarKanaCrawler#crawlMangaList()} method.
	 */
	@Test
	public void crawlAllMangasTest() {
		// Method call.
		Deque<Manga> mangaList = crawler.crawlMangaList();

		// Checking the list.
		Assert.assertNotNull("The crawler returned null value.", mangaList);
		Assert.assertFalse("The manga list was empty.", mangaList.isEmpty());

		// "Naruto test", it would be strange not to find Naruto.
		Manga mangaTest = new MangaBuilder().title("Naruto").url("http://starkana.com/manga/N/Naruto").createManga();
		Assert.assertTrue("Manga list seems to be incomplete or corrupted.", mangaList.contains(mangaTest));
	}

	/**
	 * Test of the {@link StarKanaCrawler#crawlManga(String)} method.
	 */
	@Test
	public void crawlMangaTest() {
		try {
			Manga manga = new MangaBuilder().title("Naruto").createManga();
			crawler.crawlManga(manga);
			Assert.assertEquals("Incorrect manga title.", "Naruto", manga.getTitle());
			Assert.assertEquals("Incorrect manga year.", new Integer(1999), manga.getYear());
			Assert.assertTrue("At least one author is missing.", manga.getAuthors().contains("KISHIMOTO Masashi"));
			Assert.assertTrue("At least one artist is missing.", manga.getArtists().contains("KISHIMOTO Masashi"));
			Assert.assertTrue("At least one genre is missing.", manga.getGenres().contains("Shounen"));
			Assert.assertTrue("The summary is incomplete.", manga.getSummary().contains("Naruto Uzumaki"));
			Assert.assertNotNull("No chapter were found.", manga.getChapters());
			Assert.assertFalse("The chapter list was empty.", manga.getChapters().isEmpty());
			Assert.assertTrue("Some chapters were missing.", manga.getChapters().size() > 600);
			Assert.assertEquals("The thumbnail found is incorrect.", "http://starkana.com/upload/covers/new/thumbs/default_15_16737.png", manga.getThumbnail());
		} catch (NoResultFoundException e) {
			Assert.fail("Test manga could not be found.");
		}
	}

	/**
	 * Test of the {@link StarKanaCrawler#crawlManga(String)} method.
	 */
	@Test
	public void crawlIncompleteMangaTest() {
		try {
			Manga manga = new MangaBuilder().title("17 O'Clocks (One shot)").createManga();
			crawler.crawlManga(manga);
			Assert.assertEquals("Incorrect manga title.", "17 O'Clocks (One shot)", manga.getTitle());
			Assert.assertEquals("Incorrect manga year.", new Integer(2010), manga.getYear());
			Assert.assertTrue("At least one author is missing.", manga.getAuthors().contains("Sakai Mayu"));
			Assert.assertTrue("At least one genre is missing.", manga.getGenres().contains("Shoujo"));
			Assert.assertTrue("The summary is incomplete.", manga.getSummary().contains("space for breathing"));
			Assert.assertEquals("The thumbnail found is incorrect.", "http://starkana.com/upload/covers/1/thumbs/default_17_O_Clocks_1340191496.jpg", manga.getThumbnail());
		} catch (NoResultFoundException e) {
			Assert.fail("Test manga could not be found.");
		}
	}

	/**
	 * Test of the {@link StarKanaCrawler#crawlChapter(Manga, Chapter)} method.
	 */
	@Test
	public void crawlChapterTest() {
		try {
			Manga manga = new MangaBuilder().title("Naruto").createManga();
			Chapter chapter = new ChapterBuilder().number("272").url("http://starkana.com/manga/N/Naruto/chapter/272").createChapter();
			chapter = crawler.crawlChapter(manga, chapter);

			Assert.assertNotNull("The crawler returned null value.", chapter);
			Assert.assertFalse("The chapter images list was empty.", chapter.getImagesUrls().isEmpty());
			Assert.assertEquals("The amount of gathered image URLs is incorrect.", 18, chapter.getImagesUrls().size());
			Assert.assertTrue("Some image URLs are missing.", chapter.getImagesUrls().contains("http://starkana.com/manga-img/741/27323/naruto_ch272_p10-11.png"));
		} catch (NoResultFoundException e) {
			Assert.fail("Test chapter could not be found.");
		}
	}

	@Test
	public void getUpdatedMangaListTest() {
		Deque<Manga> updatedMangas = crawler.getUpdatedMangaList();
		Assert.assertFalse("There should be at least one updated manga.", updatedMangas.isEmpty());
		Manga testManga = new MangaBuilder().title("Fuuka").url("http://starkana.com/manga/F/Fuuka").createManga();
		Assert.assertTrue("Some manga are missing.", updatedMangas.contains(testManga));
	}
}
