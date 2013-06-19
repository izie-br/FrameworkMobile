package com.quantium.mobile.framework.query;

import java.io.Serializable;
import java.util.List;

public interface QuerySet<T> extends Serializable {

	Table getTable();

	/**
	 * Use "orderBy( coluna.asc())" ou "orderBy( coluna.desc())"
	 * para maior legibilidade
	 */
	@Deprecated
	QuerySet<T> orderBy(Table.Column<?> column, Q.OrderByAsc asc);
	QuerySet<T> orderBy(Table.Column<?> column);
	QuerySet<T> orderBy(Q.OrderByClause clause);

	QuerySet<T> limit (int limit);
	QuerySet<T> offset (int offset);

	QuerySet<T> filter(Q q);

	List<T> all();
	T first();

	long count ();
}
