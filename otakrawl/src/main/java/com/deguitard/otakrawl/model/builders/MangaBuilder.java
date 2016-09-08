package com.deguitard.otakrawl.model.builders;

import java.util.Deque;
import java.util.List;

import com.deguitard.otakrawl.model.Chapter;
import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.services.crawler.provider.CrawlSource;

/**
 * Used to create a Manga easily.
 * Works this way :
 * <pre>
 * {@code
 * Manga manga = new MangaBuilder().title("Skip Beat").year(2002).createManga();
 * }
 * </pre>
 * @author Vianney Dupoy de Guitard
 *
 */
public class MangaBuilder {

	private String title;
	private List<String> altTitles;
	private List<String> authors;
	private List<String> artists;
	private Integer year;
	private String summary;
	private List<String> genres;
	private Deque<Chapter> chapters;
	private String url;
	private List<String> suggestions;
	private String thumbnail;
	private CrawlSource source;
	private String mangaFoxId;
	private boolean ongoing;

	public MangaBuilder() { }

	public MangaBuilder(Manga base) {
		title = base.getTitle();
		altTitles = base.getAltTitles();
		authors = base.getAuthors();
		artists = base.getArtists();
		year = base.getYear();
		summary = base.getSummary();
		genres = base.getGenres();
		chapters = base.getChapters();
		url = base.getUrl();
		suggestions = base.getSuggestions();
		thumbnail = base.getThumbnail();
		source = base.getSource();
		mangaFoxId = base.getMangaFoxId();
		ongoing = base.isOngoing();
	}

	public MangaBuilder title(String title) {
		this.title = title;
		return this;
	}

	public MangaBuilder altTitles(List<String> altTitles) {
		this.altTitles = altTitles;
		return this;
	}

	public MangaBuilder authors(List<String> authors) {
		this.authors = authors;
		return this;
	}

	public MangaBuilder artists(List<String> artists) {
		this.artists = artists;
		return this;
	}

	public MangaBuilder year(Integer year) {
		this.year = year;
		return this;
	}

	public MangaBuilder summary(String summary) {
		this.summary = summary;
		return this;
	}

	public MangaBuilder genres(List<String> genres) {
		this.genres = genres;
		return this;
	}

	public MangaBuilder chapters(Deque<Chapter> chapters) {
		this.chapters = chapters;
		return this;
	}

	public MangaBuilder url(String url) {
		this.url = url;
		return this;
	}

	public MangaBuilder suggestions(List<String> suggestions) {
		this.suggestions = suggestions;
		return this;
	}

	public MangaBuilder thumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
		return this;
	}

	public MangaBuilder source(CrawlSource source) {
		this.source = source;
		return this;
	}

	public MangaBuilder mangaFoxId(String mangaFoxId) {
		this.mangaFoxId = mangaFoxId;
		return this;
	}

	public MangaBuilder status(boolean ongoing) {
		this.ongoing = ongoing;
		return this;
	}

	/**
	 * Once all attributes have been set, one can call this method to return
	 * a fully initialized manga.
	 * @return the initialized manga.
	 */
	public Manga createManga() {
		Manga manga = new Manga();
		manga.setTitle(title);
		manga.setAltTitles(altTitles);
		manga.setArtists(artists);
		manga.setAuthors(authors);
		manga.setYear(year);
		manga.setSummary(summary);
		manga.setGenres(genres);
		manga.setChapters(chapters);
		manga.setUrl(url);
		manga.setSuggestions(suggestions);
		manga.setThumbnail(thumbnail);
		manga.setSource(source);
		manga.setMangaFoxId(mangaFoxId);
		manga.setIsOngoing(ongoing);

		return manga;
	}
}
