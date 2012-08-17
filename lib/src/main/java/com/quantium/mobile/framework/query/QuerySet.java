package com.quantium.mobile.framework.query;

import java.util.ArrayList;
import java.util.Collection;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class QuerySet<T>{

	private Q q;
//	private String where;
//	private List<String> selectionArgs;
	private String orderBy;

//	private String groupBy;
//	private String having;

	private int limit;
	private int offset;
	//private boolean distinct;


	protected abstract T cursorToObject(Cursor cursor);

	protected abstract Table.Column<?>[] getColunas();

	protected abstract Table getTabela();

	protected abstract SQLiteDatabase getDb();

	public QuerySet<T> orderBy(Table.Column<?> column, Q.OrderByAsc asc){
		if (column != null) {
			this.orderBy =
					( (this.orderBy == null) ? "" : this.orderBy ) +
					" " + column.getTable().getName() +
					"." + column.getName() +
					" " + asc.toString();
		}
		return this;
	}

	public QuerySet<T> orderBy(Table.Column<?> column){
		return orderBy(column, Q.ASC);
	}

	public QuerySet<T> limit (int limit){
		if (limit > 0)
			this.limit = limit;
		return this;
	}

	public QuerySet<T> offset (int offset){
		if (offset > 0)
			this.offset = offset;
		return this;
	}

	public QuerySet<T> filter(Q q){
		if (q==null)
			return this;
		else if (this.q==null)
			this.q = q;
		else
			this.q.and (q);
		return this;
	}

//	@Deprecated
//	public QuerySet<T> filter(String qstr,Object...args){
//		if(StringUtil.isNull(where))
//			where = qstr;
//		else
//			where = String.format("(%s) AND (%s)",this.where,qstr);
//		if(args!=null){ 
//			if(selectionArgs == null)
//				selectionArgs = new ArrayList<String>( args.length );
//			for(Object arg : args)
//				selectionArgs.add(SQLiteUtils.parse(arg));
//		}
//		return this;
//	}

	public Collection<T> all(){
		Collection<T> all = new ArrayList<T>();
		Cursor cursor = getCursor();
		try{
			while(cursor.moveToNext())
				all.add(cursorToObject(cursor));
		} finally {
			cursor.close();
		}
		return all;
	}

	public T first(){
		if(limit<0)
			limit = 1;
		Cursor cursor = getCursor();
		try{
			if(cursor.moveToNext())
				return cursorToObject(cursor);
		}
		finally{
			cursor.close();
		}
		return null;
	}

	/**
	 * Retorna o cursor, para uso em cursor adapter, etc.
	 * @return cursor
	 */
	public Cursor getCursor() {
		String limitStr =
				(limit>0 && offset>0) ?
						String.format("%d,%d", offset,limit):
				(limit>0) ?
						String.format("%d", limit):
				(offset>0) ?
						String.format("%d,", offset):
				// limit <= 0 && offset <= 0
						"";

//		Cursor cursor = getDb().query(
//			// distinct
//			true,
//			// tabela
//			getTabela(),
//			// colunas
//			getColunas(),
//			// where (********)
//			where,
//			// argumentos para substituir os "?"
//			selectionArgs(),
//			// groupBy e having
//			groupBy, having,
//			// order by
//			orderBy,
//			// limit e offset
//			limitStr
//		);
		if (this.q == null) {
			this.q = new Q (getTabela());
		}
		String args [] = null;
		ArrayList<String> listArg = new ArrayList<String>();
		String qstr = q.select(getColunas(),listArg);
		if (listArg.size() > 0) {
			args = new String[listArg.size()];
			listArg.toArray(args);
		}
		String orderByLocal = orderBy == null  ? "" : (" ORDER BY " + orderBy + " ");
		Cursor cursor = getDb().rawQuery(
				qstr +
				orderByLocal +
				" " +limitStr,
				args
		);
		return cursor;
	}


}
