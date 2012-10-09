package com.quantium.mobile.framework.test;

import android.database.sqlite.SQLiteDatabase;

import com.quantium.mobile.framework.DAOFactory;
import com.quantium.mobile.framework.Session;
import com.quantium.mobile.framework.test.db.DB;
import com.quantium.mobile.framework.test.gen.SQLiteDAOFactory;

public class SessionFacade implements Session{

	DAOFactory daoFactory = new SQLiteDAOFactory() {
		
		@Override
		public SQLiteDatabase getDb() {
			return DB.getDb();
		}
	};

	@Override
	public DAOFactory getDAOFactory() {
		return daoFactory;
	}

}
