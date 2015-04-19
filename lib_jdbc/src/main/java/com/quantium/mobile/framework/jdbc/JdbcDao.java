package com.quantium.mobile.framework.jdbc;

import com.quantium.mobile.framework.DAO;

import java.sql.ResultSet;

public interface JdbcDao<T> extends DAO<T> {
    T cursorToObject(ResultSet cursor, boolean useCache);
}
