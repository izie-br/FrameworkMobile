package com.quantium.memorytables;

import java.io.IOException;
import java.lang.reflect.Method;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.MapSerializable;
import com.quantium.mobile.framework.query.QuerySet;

public abstract class MemoryTableDAO<T extends MapSerializable<?>>
		implements DAO<T>
{

	private MemoryTable table;
	private Method pkGetter;
	private Method pkSetter;
	MemoryDaoFactory factory;

	public MemoryTableDAO(MemoryTable table, Method pkGetter,
	                      Method pkSetter,MemoryDaoFactory factory)
	{
		this.table = table;
		this.factory = factory;

		this.pkGetter = pkGetter;
		this.pkGetter.setAccessible(true);
		this.pkSetter = pkSetter;
		this.pkSetter.setAccessible(true);
	}

	@Override
	public QuerySet<T> query() {
		return this.query(null);
	}

	@Override
	public boolean save(T obj) throws IOException {
		try {
			Comparable<?> pk = (Comparable<?>)pkGetter.invoke(obj);
			if (pk == null){
				Long newpk = this.table.insert(obj);
				pkSetter.invoke(obj, newpk);
			} else {
				this.table.put(pk, obj);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	@Override
	public boolean save(T obj, int flags) throws IOException {
		return this.save(obj);
	}

	@Override
	public boolean delete(T obj) throws IOException {
		try {
			Comparable<?> pk = (Comparable<?>)pkGetter.invoke(obj);
			if (pk == null){
				return false;
			} else {
				Object removed = this.table.remove(pk);
				return removed != null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
