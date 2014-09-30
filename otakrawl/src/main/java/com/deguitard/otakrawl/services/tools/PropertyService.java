package com.deguitard.otakrawl.services.tools;

import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;

/**
 * Reads and writes properties in a file.
 *
 * @author Vianney Dupoy de Guitard.
 */
public interface PropertyService {

	/**
	 * Reads the key in the property file and returns the value.
	 * @param key : the key to read.
	 * @return the value associated to the key.
	 * @throws NoResultFoundException when the key is not present in the property file.
	 */
	String getValue(String key) throws NoResultFoundException;

	/**
	 * Edits or adds a property to the file.
	 * @param key : the key of the property to update/add.
	 * @param value : the value to set.
	 */
	void setValue(String key, String value);
}
