package com.deguitard.otakrawl;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.deguitard.otakrawl.services.OtakrawlModule;
import com.deguitard.otakrawl.services.imprt.ImportService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mashape.unirest.http.Unirest;

/**
 * <p>Entry point of Otakrawl.</p>
 *
 * @author Vianney Dupoy de Guitard
 */
public class Otakrawl 
{

	/** The "Usage" documentation given if the parameters are incorrect. */
	private static final String USAGE = "Usage: java -jar otakrawl.jar <full|update|suggestions>";

	/** Guice injector. */
	private static Injector injector;

	/**
	 * Main method, starts the import or displays an error.
	 * @param args : the first argument must be the import type to do.
	 * @throws IOException : in case the REST connection could not end properly.
	 */
	public static void main(final String[] args) throws IOException
	{
		ImportType importType = getImportType(args);
		initHttpClient();
		startImport(importType);
	}

	/**
	 * <p>Find the import type from the arguments specified to the jar.</p>
	 * <p>If no parameter was supplied, an exception is thrown. It goes the same if the import type is unknown.</p>
	 *
	 * @param args : the arguments given at the start of the application.
	 * @return the kind of import to do.
	 */
	private static ImportType getImportType(final String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException(USAGE);
		}
		String importTypeStr = args[0];
		ImportType importType;
		try {
			importType = ImportType.valueOf(importTypeStr.toUpperCase());
		} catch (NoSuchElementException e) {
			throw new IllegalArgumentException(USAGE);
		}
		return importType;
	}

	/**
	 * <p>Starts an import, based on the type.</p>
	 * <p>Refer to {@link ImportType} to learn about the different kinds of imports.</p>
	 *
	 * @param importType : the kind of import to do.
	 */
	private static void startImport(final ImportType importType) {
		ImportService importService = getInjector().getInstance(ImportService.class);
		switch (importType) {
		case FULL:
			importService.fullImport();
			break;
		case MERGE:
			importService.merge();
			break;
		case UPDATE:
			importService.updateImport();
			break;
		case SUGGESTIONS:
			importService.updateSuggestions();
			break;
		}
	}

	/**
	 * Returns the guice injector.
	 * @return the guice injector.
	 */
	public static Injector getInjector() {
		if (injector == null) {
			injector = Guice.createInjector(new OtakrawlModule());
		}
		return injector;
	}

	/**
	 * Initializes a thread safe HTTP Client.
	 */
	private static void initHttpClient() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(100);
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
		Unirest.setHttpClient(httpClient);;
	}
}
