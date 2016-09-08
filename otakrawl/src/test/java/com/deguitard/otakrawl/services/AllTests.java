package com.deguitard.otakrawl.services;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.deguitard.otakrawl.builders.MangaBuilderTest;
import com.deguitard.otakrawl.services.crawler.AnimePlanetSuggestionCrawlerTest;
import com.deguitard.otakrawl.services.crawler.MangaHereCrawlerTest;
import com.deguitard.otakrawl.services.downloader.ChapterDownloaderImplTest;
import com.deguitard.otakrawl.services.persistence.DatabaseConnectionTest;
import com.deguitard.otakrawl.services.persistence.MangaDaoTest;
import com.deguitard.otakrawl.services.tools.PropertyServiceTest;

@RunWith(Suite.class)
@SuiteClasses({AnimePlanetSuggestionCrawlerTest.class, MangaHereCrawlerTest.class, ChapterDownloaderImplTest.class, DatabaseConnectionTest.class, MangaDaoTest.class, PropertyServiceTest.class, MangaBuilderTest.class})
public class AllTests {

}
