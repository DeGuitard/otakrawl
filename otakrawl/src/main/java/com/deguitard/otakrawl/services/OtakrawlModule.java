package com.deguitard.otakrawl.services;

import org.bson.types.ObjectId;

import com.deguitard.otakrawl.services.imprt.ImportService;
import com.deguitard.otakrawl.services.imprt.ImportServiceImpl;
import com.deguitard.otakrawl.services.persistence.DatabaseConnection;
import com.deguitard.otakrawl.services.persistence.DatabaseConnectionImpl;
import com.deguitard.otakrawl.services.persistence.dao.MangaDao;
import com.deguitard.otakrawl.services.persistence.serialization.ObjectIdTypeAdapter;
import com.deguitard.otakrawl.services.tools.PropertyService;
import com.deguitard.otakrawl.services.tools.PropertyServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;

/**
 * Guice module. Binds the interfaces to the implementations to use.
 *
 * @author Vianney Dupoy de Guitard
 */
public class OtakrawlModule extends AbstractModule {

	/** {@inheritDoc} */
	@Override
	protected void configure() {
		bind(PropertyService.class).to(PropertyServiceImpl.class);
		bind(DatabaseConnection.class).to(DatabaseConnectionImpl.class);
		bind(ImportService.class).to(ImportServiceImpl.class);
		bind(Gson.class).toInstance(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create());
		bind(MangaDao.class).toInstance(new MangaDao());
	}

}
