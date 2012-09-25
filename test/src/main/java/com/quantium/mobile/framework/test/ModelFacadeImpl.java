package com.quantium.mobile.framework.test;

import android.database.sqlite.SQLiteDatabase;

import com.quantium.mobile.framework.test.db.DB;
import com.quantium.mobile.framework.test.gen.ModelFacade;

public class ModelFacadeImpl extends ModelFacade{

	@Override
	public SQLiteDatabase getDb() {
		return DB.getDb();
	}

}
