package com.quantium.mobile.framework.jdbc;

import java.sql.ResultSet;

import com.quantium.mobile.framework.DAO;

public interface JdbcDao<T> extends DAO<T> {
	T cursorToObject(ResultSet cursor, boolean useCache);
}
