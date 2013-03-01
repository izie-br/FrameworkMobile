package com.quantium.mobile.framework.query;

public abstract class BaseQuerySet<T> implements QuerySet<T>, Cloneable{

	protected Q q;
	protected String orderBy;

//	private String groupBy;
//	private String having;

	protected int limit;
	protected int offset;

	protected abstract Table.Column<?>[] getColunas();

	protected abstract String nullsFirstOrderingClause();

	public QuerySet<T> orderBy(Table.Column<?> column, Q.OrderByAsc asc){
		StringBuilder orderBySb = new StringBuilder();
		// Adiciona "{orderbyantigo}, " antes do novo item {orderBy}
		if (this.orderBy != null) {
			orderBySb.append(this.orderBy);
			orderBySb.append(',');
		}
		// Escreve:
		//   "{tabela}.{coluna} {ASC|DESC} <NULL_ORDERING>"
		orderBySb.append(column.getTable().getName());
		orderBySb.append(".");
		orderBySb.append(column.getName());
		orderBySb.append(" ");
		orderBySb.append(asc.toString());
		// NULL_ORDERING, nao eh necessario no SQLITE
		// no H2DB eh "NULLS FIRST"
		String nullOrdering = this.nullsFirstOrderingClause();
		if (nullOrdering != null) {
			orderBySb.append(" ");
			orderBySb.append(nullOrdering);
		}
		BaseQuerySet<T> obj = this.clone();
		obj.orderBy = orderBySb.toString();
		return obj;
	}

	public QuerySet<T> orderBy(Table.Column<?> column){
		return orderBy(column, Q.ASC);
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

	@Override
	protected BaseQuerySet<T> clone() {
		try {
			@SuppressWarnings("unchecked")
			BaseQuerySet<T> obj = (BaseQuerySet<T>)super.clone();
			return obj;
		} catch (CloneNotSupportedException e) {
			// Impossivel a menos que a excecao seja explicitamente criada
			//    ou que esta classe deixe de implementar Cloenable
			throw new RuntimeException(e);
		}
	}


}
