package com.deguitard.otakrawl.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bson.types.ObjectId;

public class Activity extends Entity {

	private ObjectId manga;
	private String chapterNumber;
	private ActivityType type;

	public ObjectId getManga() {
		return manga;
	}

	public void setManga(ObjectId manga) {
		this.manga = manga;
	}

	public String getChapterNumber() {
		return chapterNumber;
	}

	public void setChapterNumber(String chapterNumber) {
		this.chapterNumber = chapterNumber;
	}

	public ActivityType getType() {
		return type;
	}

	public void setType(ActivityType type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public enum ActivityType {
		NEW_CHAPTER,
		NEW_MANGA
	}
}
