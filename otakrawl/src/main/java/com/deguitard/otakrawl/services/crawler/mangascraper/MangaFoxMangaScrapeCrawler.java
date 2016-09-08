package com.deguitard.otakrawl.services.crawler.mangascraper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deguitard.otakrawl.model.Chapter;
import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.model.builders.ChapterBuilder;
import com.deguitard.otakrawl.model.builders.MangaBuilder;
import com.deguitard.otakrawl.services.crawler.ICrawler;
import com.deguitard.otakrawl.services.crawler.provider.CrawlSource;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;
import com.deguitard.otakrawl.services.tools.PropertyService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Crawler using MangaScrape API to crawl MangaFox.
 *
 * @author Vianney Dupoy de Guitard
 */
public class MangaFoxMangaScrapeCrawler implements ICrawler {

	/** Applicative logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MangaFoxMangaScrapeCrawler.class);

	/** Max attemps the crawler must issue for a request. */
	private static final int MAX_TRIES = 5;

	/** API Key. */
	private static String apiKey = "DWhBZvuSUFmshwVyVXtfi2mMqvmup14oHXDjsnTONlfBblT4kM";

	/** URL of the REST API. */
	private static final String API_URL = "https://doodle-manga-scraper.p.mashape.com/mangafox.me/";

	/** The service used to get the properties - and thus, the api key. */
	@Inject private PropertyService propertyService;

	/** {@inheritDoc} */
	@Override
	public Deque<Manga> crawlMangaList() {
		Deque<Manga> mangaList = new ArrayDeque<>(14000);
		LOGGER.info("Crawling MangaFox manga list.");

		try {
			// Call the REST API.
			HttpResponse<JsonNode> response = restCall();
			JSONArray list = response.getBody().getArray();

			// Creates a Manga object for each element of the JSON Object list.
			for (int i = 0; i < 200; i++) {
				JSONObject manga = list.getJSONObject(i);
				String title = manga.getString("name");
				String id = manga.getString("mangaId");
				mangaList.add(new MangaBuilder().title(title).mangaFoxId(id).source(CrawlSource.MANGA_FOX).createManga());
			}

			LOGGER.info("{} mangas found.", list.length());
		} catch (UnirestException e) {
			LOGGER.error("An exception occured while collecting MangaFox manga list.", e);
		}
		return mangaList;
	}

	/** {@inheritDoc} */
	@Override
	public Manga crawlManga(Manga manga) throws NoResultFoundException {
		if (StringUtils.isBlank(manga.getMangaFoxId())) {
			throw new IllegalArgumentException("The mangafox ID was not set.");
		}

		LOGGER.debug("Crawling data of manga named '{}'.", manga.getTitle());
		try {
			// Call to the REST API
			HttpResponse<String> response = restCall("manga/" + manga.getMangaFoxId());
			JsonObject json = new JsonParser().parse(response.getBody()).getAsJsonObject();

			// Sometimes the api response is just an empty object.
			if (json.isJsonNull() || !json.isJsonObject()) {
				LOGGER.error("The received data is inconsistent.");
				throw new NoResultFoundException();
			}

			// Gets the info from the JSON.
			Integer year = json.has("yearOfRelease") ? json.get("yearOfRelease").getAsInt() : null;
			String url = json.has("href") ? json.get("href").getAsString() : null;
			String status = json.has("status")  ? json.get("status").getAsString() : null;
			String summary = json.has("info")  ? json.get("info").getAsString() : null;
			String thumbnail = json.has("cover")  ? json.get("cover").getAsString() : null;
			List<String> authors = toList(json.get("author").getAsJsonArray());
			List<String> artists = toList(json.get("artist").getAsJsonArray());
			List<String> genres = toList(json.get("genres").getAsJsonArray());
			Deque<Chapter> chapters = toChapters(json.get("chapters").getAsJsonArray());

			// Formats the data.
			authors = formatNames(authors);
			artists = formatNames(artists);
			genres = formatNames(genres);

			// Fills the manga.
			manga.setYear(year);
			manga.setUrl(url);
			manga.setSummary(summary);
			manga.setThumbnail(thumbnail);
			manga.setAuthors(authors);
			manga.setArtists(artists);
			manga.setGenres(genres);
			manga.setChapters(chapters);
			manga.setIsOngoing("ongoing".equals(status));
		} catch (Exception e) {
			LOGGER.error("An exception occured while collecting data about '{}'.", manga.getTitle(), e);
			throw new NoResultFoundException();
		}
		return manga;
	}

	/** {@inheritDoc} */
	@Override
	public Chapter crawlChapter(Manga manga, Chapter chapter) throws NoResultFoundException {
		int tries = 0;
		boolean done = false;
		String jsonStr = null;
		while (!done) {
			try {
				// Call to the REST API
				HttpResponse<String> response = restCall("manga/" + manga.getMangaFoxId() + "/" + chapter.getNumber());
				jsonStr = response.getBody();
				JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();

				// Sometimes the api response is just an empty object.
				if (json.isJsonNull()) {
					LOGGER.error("The received data for chapter no. {} from {} is inconsistent : json was null / without attributes.", chapter.getNumber(), manga.getMangaFoxId());
					LOGGER.error("JSON Content: {}", json);
					throw new NoResultFoundException();
				}

				// Some attributes are sometimes missing. Let's check them.
				if (!json.has("pages")) {
					LOGGER.error("The received data is inconsistent : json miss the 'pages' attributes.");
					LOGGER.error("JSON Content: {}", json);
					throw new NoResultFoundException();
				}

				// Gets the info from the JSON.
				JsonArray jsonArray = json.get("pages").getAsJsonArray();
				List<String> imagesUrls = new ArrayList<>();
				for (int i = 0; i < jsonArray.size(); i++) {
					JsonElement element = jsonArray.get(i);
					if (element.isJsonObject()) {
						JsonObject page = element.getAsJsonObject();
						imagesUrls.add(page.get("url").getAsString());
					}
				}

				// Fills the chapter.
				chapter.setImagesUrls(imagesUrls);
				chapter.updatePageCount();

				// Ends the tries.
				done = true;
			} catch (UnirestException e) {
				LOGGER.error("An exception occured while fetching chapter no. {} from {}.", chapter.getNumber(), manga.getTitle(), e);
				throw new NoResultFoundException();
			} catch (Exception e) {
				tries++;
				LOGGER.warn("Error while parsing chapter no. {} from {}, attempt no. {}", chapter.getNumber(), manga.getTitle(), tries);
				if (tries == MAX_TRIES) {
					LOGGER.error("JSON SRC: {}", jsonStr);
					LOGGER.error("An exception occured while parsing chapter no. {} from {}.", chapter.getNumber(), manga.getTitle(), e);
					throw new NoResultFoundException();
				} else {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						LOGGER.error("Couldn't wait before re-trying the chapter no. {} from {} crawl", chapter.getNumber(), manga.getTitle(), e1);
					}
				}
			}
		}
		return chapter;
	}

	/** {@inheritDoc} */
	@Override
	public Deque<Manga> getUpdatedMangaList() {
		Deque<Manga> mangaList = crawlMangaList();
		Deque<Manga> recentMangas = new ArrayDeque<>();

		int fourHours = 3600 * 4;
		long fourHoursAgo = new Date().getTime() - fourHours;
		for (Manga manga : mangaList) {
			try {
				crawlManga(manga);
				if (manga.getUpdatedAt().getTime() > fourHoursAgo) {
					recentMangas.add(manga);
				}
			} catch (NoResultFoundException e) { }
		}
		return recentMangas;
	}

	/**
	 * Format a list of names.
	 * The names returned by the API have this format : "xxxx-yyyy".
	 * This method will convert those names to : "Xxxx Yyyy".
	 * @param names
	 * @return the formatted names.
	 */
	private List<String> formatNames(List<String> names) {
		List<String> newNames = new ArrayList<>();
		for (String name : names) {
			name = name.replace("-", " ");
			name = WordUtils.capitalizeFully(name);
			newNames.add(name);
		}
		return newNames;
	}

	/**
	 * Converts the JSON Array of chapters in POJOs.
	 * @param jsonArray : the array to convert.
	 * @return the chapter POJOs.
	 */
	private Deque<Chapter> toChapters(JsonArray jsonArray) {
		Deque<Chapter> chapters = new ArrayDeque<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonElement element = jsonArray.get(i);
			if (!element.isJsonObject()) continue;

			JsonObject chapter = element.getAsJsonObject();
			if (!chapter.has("chapterId")) continue;

			String id = String.valueOf(chapter.get("chapterId"));
			String name = "";
			if (chapter.has("name")) {
				name = chapter.get("name").getAsString();
			}
			chapters.add(new ChapterBuilder().number(id).name(name).createChapter());
		}
		return chapters;
	}

	/**
	 * Converts a JSON Array into a String List.
	 * @param jsonArray : the json array to convert.
	 * @return the converted json array.
	 */
	private List<String> toList(JsonArray jsonArray) {
		List<String> list = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonElement element = jsonArray.get(i);
			if (element.isJsonPrimitive()) list.add(element.getAsString());
		}
		return list;
	}

	/**
	 * Returns the API Key, initializing it if needed.
	 * @return the api key.
	 */
	private synchronized String getApiKey() {
		if (apiKey == null) {
			try {
				apiKey = propertyService.getValue("mangascrape.api.key");
			} catch (NoResultFoundException e) {
				LOGGER.error("Could not fetch API key from properties.");
			}
		}
		return apiKey;
	}

	/**
	 * Call the rest API.
	 * @return the API response.
	 * @throws UnirestException : if anything goes bad, it will throw this exception.
	 */
	private HttpResponse<JsonNode> restCall() throws UnirestException {
		return Unirest.get(API_URL).header("X-Mashape-Key", getApiKey()).header("Accept", "text/plain").asJson();
	}

	/**
	 * Call the rest API.
	 * @param urlSuffix : the suffix to add to the URL.
	 * @return the API response.
	 * @throws UnirestException : if anything goes bad, it will throw this exception.
	 */
	private HttpResponse<String> restCall(String urlSuffix) throws UnirestException {
		return Unirest.get(API_URL + urlSuffix).header("X-Mashape-Key", getApiKey()).header("Accept", "text/plain").asString();
	}

}
