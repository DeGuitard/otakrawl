package com.deguitard.otakrawl;

import java.util.NoSuchElementException;

import com.deguitard.otakrawl.services.OtakrawlModule;
import com.deguitard.otakrawl.services.imprt.ImportService;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * <p>Entry point of Otakrawl.</p>
 *
 * @author Vianney Dupoy de Guitard
 */
public class Otakrawl 
{

	/** The "Usage" documentation given if the parameters are incorrect. */
	private static final String USAGE = "Usage: java -jar otakrawl.jar <full|update|suggestions>";

	/**
	 * Main method, starts the import or displays an error.
	 * @param args : the first argument must be the import type to do.
	 */
	public static void main(final String[] args)
	{
		ImportType importType = getImportType(args);
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
		Injector injector = Guice.createInjector(new OtakrawlModule());
		ImportService importService = injector.getInstance(ImportService.class);
		switch (importType) {
		case FULL:
			importService.fullImport();
			break;
		case UPDATE:
			importService.updateImport();
			break;
		case SUGGESTIONS:
			importService.updateSuggestions();
			break;
		}
	}
}
