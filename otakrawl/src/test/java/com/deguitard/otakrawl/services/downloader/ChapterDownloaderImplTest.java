package com.deguitard.otakrawl.services.downloader;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.deguitard.otakrawl.OtakrawlTestRunner;
import com.deguitard.otakrawl.OtakrawlTestRunner.GuiceModules;
import com.deguitard.otakrawl.model.Chapter;
import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.model.builders.ChapterBuilder;
import com.deguitard.otakrawl.model.builders.MangaBuilder;
import com.deguitard.otakrawl.services.OtakrawlModule;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;
import com.deguitard.otakrawl.services.tools.PropertyKey;
import com.deguitard.otakrawl.services.tools.PropertyService;
import com.google.inject.Inject;

/**
 * Unit test for {@link ChaptersDownloaderImpl}.
 *
 * @author Vianney Dupoy de Guitard
 */
@RunWith(OtakrawlTestRunner.class)
@GuiceModules({OtakrawlModule.class})
public class ChapterDownloaderImplTest {

	/** Downloader implementation to test. */
	@Inject private ChaptersDownloaderImpl chaptersDownloader = new ChaptersDownloaderImpl();

	/** Property service used to retrieve the download folder path. */
	@Inject private PropertyService propertyService;

	/** Chapter to test. */
	private Chapter chapter = new ChapterBuilder().number("1").imagesUrls(new ArrayList<String>()).createChapter();

	/** Manga to test. */
	private Manga manga = new MangaBuilder().title("Test").chapters(new ArrayDeque<Chapter>()).createManga();

	/** Manga list. */
	private Deque<Manga> mangaList = new ArrayDeque<Manga>();

	/**
	 * Sets up the test data.
	 */
	@Before
	public void setUp() {
		manga.setId(new ObjectId());
		chapter.setId(new ObjectId());
		manga.getChapters().add(chapter);
		mangaList.add(manga);
	}

	/**
	 * Test of the {@link ChaptersDownloaderImpl#download(java.util.Deque)} method.
	 */
	@Test
	public void downloadTestOk() throws NoResultFoundException {
		// Sets the thumbnail to download.
		manga.setThumbnail("http://starkana.com/upload/covers/new/thumbs/default_99744_21726.jpg");

		// Sets two chapter pages to download.
		chapter.getImagesUrls().add("http://z.mhcdn.net/store/manga/246/38-401.0/compressed/n001.jpg");
		chapter.getImagesUrls().add("http://z.mhcdn.net/store/manga/246/38-401.0/compressed/n002-003.jpg");
		chaptersDownloader.download(mangaList);

		// Creates the expected file paths.
		String thumbnailPath = chaptersDownloader.getThumbnailFilePath(manga);
		String firstFilePath = chaptersDownloader.getChapterFilePath(manga, chapter, 0, chapter.getImagesUrls().get(0));
		String secondFilePath = chaptersDownloader.getChapterFilePath(manga, chapter, 1, chapter.getImagesUrls().get(1));

		// Finds the expected files.
		File thumbnailFile = new File(thumbnailPath);
		File firstFile = new File(firstFilePath);
		File secondFile = new File(secondFilePath);

		// Checks the files are created.
		Assert.assertTrue("The thumbnail wasn't downloaded.", thumbnailFile.exists());
		Assert.assertTrue("The first page wasn't downloaded.", firstFile.exists());
		Assert.assertTrue("The second page wasn't downloaded.", secondFile.exists());

		// Clears the download folder used for unit testing.
		String folder = propertyService.getValue(PropertyKey.DOWNLOAD_DIRECTORY.getKey());
		File folderFile = new File(folder);
		folderFile.delete();
	}
}
