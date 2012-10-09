package com.quantium.mobile.framework;

public interface DAOFactory {
	<T> DAO<T> getDaoFor(Class<T> klass);
}
