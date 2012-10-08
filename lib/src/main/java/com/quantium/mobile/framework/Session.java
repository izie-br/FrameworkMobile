package com.quantium.mobile.framework;

import android.database.sqlite.SQLiteDatabase;

public interface Session {
	SQLiteDatabase getDb();
	DAOFactory getDAOFactory();
}
