package com.deguitard.otakrawl.services.tools;

/**
 * List of property keys.
 *
 * @author Vianney Dupoy de Guitard
 */
public enum PropertyKey {

	/** Key to the download directory. */
	DOWNLOAD_DIRECTORY("download.directory");

	/** The property key. */
	private String key;

	/** Private constructor. */
	private PropertyKey(String pKey) {
		key = pKey;
	}

	/** @return the key. */
	public String getKey() {
		return this.key;
	}
}
