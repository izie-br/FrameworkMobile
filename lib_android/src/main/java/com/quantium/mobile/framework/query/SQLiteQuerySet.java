package com.quantium.mobile.framework.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class SQLiteQuerySet<T> extends BaseQuerySet<T> {

	protected abstract T cursorToObject(Cursor cursor);

	protected abstract SQLiteDatabase getDb();

	@Override
	protected String nullsFirstOrderingClause() {
		// o SQLITE retorna NULLS FIRST por padrao em todas buscas
		return null;
	}

	public List<T> all(){
		List<T> all = new ArrayList<T>();
		Cursor cursor = getCursor(getColumns ());
		try{
			while(cursor.moveToNext())
				all.add(cursorToObject(cursor));
		} finally {
			cursor.close();
		}
		return all;
	}

	public T first(){
		Cursor cursor = getCursor(getColumns ());
		try{
			if(cursor.moveToNext())
				return cursorToObject(cursor);
		}
		finally{
			cursor.close();
		}
		return null;
	}

	@Override
	public long count() {
		Cursor cursor = getCursor(Arrays.asList ("count(*)"));
		try{
			if(cursor.moveToNext())
				return cursor.getLong (0);
		}
		finally{
			cursor.close();
		}
		throw new RuntimeException();
	}

	/**
	 * Retorna o cursor, para uso em cursor adapter, etc.
	 * @return cursor
	 */
	public Cursor getCursor(List<?> selection) {
		if (limit <= 0)
			limit = -1;
		String limitStr =
				(offset > 0) ?
						String.format("LIMIT %d,%d", offset, limit):
				(limit > 0) ?
						String.format("LIMIT %d", limit):
//				(offset != 0) ?
//						String.format("LIMIT %d,%d", -1, offset):
				// limit <= 0 && offset <= 0
						"";

		if (this.q == null) {
			this.q = new Q (getTable());
		}
		String args [] = null;
		ArrayList<Object> listArg = new ArrayList<Object>();
		String qstr = new QSQLProvider(q).select(selection,listArg);
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

}
