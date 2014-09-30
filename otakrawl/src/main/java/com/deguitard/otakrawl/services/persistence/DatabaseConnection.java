package com.deguitard.otakrawl.services.persistence;

import com.mongodb.DB;

/**
 * This interface must be used whenever there are database manipulations.
 *
 * @author Vianney Dupoy de Guitard
 */
public interface DatabaseConnection {

	/**
	 * Initializes a connection to the database host, if necessary.
	 * Then returns the database connection.
	 *
	 * @return the database connection.
	 */
	DB getDb();
}