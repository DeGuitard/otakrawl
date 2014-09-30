package com.deguitard.otakrawl.model.builders;

import java.util.ArrayList;
import java.util.List;

import com.deguitard.otakrawl.model.Chapter;

public class ChapterBuilder {

	private String number;
	private String url;
	private List<String> imagesUrls;

	public ChapterBuilder() {
		imagesUrls = new ArrayList<String>();
	}

	public ChapterBuilder number(String number) {
		this.number = number;
		return this;
	}

	public ChapterBuilder url(String url) {
		this.url = url;
		return this;
	}

	public ChapterBuilder imagesUrls(List<String> imagesUrls) {
		this.imagesUrls = imagesUrls;
		return this;
	}

	public Chapter createChapter() {
		Chapter chapter = new Chapter();
		chapter.setNumber(number);
		chapter.setUrl(url);
		chapter.setImagesUrls(imagesUrls);
		return chapter;
	}
}
