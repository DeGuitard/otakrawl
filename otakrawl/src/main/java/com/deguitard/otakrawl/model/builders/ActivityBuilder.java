package com.deguitard.otakrawl.model.builders;

import org.bson.types.ObjectId;

import com.deguitard.otakrawl.model.Activity;
import com.deguitard.otakrawl.model.Activity.ActivityType;
import com.deguitard.otakrawl.model.Manga;

public class ActivityBuilder {

	private ObjectId manga;
	private String chapterNumber;
	private ActivityType type;

	public ActivityBuilder manga(ObjectId manga) {
		this.manga = manga;
		return this;
	}

	public ActivityBuilder manga(Manga mangaToUpdate) {
		this.manga = mangaToUpdate.getId();
		return this;
	}

	public ActivityBuilder chapterNumber(String chapterNumber) {
		this.chapterNumber = chapterNumber;
		return this;
	}

	public ActivityBuilder type(ActivityType type) {
		this.type = type;
		return this;
	}

	public Activity createActivity() {
		Activity activity = new Activity();
		activity.setManga(manga);
		activity.setChapterNumber(chapterNumber);
		activity.setType(type);

		return activity;
	}
}
