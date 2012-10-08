package com.quantium.mobile.framework;

import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;

public interface DAO<T> {
	QuerySet<T> query();
	QuerySet<T> query(Q q);
	boolean save(T obj);
	boolean save(T obj, int flags);
	boolean delete(T obj);
}
