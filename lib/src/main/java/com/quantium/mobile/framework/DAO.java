package com.quantium.mobile.framework;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.validation.ValidationError;

public interface DAO<T> {
	QuerySet<T> query();
	QuerySet<T> query(Q q);
	boolean save(T obj) throws IOException;
	boolean save(T obj, int flags) throws IOException;
	boolean delete(T obj) throws IOException;
	ToManyDAO with(T obj);
	T mapToObject(Map<String, Object> map);
	void updateWithMap(T target, Map<String, Object> map);
	Collection<ValidationError> validate (T obj);
	T get(Object id);
	Table getTable();
}
