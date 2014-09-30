package com.deguitard.otakrawl.services.persistence.serialization;

import java.io.IOException;

import org.bson.types.ObjectId;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ObjectIdTypeAdapter extends TypeAdapter<ObjectId> {

	@Override
	public void write(JsonWriter out, ObjectId value) throws IOException {
		if (value != null) {
			out.beginObject().name("$oid").value(value.toString()).endObject();
		} else {
			out.nullValue();
		}
	}

	@Override
	public ObjectId read(JsonReader in) throws IOException {
		in.beginObject();
		in.nextName();
		String objectId = in.nextString();
		in.endObject();
		return new ObjectId(objectId);
	}

}
