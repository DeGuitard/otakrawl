package com.deguitard.otakrawl.services.persistence;

import java.util.Iterator;

import org.bson.types.ObjectId;

import com.deguitard.otakrawl.model.Entity;
import com.deguitard.otakrawl.services.persistence.serialization.ObjectIdTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class DBTypedCursor<T extends Entity> implements Iterable<T>, Iterator<T> {

	private Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
	private Class<T> klass;
	private DBCursor cursor;

	public DBTypedCursor(Class<T> pKlass, DBCursor pCursor) {
		klass = pKlass;
		cursor = pCursor;
	}

	public Iterator<T> copy() {
		return new DBTypedCursor<>(klass, cursor);
	}

	@Override
	public Iterator<T> iterator() {
		return copy();
	}

	public T curr() {
		DBObject dbCurr = cursor.curr();
		return gson.fromJson(dbCurr.toString(), klass);
	}

	@Override
	public boolean hasNext() {
		return cursor.hasNext();
	}

	@Override
	public T next() {
		DBObject dbNext = cursor.next();
		return gson.fromJson(dbNext.toString(), klass);
	}

	public void close() {
		cursor.close();
	}

	@Override
	public void remove() {
		cursor.remove();
	}

}
