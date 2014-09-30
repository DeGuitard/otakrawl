package com.deguitard.otakrawl.services.persistence;

import java.net.UnknownHostException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;
import com.deguitard.otakrawl.services.tools.PropertyService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * Implementation of {@link DatabaseConnection}, with MangoDB.
 *
 * @author Vianney Dupoy de Guitard
 *
 */
@Singleton
public class DatabaseConnectionImpl implements DatabaseConnection {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectionImpl.class);

	/** Key of the host property. */
	private static final String HOST_KEY = "database.host";
	/** Key of the user property. */
	private static final String USER_KEY = "database.user";
	/** Key of the password property. */
	private static final String PWD_KEY = "database.pwd";
	/** Key of the database name property. */
	private static final String DB_NAME_KEY = "database.name";

	/** Service used to read the properties. */
	@Inject
	private PropertyService propertyService;

	/** Database connection. */
	private DB db;

	/**
	 * Initialize a database connection.
	 * Should be ran only once.
	 */
	public void initialize() {
		LOGGER.info("Initializing database connection");
		try {
			String host = propertyService.getValue(HOST_KEY);
			String user = propertyService.getValue(USER_KEY);
			String pwd = propertyService.getValue(PWD_KEY);
			String dbName = propertyService.getValue(DB_NAME_KEY);

			MongoCredential credential = MongoCredential.createMongoCRCredential(user, dbName, pwd.toCharArray());
			MongoClient client = new MongoClient(new ServerAddress(host), Arrays.asList(credential));
			db = client.getDB(dbName);
		} catch (UnknownHostException e) {
			LOGGER.error("Could not initialize connection.");
			throw new IllegalStateException("Could not initialize the database connection.");
		} catch (NoResultFoundException e) {
			LOGGER.error("Could not initialize connection: error while reading the properties file.");
			throw new IllegalStateException("Could not initialize the database connection.");
		}
	}

	/** {@inheritDoc} */
	public DB getDb() {
		if (db == null) {
			initialize();
		}
		return db;
	}

}
