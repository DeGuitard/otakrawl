package com.deguitard.otakrawl.services.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.deguitard.otakrawl.OtakrawlTestRunner;
import com.deguitard.otakrawl.OtakrawlTestRunner.GuiceModules;
import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.model.builders.MangaBuilder;
import com.deguitard.otakrawl.services.OtakrawlModule;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;
import com.deguitard.otakrawl.services.persistence.dao.MangaDao;
import com.google.inject.Inject;

/**
 * Tests the Generic DAO implementation, using Manga.
 * Checks the CRUD operations and the bulk operations.
 *
 * @author Vianney Dupoy de Guitard
 */
@RunWith(OtakrawlTestRunner.class)
@GuiceModules(OtakrawlModule.class)
public class MangaDaoTest {

	/** DAO to test. */
	@Inject
	private MangaDao dao;

	/**
	 * Tests basic CRUD operations.
	 */
	@Test
	public void crudTest() {
		// Create
		Manga manga = new MangaBuilder().title("Unit test").createManga();
		dao.saveOrUpdate(manga);
		Assert.assertNotNull("Manga was not saved.", manga.getId());

		// Read
		try {
			Manga mangaFound = dao.findOne(manga);
			Assert.assertNotNull("Manga returned is null.", mangaFound);
		} catch (NoResultFoundException e) {
			Assert.fail("Wasn't able to find the manga to read.");
		}

		// Update
		try {
			manga.setYear(2014);
			dao.saveOrUpdate(manga);
			Manga mangaFound = dao.findOne(manga);
			Assert.assertEquals("Manga wasn't updated.", manga.getYear(), mangaFound.getYear());
		} catch (NoResultFoundException e) {
			Assert.fail("Updated manga wasn't found.");
		}

		// Delete
		try {
			dao.delete(manga);
			dao.findOne(manga);
			Assert.fail("Manga wasn't removed.");
		} catch (NoResultFoundException e) { }
	}

	/**
	 * Tests bulk operations : create, read, update & delete.
	 */
	@Test
	public void bulkTest() {
		int entitiesToInsert = 2000;
		int initialCount = dao.countAll();

		// Bulk insert.
		List<Manga> mangas = new ArrayList<Manga>();
		for (int i = 0; i < entitiesToInsert; i++) {
			Manga manga = new MangaBuilder().title("Unit Test " + i).year(2014).createManga();
			mangas.add(manga);
		}
		dao.saveOrUpdate(mangas);
		int endCount = dao.countAll();
		Assert.assertEquals("The bulk insert has failed.", initialCount + entitiesToInsert, endCount);

		// Bulk update.
		for (Manga manga : mangas) {
			manga.setYear(2007);
		}
		dao.saveOrUpdate(mangas);
		endCount = dao.countAll();
		Assert.assertEquals("The bulk insert has failed.", initialCount + entitiesToInsert, endCount);

		try {
			Manga manga = dao.findOne(mangas.get(0));
			Assert.assertNotNull("The found manga was null.", manga);
			Assert.assertEquals("The year wasn't updated.", new Integer(2007), manga.getYear());
		} catch (NoResultFoundException e) {
			Assert.fail("One inserted manga was not found.");
		}

		// Bulk read
		int readCount = 0;
		for (Manga manga : dao.findAll()) {
			manga.getId();
			readCount++;
		}
		Assert.assertEquals(initialCount + entitiesToInsert, readCount);

		// Bulk delete.
		dao.delete(mangas);
		Assert.assertEquals("The mangas weren't removed.", initialCount, dao.countAll());
	}

	@Test
	public void findByTitlesTest() {
		Manga testA = new MangaBuilder().title("Unit Test A").createManga();
		Manga testB = new MangaBuilder().title("Unit Test B").createManga();
		List<Manga> mangaList = Arrays.asList(testA, testB);

		dao.saveOrUpdate(mangaList);
		Deque<Manga> result = dao.findByTitles(mangaList);
		Assert.assertEquals("At least one manga was not saved or fetched.", mangaList.size(), result.size());
		Assert.assertTrue("The saved mangas were not fetched.", result.containsAll(mangaList));

		dao.delete(mangaList);
	}
}
