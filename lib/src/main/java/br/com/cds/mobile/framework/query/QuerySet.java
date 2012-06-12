package br.com.cds.mobile.framework.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class QuerySet<T>{

	private Q q;
//	private String where;
//	private List<String> selectionArgs;
//	private String orderBy;

//	private String groupBy;
//	private String having;

	private int limit;
	private int offset;
	//private boolean distinct;


	protected abstract T cursorToObject(Cursor cursor);

	protected abstract Table.Column<?>[] getColunas();

	protected abstract Table getTabela();

	protected abstract SQLiteDatabase getDb();

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
		String args [] = null;
		if (this.q == null) {
			this.q = new Q (getTabela());
		} else {
			List<String> arguments = q.getArguments();
			if (arguments != null) {
				args = new String[arguments.size()];
				arguments.toArray(args);
			}
		}
		Cursor cursor = getDb().rawQuery(
				q.select(getColunas())+" " +limitStr,
				args
		);
		return cursor;
	}


}
