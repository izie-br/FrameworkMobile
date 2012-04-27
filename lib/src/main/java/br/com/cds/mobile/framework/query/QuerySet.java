package br.com.cds.mobile.framework.query;

import java.util.ArrayList;
import java.util.Collection;

import br.com.cds.mobile.framework.utils.SQLiteUtils;
import br.com.cds.mobile.framework.utils.StringUtil;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class QuerySet<T>{

	private String where;
	private ArrayList<Object> selectionArgs;
	private String orderBy;

	private String groupBy;
	private String having;

	private int limit;
	private int offset;
	//private boolean distinct;


	protected abstract T cursorToObject(Cursor cursor);

	protected abstract String[] getColunas();

	protected abstract String getTabela();

	protected abstract SQLiteDatabase getDb();

	@SuppressWarnings("deprecation")
	public QuerySet<T> filter(Q q){
		return filter(q.toString(),q.getArgumentos());
	}

	@Deprecated
	public QuerySet<T> filter(String qstr,Object...args){
		if(StringUtil.isNull(where))
			where = qstr;
		else
			where = String.format("(%s) AND (%s)",this.where,qstr);
		if(args!=null){ 
			if(selectionArgs == null)
				selectionArgs = new ArrayList<Object>( args.length );
			for(Object arg : args)
				selectionArgs.add(arg);
		}
		return this;
	}

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

	private String[] selectionArgs(){
		if(selectionArgs==null)
			return null;
		String args[] = new String[ selectionArgs.size()];
		for(int i=0;i<args.length;i++)
			args[i] = SQLiteUtils.parse(selectionArgs.get(i));
		return args;
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
						null;

		Cursor cursor = getDb().query(
			// distinct
			true,
			// tabela
			getTabela(),
			// colunas
			getColunas(),
			// where (********)
			where,
			// argumentos para substituir os "?"
			selectionArgs(),
			// groupBy e having
			groupBy, having,
			// order by
			orderBy,
			// limit e offset
			limitStr
		);
		return cursor;
	}


}
