package com.deguitard.otakrawl.services.persistence.dao;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.services.persistence.GenericDao;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;


public class MangaDao extends GenericDao<Manga> {

	public MangaDao() {
		super(Manga.class);
	}

	public Deque<Manga> findByTitles(Iterable<String> titles) {
		return findByField(titles, "title");
	}

	public Deque<Manga> findByTitles(Collection<Manga> mangaList) {
		// Gets all the titles.
		Deque<String> titles = new ArrayDeque<>();
		for (Manga manga : mangaList) {
			titles.add(manga.getTitle());
		}

		return findByField(titles, "title");
	}

	public Deque<Manga> findByUrl(Collection<Manga> mangaList) {
		// Gets all the urls.
		Deque<String> urls = new ArrayDeque<>();
		for (Manga manga : mangaList) {
			urls.add(manga.getUrl());
		}

		return findByField(urls, "url");
	}

	private Deque<Manga> findByField(Iterable<String> values, String field) {
		// Searches & stores the mangas.
		Deque<Manga> result = new ArrayDeque<>();
		DBCursor cursor = getCollection().find(new BasicDBObject(field, new BasicDBObject("$in", values)));
		for (DBObject dbObj : cursor) {
			result.add(getGson().fromJson(dbObj.toString(), Manga.class));
		}
		return result;
	}

	@Override
	protected String getCollectionName() {
		return "mangas";
	}

}
