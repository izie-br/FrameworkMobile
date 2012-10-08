package com.quantium.mobile.framework.test;

import android.database.sqlite.SQLiteDatabase;

import com.quantium.mobile.framework.test.db.DB;
import com.quantium.mobile.framework.test.gen.AbstractSessionFacade;

public class ModelFacadeImpl extends AbstractSessionFacade{

	@Override
	public SQLiteDatabase getDb() {
		return DB.getDb();
	}

}
