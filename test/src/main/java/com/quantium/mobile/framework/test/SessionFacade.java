package com.quantium.mobile.framework.test;

import android.database.sqlite.SQLiteDatabase;

import com.quantium.mobile.framework.AndroidPrimaryKeyProvider;
import com.quantium.mobile.framework.AndroidToSyncProvider;
import com.quantium.mobile.framework.BaseModelFacade;
import com.quantium.mobile.framework.DAOFactory;
import com.quantium.mobile.framework.PrimaryKeyProvider;
import com.quantium.mobile.framework.Session;
import com.quantium.mobile.framework.ToSyncProvider;
import com.quantium.mobile.framework.test.db.DB;
import com.quantium.mobile.framework.test.gen.SQLiteDAOFactory;

public class SessionFacade extends BaseModelFacade {

	public SessionFacade() {
		super(new SQLiteDAOFactory() {
			
			@Override
			public SQLiteDatabase getDb() {
				return DB.getDb();
			}
		}, new AndroidPrimaryKeyProvider(){
			
			@Override
			public SQLiteDatabase getDb() {
				return DB.getDb();
			}
		}, new AndroidToSyncProvider(){
			
			@Override
			public SQLiteDatabase getDb() {
				return DB.getDb();
			}
		});
	}

	@Override
	protected String getLoggedUserId() {
		return null;
	}

}
