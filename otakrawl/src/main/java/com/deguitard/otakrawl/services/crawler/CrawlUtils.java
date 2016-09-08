package com.deguitard.otakrawl.services.crawler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.deguitard.otakrawl.model.Chapter;

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
	
	/**
	 * Creates a merge of the existing manga chapters and the crawled chapters.
	 * If the manga has no chapter, it will return the crowled chapters.
	 * If the manga has chapters, it will add any crawled chapter with a new name.
	 *
	 * @param dbChapters : chapters already in database.
	 * @param crawledChapters : crawled chapters.
	 * @return the merge of the two lists.
	 */
	public static Deque<Chapter> mergeChapterList(Deque<Chapter> dbChapters, Deque<Chapter> crawledChapters) {
		Deque<Chapter> mergedChapters = new ArrayDeque<>();
		if (dbChapters != null) {
			mergedChapters = dbChapters;
			for (Chapter crawledChapter : crawledChapters) {
				boolean found = false;
				for (Chapter dbChapter : dbChapters) {
					if (dbChapter.getNumber().equals(crawledChapter.getNumber())) {
						found = true;
						break;
					}
				}
				if (!found) {
					mergedChapters.add(crawledChapter);
				}
			}
		} else {
			mergedChapters = crawledChapters;
		}
		return mergedChapters;
	}
}
