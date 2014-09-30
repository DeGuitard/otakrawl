package com.deguitard.otakrawl.services.crawler;

import java.util.Collection;

import com.deguitard.otakrawl.model.Manga;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;

public interface SuggestionCrawler {

	Collection<Manga> findSuggestions(Manga manga) throws NoResultFoundException;
}
