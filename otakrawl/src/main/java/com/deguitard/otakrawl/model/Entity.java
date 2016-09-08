package com.deguitard.otakrawl.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.google.gson.annotations.SerializedName;

public abstract class Entity {

	@SerializedName("_id")
	private ObjectId id;
	private Date createdAt;
	private Date updatedAt;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void generateIdIfNew() {
		if (id == null) {
			id = new ObjectId();
			createdAt = new Date();
		}
	}
}
