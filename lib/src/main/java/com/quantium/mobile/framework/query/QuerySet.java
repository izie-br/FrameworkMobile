package com.quantium.mobile.framework.query;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    QuerySet<T> filter(String rawQuery, Table table);
	List<T> all();
	T first();
	long count ();
    <U> Set<U> selectDistinct(Table.Column<U> column);
    List<T> groupBy(Q.GroupByClause groupByClause, Table.Column<?> ... selection);
    Set<T> allUnique();
}
