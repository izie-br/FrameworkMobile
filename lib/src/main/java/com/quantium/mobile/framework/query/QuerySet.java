package com.quantium.mobile.framework.query;

import java.util.List;

public interface QuerySet<T>{

	Table getTable();

	QuerySet<T> orderBy(Table.Column<?> column, Q.OrderByAsc asc);
	QuerySet<T> orderBy(Table.Column<?> column);

	QuerySet<T> limit (int limit);
	QuerySet<T> offset (int offset);

	QuerySet<T> filter(Q q);

	List<T> all();
	T first();

	long count ();
}
