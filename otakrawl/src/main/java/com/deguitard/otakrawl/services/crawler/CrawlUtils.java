package com.deguitard.otakrawl.services.crawler;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class meant to be staticly used.
 * Contains various methods useful when extracting content from webpages.
 *
 * @author Vianney Dupoy de Guitard
 */
public class CrawlUtils {

	/** Private constructor as this class is meant to be used with a static access. */
	private CrawlUtils() { }

	/**
	 * Take a set of elements, extract for each it text, and returns it as a collection.
	 * @param elements : the set of elements.
	 * @return the list with the extracted texts.
	 */
	public static List<String> getEltsText(Elements elements) {
		List<String> textList = new ArrayList<String>();
		for (Element element : elements) {
			textList.add(element.text());
		}
		return textList;
	}
}
