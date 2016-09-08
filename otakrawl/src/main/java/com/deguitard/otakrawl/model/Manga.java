package com.deguitard.otakrawl.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.deguitard.otakrawl.services.crawler.provider.CrawlSource;

public class Manga extends Entity {

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
	private boolean ongoing;
	private CrawlSource source;
	private String mangaFoxId;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getAltTitles() {
		return altTitles;
	}

	public void setAltTitles(List<String> altTitles) {
		this.altTitles = altTitles;
	}

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	public List<String> getArtists() {
		return artists;
	}

	public void setArtists(List<String> artists) {
		this.artists = artists;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public List<String> getGenres() {
		return genres;
	}

	public void setGenres(List<String> genres) {
		this.genres = genres;
	}

	public Deque<Chapter> getChapters() {
		if (chapters == null) {
			chapters = new ArrayDeque<>();
		}
		return chapters;
	}

	public void setChapters(Deque<Chapter> chapters) {
		this.chapters = chapters;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<String> getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(List<String> suggestions) {
		this.suggestions = suggestions;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public CrawlSource getSource() {
		return source;
	}

	public void setSource(CrawlSource source) {
		this.source = source;
	}

	public String getMangaFoxId() {
		return mangaFoxId;
	}

	public void setMangaFoxId(String mangaFoxId) {
		this.mangaFoxId = mangaFoxId;
	}

	public boolean isOngoing() {
		return ongoing;
	}

	public void setIsOngoing(boolean ongoing) {
		this.ongoing = ongoing;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
