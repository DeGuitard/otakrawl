package com.deguitard.otakrawl.services.crawler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Formatter for StarKana URLs.
 * Can find URL base on some information, such as a manga title.
 *
 * @author Vianney Dupoy de Guitard
 */
public class StarKanaUrlFormatter {

	/** Applicative logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(StarKanaCrawler.class);

	/** URL of the website. */
	private static final String WEBSITE_URL = "http://starkana.com/";

	/** Base URL of the website. */
	private static final String BASE_URL = WEBSITE_URL + "manga/";

	/** Characters to replace by two underscores in URLs. */
	private static final String REPLACE_TWO_UNDERSCORES = "èЯä³ß°Ω";

	/** Characters to replace by three underscores in URLs. */
	private static final String REPLACE_THREE_UNDERSCORES = "♀♥－スナイプ←草薙の剣";

	/** Regex to replace special characters by one underscore. */
	private static final String REPLACE_ONE_UNDERSCORE_REGX = "[^\\d\\w\\-:~().$'!\"," + REPLACE_TWO_UNDERSCORES + REPLACE_THREE_UNDERSCORES + "]";

	/** Regex to replace special characters by two underscores. */
	private static final String REPLACE_TWO_UNDERSCORE_REGX = "[" + REPLACE_TWO_UNDERSCORES + "]";

	/** Regex to replace special characters by three underscores. */
	private static final String REPLACE_THREE_UNDERSCORE_REGX = "[" + REPLACE_THREE_UNDERSCORES + "]";

	/** URL cache of manga URLs. */
	private static Map<String, String> urlCache = new HashMap<>();

	/**
	 * Returns the URL for a manga, based on its title.
	 * @param title : the title of the manga to look for.
	 * @return the URL of the manga page on starkana.
	 */
	public static String getMangaUrl(String title) {
		String url = urlCache.get(title);
		if (url != null) {
			return urlCache.get(title);
		}

		// Grabs the first letter.
		String firstLetter = title.substring(0, 1).toUpperCase();
		if (!firstLetter.matches("[A-Z]")) {
			firstLetter = "0";
		}

		// Replaces the special characters the same way starkana does.
		String urlTitle = title.replaceAll(REPLACE_ONE_UNDERSCORE_REGX, "_");
		urlTitle = urlTitle.replaceAll(REPLACE_TWO_UNDERSCORE_REGX, "__");
		urlTitle = urlTitle.replaceAll(REPLACE_THREE_UNDERSCORE_REGX, "___");
		urlTitle = urlTitle.replaceAll("['\"]", "");
		while (urlTitle.matches("[+$_].*")) {
			urlTitle = urlTitle.substring(1);
		}
		while (urlTitle.matches(".*[+_]")) {
			urlTitle = urlTitle.substring(0, urlTitle.length() - 1);
		}
		try {
			urlTitle = URLEncoder.encode(urlTitle, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Failed to encode title.", e);
		}

		url = BASE_URL + firstLetter + "/" + urlTitle;
		urlCache.put(title, url);
		return url;
	}
}
