package com.deguitard.otakrawl.services.persistence.dao;

import com.deguitard.otakrawl.model.Activity;
import com.deguitard.otakrawl.services.persistence.GenericDao;


public class ActivityDao extends GenericDao<Activity> {

	public ActivityDao() {
		super(Activity.class);
	}

	@Override
	protected String getCollectionName() {
		return "activity";
	}

}