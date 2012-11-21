package com.quantium.mobile.framework.query;

import java.util.ArrayList;
import java.util.List;

import com.quantium.mobile.framework.utils.StringUtil;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class SQLiteQuerySet<T> implements QuerySet<T>, Cloneable{
	private Q q;
	private String orderBy;

//	private String groupBy;
//	private String having;

	private int limit;
	private int offset;


	protected abstract T cursorToObject(Cursor cursor);

	protected abstract Table.Column<?>[] getColunas();

	protected abstract SQLiteDatabase getDb();

	public QuerySet<T> orderBy(Table.Column<?> column, Q.OrderByAsc asc){
		SQLiteQuerySet<T> obj = this.clone();
		if (column != null) {
			obj.orderBy =
					( (this.orderBy == null) ? "" : this.orderBy + ",") +
					" " + column.getTable().getName() +
					"." + column.getName() +
					" " + asc.toString();
		}
		return obj;
	}

	public QuerySet<T> orderBy(Table.Column<?> column){
		return orderBy(column, Q.ASC);
	}

	public QuerySet<T> limit (int limit){
		SQLiteQuerySet<T> obj = this.clone();
		if (limit > 0)
			obj.limit = limit;
		return obj;
	}

	public QuerySet<T> offset (int offset){
		SQLiteQuerySet<T> obj = this.clone();
		if (offset > 0)
			obj.offset = offset;
		return obj;
	}

	public QuerySet<T> filter(Q q){
		if (q==null)
			return this;
		SQLiteQuerySet<T> obj = this.clone();
		if (this.q==null)
			obj.q = q;
		else
			obj.q = this.q.and (q);
		return obj;
	}


	public List<T> all(){
		List<T> all = new ArrayList<T>();
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
						String.format("LIMIT %d,%d", offset,limit):
				(limit>0) ?
						String.format("LIMIT %d", limit):
				(offset>0) ?
						String.format("OFFSET %d,", offset):
				// limit <= 0 && offset <= 0
						"";

		if (this.q == null) {
			this.q = new Q (getTable());
		}
		String args [] = null;
		ArrayList<Object> listArg = new ArrayList<Object>();
		String qstr = new QSQLProvider(q).select(getColunas(),listArg);
		if (listArg.size() > 0) {
			args = new String[listArg.size()];
			for (int i=0; i < args.length; i++)
				args[i] = listArg.get(i).toString();
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

	@Override
	protected SQLiteQuerySet<T> clone() {
		try {
			@SuppressWarnings("unchecked")
			SQLiteQuerySet<T> obj = (SQLiteQuerySet<T>)super.clone();
			return obj;
		} catch (CloneNotSupportedException e) {
			// Impossivel a menos que a excecao seja explicitamente criada
			//    ou que esta classe deixe de implementar Cloenable
			throw new RuntimeException(StringUtil.getStackTrace(e));
		}
	}

}
