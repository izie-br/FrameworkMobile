package com.quantium.memorytables;

import java.util.HashMap;
import java.util.Map;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.DAOFactory;

public class MemoryDaoFactory implements DAOFactory{

	public Map<Class<?>,MemoryTable> tables;

	public MemoryDaoFactory(){
		tables = new HashMap<Class<?>, MemoryTable>();
	}

	@Override
	public <T> DAO<T> getDaoFor(Class<T> klass) {
		MemoryTable table = tables.get(klass);
		if (table == null){
		}
		return null;
	}

}

