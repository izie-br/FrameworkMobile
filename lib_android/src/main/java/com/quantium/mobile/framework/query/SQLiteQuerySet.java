package com.quantium.mobile.framework.query;

import java.sql.ResultSet;
import java.util.*;

import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.utils.StringUtil;
import com.quantium.mobile.framework.utils.ValueParser;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class SQLiteQuerySet<T> extends BaseQuerySet<T> {

    private static boolean debug = false;

    public static void setDebug(boolean debug) {
        SQLiteQuerySet.debug = debug;
    }

    public SQLiteQuerySet(ValueParser parser) {
		super(parser);
	}

	protected abstract T cursorToObject(Cursor cursor);

	protected abstract SQLiteDatabase getDb();

    public List<T> all(){
        List<T> all = new ArrayList<T>();
        if (debug) {
            LogPadrao.d((this.q == null ? 0 : this.q.hashCode())+" - Iniciando all:"+new Date());
        }
        Cursor cursor = getCursor(getColumns ());
        try{
            while(cursor.moveToNext())
                all.add(cursorToObject(cursor));
        } finally {
            cursor.close();
        }
        if (debug) {
            LogPadrao.d((this.q == null ? 0 : this.q.hashCode())+" - Finalizando all:"+new Date());
        }
        return all;
    }

    public Set<T> allUnique(){
        if (debug) {
            LogPadrao.d((this.q == null ? 0 : this.q.hashCode())+" - Iniciando allUnique:"+new Date());
        }
        Set<T> all = new HashSet<T>();
        Cursor cursor = getCursor(getColumns ());
        try{
            while(cursor.moveToNext())
                all.add(cursorToObject(cursor));
        } finally {
            cursor.close();
        }
        if (debug) {
            LogPadrao.d((this.q == null ? 0 : this.q.hashCode())+" - Finalizando allUnique:"+new Date());
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

    public Cursor getCursor(List<?> selection, Q anotherQ) {
        return getCursor(selection, anotherQ, true);
    }
    /**
     * Retorna o cursor, para uso em cursor adapter, etc.
     * @return cursor
     */
    public Cursor getCursor(List<?> selection, Q anotherQ, boolean distinct) {
        String args [] = null;
        ArrayList<Object> listArg = new ArrayList<Object>();
        String qstr = new QSQLProvider(anotherQ, parser)
                .limit(this.limit)
                .offset(this.offset)
                .orderBy(this.orderClauses)
                .select(selection,listArg, distinct);
        if (listArg.size() > 0) {
            args = new String[listArg.size()];
            for (int i=0; i < args.length; i++){
                args[i] = listArg.get(i).toString();
            }
        }
        if (debug) {
            LogPadrao.d((this.q == null ? 0 : this.q.hashCode())+" - qstr:"+qstr+"/args:"+ StringUtil.join(args, ","));
        }
        Cursor cursor = getDb().rawQuery(qstr, args);
        return cursor;
    }


    /**
     * Retorna o cursor, para uso em cursor adapter, etc.
     * @return cursor
     */
    public Cursor getCursor(List<?> selection, Q.GroupByClause groupByClause) {
        String args [] = null;
        List<Object>  list = new ArrayList<Object>();
        list.addAll(selection);
        list.add(String.format(groupByClause.getFunction().getName(), groupByClause.getColumn().getName()).concat(" as ").concat(groupByClause.getColumn().getName()));
        ArrayList<Object> listArg = new ArrayList<Object>();
        String qstr = new QSQLProvider(this.q, parser)
                .limit(this.limit)
                .offset(this.offset)
                .orderBy(this.orderClauses)
                .groupBy(selection)
                .select(list,listArg);
        if (debug) {
            LogPadrao.d((this.q == null ? 0 : this.q.hashCode())+" - qstr:"+qstr+"/args:"+ StringUtil.join(args, ","));
        }
        if (listArg.size() > 0) {
            args = new String[listArg.size()];
            for (int i=0; i < args.length; i++)
                args[i] = listArg.get(i).toString();
        }
        Cursor cursor = getDb().rawQuery(qstr, args);
        return cursor;
    }

    @Override
    public List<T> groupBy(Q.GroupByClause groupByClause, Object... selection) {
        List<T> all = new ArrayList<T>();
        Cursor cursor = getCursor(Arrays.asList(selection), groupByClause);
        try{
            while(cursor.moveToNext())
                all.add(cursorToObject(cursor));
        } finally {
            cursor.close();
        }
        return all;
    }

    @Override
    public T groupBy(Q.GroupByClause groupByClause) {
        Cursor cursor = getCursor(new ArrayList<Table.Column<?>>(), groupByClause);
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
    public <U> Set<U> selectDistinct(Table.Column<U> column) {
        if (q == null) {
            q = new Q (getTable());
        }
        Cursor cursor = getCursor(Arrays.asList(String.format("distinct(%s)", column.getTable().getName().concat(".").concat(column.getName()))), q.and(column.isNotNull()), false);
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

    @Override
    public void extractQ(StringBuilder where, List<Object> args) {
        new QSQLProvider(this.q, parser).genQstringAndArgs(where, args);
    }
}
