package com.deguitard.otakrawl.services;

import org.bson.types.ObjectId;

import com.deguitard.otakrawl.services.crawler.ICrawler;
import com.deguitard.otakrawl.services.crawler.mangafox.MangaFoxCrawler;
import com.deguitard.otakrawl.services.crawler.mangahere.MangaHereCrawler;
import com.deguitard.otakrawl.services.crawler.provider.MangaFox;
import com.deguitard.otakrawl.services.crawler.provider.MangaHere;
import com.deguitard.otakrawl.services.crawler.provider.StarKana;
import com.deguitard.otakrawl.services.crawler.starkana.StarKanaCrawler;
import com.deguitard.otakrawl.services.downloader.ChaptersDownloaderImpl;
import com.deguitard.otakrawl.services.downloader.IChaptersDownloader;
import com.deguitard.otakrawl.services.imprt.ImportService;
import com.deguitard.otakrawl.services.imprt.ImportServiceImpl;
import com.deguitard.otakrawl.services.persistence.DatabaseConnection;
import com.deguitard.otakrawl.services.persistence.DatabaseConnectionImpl;
import com.deguitard.otakrawl.services.persistence.dao.ActivityDao;
import com.deguitard.otakrawl.services.persistence.dao.MangaDao;
import com.deguitard.otakrawl.services.persistence.serialization.ObjectIdTypeAdapter;
import com.deguitard.otakrawl.services.tools.PropertyService;
import com.deguitard.otakrawl.services.tools.PropertyServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;

/**
 * Guice module. Binds the interfaces to the implementations to use.
 *
 * @author Vianney Dupoy de Guitard
 */
public class OtakrawlModule extends AbstractModule {

	/** {@inheritDoc} */
	@Override
	protected void configure() {
		// Basic services (database, properties, serialization).
		bind(PropertyService.class).to(PropertyServiceImpl.class);
		bind(DatabaseConnection.class).to(DatabaseConnectionImpl.class);

		// Import service;
		bind(ImportService.class).to(ImportServiceImpl.class);

		// Crawler services.
		Multibinder<ICrawler> crawlBinder = Multibinder.newSetBinder(binder(), ICrawler.class);
		// crawlBinder.addBinding().to(StarKanaCrawler.class);
		// crawlBinder.addBinding().to(MangaHereCrawler.class);
		// crawlBinder.addBinding().to(MangaFoxMangaScrapeCrawler.class);
		crawlBinder.addBinding().to(MangaFoxCrawler.class);
		bind(ICrawler.class).annotatedWith(StarKana.class).to(StarKanaCrawler.class);
		bind(ICrawler.class).annotatedWith(MangaHere.class).to(MangaHereCrawler.class);
		bind(ICrawler.class).annotatedWith(MangaFox.class).to(MangaFoxCrawler.class);

		// Download service
		bind(IChaptersDownloader.class).to(ChaptersDownloaderImpl.class);

		// Persistence DAOs.
		bind(MangaDao.class).toInstance(new MangaDao());
		bind(ActivityDao.class).toInstance(new ActivityDao());
	}

	/** Provides a GSON with a special type adapter for object ids. */
	@Provides
	Gson provideGson() {
		GsonBuilder builder = new GsonBuilder();
		ObjectIdTypeAdapter ObjectIdAdapter = new ObjectIdTypeAdapter();
		builder.registerTypeAdapter(ObjectId.class, ObjectIdAdapter);
		return builder.create();
	}
}
