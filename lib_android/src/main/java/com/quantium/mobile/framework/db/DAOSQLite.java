package com.quantium.mobile.framework.db;

import android.database.Cursor;

import com.quantium.mobile.framework.DAO;

public interface DAOSQLite<T> extends DAO<T> {
	T cursorToObject(Cursor cursor, boolean useCache);
	String [] getColumns();
}
