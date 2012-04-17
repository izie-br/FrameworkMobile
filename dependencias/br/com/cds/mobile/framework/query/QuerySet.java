package br.com.cds.mobile.framework.query;

import java.util.ArrayList;
import java.util.Collection;

import br.com.cds.mobile.framework.config.DB;
import br.com.cds.mobile.framework.utils.SQLiteUtils;
import br.com.cds.mobile.framework.utils.StringUtil;

import android.database.Cursor;

public abstract class QuerySet<T> /*implements Collection<T>*/{

	private String where;
	private Object selectionArgs[];
	private String orderBy;

	private String groupBy;
	private String having;

	private int limit;
	private int offset;
	//private boolean distinct;

	protected abstract T cursorToObject(Cursor cursor);

	protected abstract String[] getColunas();

	protected abstract String getTabela();

	public QuerySet<T> distinct(){
		//distinct = true;
		return this;
	}

	@Deprecated
	public QuerySet<T> filter(String qstr,Object...args){
		if(StringUtil.isNull(where))
			where = qstr;
		else
			where = String.format("(%s) AND (%s)",this.where,qstr);
		Object newargs[] = new Object[(selectionArgs==null)?0:selectionArgs.length+args.length];
		for(int i=0;i<newargs.length;i++)
			newargs[i] = i<selectionArgs.length ? selectionArgs[i] : args[i-selectionArgs.length];
		return this;
	}

	public Collection<T> all(){
		Collection<T> all = new ArrayList<T>();
		Cursor cursor = getCursor();
		while(cursor.moveToNext())
			all.add(cursorToObject(cursor));
		return all;
	}

	public T first(){
		Cursor cursor = getCursor();
		if(cursor.moveToNext())
			return cursorToObject(cursor);
		return null;
	}

	private String[] selectionArgs(){
		String args[] = new String[selectionArgs.length];
		for(int i=0;i<selectionArgs.length;i++)
			args[i] = SQLiteUtils.parse(selectionArgs[i]);
		return args;
	}

	private Cursor getCursor() {
		Cursor cursor = DB.getDb().query(
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
				( limit > 0 ?
					(offset >0 ?
						"" + limit + " , " + offset :
						"" + limit
					) :
					null
				)
		);
		return cursor;
	}

/*
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(T e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
*/

}
