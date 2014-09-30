package com.deguitard.otakrawl.model;

import org.bson.types.ObjectId;

import com.google.gson.annotations.SerializedName;

public abstract class Entity {

	@SerializedName("_id")
	private ObjectId id;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public void generateIdIfNew() {
		if (id == null) {
			id = new ObjectId();
		}
	}
}
