package com.quantium.mobile.framework;

import java.io.IOException;

import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;

public interface DAO<T> {
	QuerySet<T> query();
	QuerySet<T> query(Q q);
	boolean save(T obj) throws IOException;
	boolean save(T obj, int flags) throws IOException;
	boolean delete(T obj) throws IOException;
	ToManyDAO with(T obj);
}
