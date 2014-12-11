package com.quantium.mobile.framework.query;

import java.util.ArrayList;
import java.util.List;

import com.quantium.mobile.framework.utils.ValueParser;

public abstract class BaseQuerySet<T> implements QuerySet<T>, Cloneable{

	protected Q q;
	protected ValueParser parser;
	protected List<Q.OrderByClause> orderClauses =
			new ArrayList<Q.OrderByClause>(1);

//	private String groupBy;
//	private String having;

	protected int limit;
	protected int offset;

	public BaseQuerySet(ValueParser parser) {
		this.parser = parser;
	}
	
	public ValueParser getParser() {
		return parser;
	}

	protected abstract List<Table.Column<?>> getColumns();

	public QuerySet<T> orderBy(Table.Column<?> column, Q.OrderByAsc asc){
		if (asc == null || asc == Q.ASC)
			return orderBy(column.asc());
		else
			return orderBy(column.desc());
	}

	public QuerySet<T> orderBy(Table.Column<?> column){
		return orderBy(column.asc());
	}

	public QuerySet<T> orderBy(Q.OrderByClause clause){
		BaseQuerySet<T> obj = this.clone();
		obj.orderClauses.add(clause);
		return obj;
	}

	public QuerySet<T> limit (int limit){
		BaseQuerySet<T> obj = this.clone();
		if (limit > 0)
			obj.limit = limit;
		return obj;
	}

	public QuerySet<T> offset (int offset){
		BaseQuerySet<T> obj = this.clone();
		if (offset > 0)
			obj.offset = offset;
		return obj;
	}

    public QuerySet<T> filter(Q q){
        if (q==null)
            return this;
        BaseQuerySet<T> obj = this.clone();
        if (this.q==null)
            obj.q = q;
        else
            obj.q = this.q.and (q);
        return obj;
    }

    public QuerySet<T> filter(String rawQuery, Table table){
        if (rawQuery==null)
            return this;
        BaseQuerySet<T> obj = this.clone();
        if (this.q==null)
            obj.q = new Q(rawQuery, table);
        else
            obj.q = this.q.and (new Q(rawQuery, table));
        return obj;
    }


    @Override
	protected BaseQuerySet<T> clone() {
		try {
			@SuppressWarnings("unchecked")
			BaseQuerySet<T> obj = (BaseQuerySet<T>)super.clone();
			obj.orderClauses = new ArrayList<Q.OrderByClause>(obj.orderClauses);
			return obj;
		} catch (CloneNotSupportedException e) {
			// Impossivel a menos que a excecao seja explicitamente criada
			//    ou que esta classe deixe de implementar Cloenable
			throw new RuntimeException(e);
		}
	}


}
