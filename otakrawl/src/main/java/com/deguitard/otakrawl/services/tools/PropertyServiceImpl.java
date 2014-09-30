package com.deguitard.otakrawl.services.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;
import com.google.inject.Singleton;

/**
 * Implementation of {@link PropertyService}.
 * Reads the properties from the file which is common to the application.
 * It permits to read / write properties.
 *
 * @author Vianney Dupoy de Guitard
 */
@Singleton
public class PropertyServiceImpl implements PropertyService {

	private static final String FILENAME = "otakrawl.properties";
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyServiceImpl.class);
	private Properties properties = new Properties();

	public PropertyServiceImpl() throws IOException {
		LOGGER.debug("Initialization of Properties Service with '{}'.", FILENAME);
		InputStream is = getClass().getClassLoader().getResourceAsStream(FILENAME);
		if (is == null) {
			LOGGER.error("Could not find properties file.");
			throw new IOException("Properties file could not be found!");
		}
		properties.load(is);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValue(String key) throws NoResultFoundException {
		LOGGER.debug("Reading property '{}'.", key);
		String value = properties.getProperty(key);
		if (value == null) {
			LOGGER.error("Property could not be found!");
			throw new NoResultFoundException();
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String key, String value) {
		LOGGER.debug("Updating property '{}' with value '{}'.", key, value);
		properties.setProperty(key, value);
	}

}
