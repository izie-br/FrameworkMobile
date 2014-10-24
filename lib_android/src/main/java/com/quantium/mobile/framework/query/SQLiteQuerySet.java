package com.quantium.mobile.framework.query;

import java.sql.ResultSet;
import java.util.*;

import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.utils.ValueParser;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class SQLiteQuerySet<T> extends BaseQuerySet<T> {

	public SQLiteQuerySet(ValueParser parser) {
		super(parser);
	}

	protected abstract T cursorToObject(Cursor cursor);

	protected abstract SQLiteDatabase getDb();

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
        return getCursor(selection, this.q);
    }


    /**
     * Retorna o cursor, para uso em cursor adapter, etc.
     * @return cursor
     */
    public Cursor getCursor(List<?> selection, Q anotherQ) {
        String args [] = null;
        ArrayList<Object> listArg = new ArrayList<Object>();
        String qstr = new QSQLProvider(anotherQ, parser)
                .limit(this.limit)
                .offset(this.offset)
                .orderBy(this.orderClauses)
                .select(selection,listArg);
        if (listArg.size() > 0) {
            args = new String[listArg.size()];
            for (int i=0; i < args.length; i++)
                args[i] = listArg.get(i).toString();
        }
        Cursor cursor = getDb().rawQuery(qstr, args);
        return cursor;
    }


    @Override
    public <U> Set<U> selectDistinct(Table.Column<U> column) {
        if (q == null) {
            q = new Q (getTable());
        }
        Cursor cursor = getCursor(Arrays.asList(String.format("distinct(%s)", column.getName())), q.and(column.isNotNull()));
        Set<U> set = new HashSet<U>();
        while (cursor.moveToNext()){
            if (column.getKlass().isAssignableFrom(String.class)) {
                set.add((U) cursor.getString(0));
            } else if (column.getKlass().isAssignableFrom(Double.class)) {
                set.add((U) new Double(cursor.getDouble(0)));
            } else if (column.getKlass().isAssignableFrom(Long.class)) {
                set.add((U) new Long(cursor.getLong(0)));
            } else if (column.getKlass().isAssignableFrom(Boolean.class)) {
                set.add((U) new Boolean(cursor.getInt(0)==1));
            }
        }
        cursor.close();
        return set;
    }

}
