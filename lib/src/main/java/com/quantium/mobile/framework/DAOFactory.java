package com.quantium.mobile.framework;

public interface DAOFactory {

	Object getDaoFor(Class<?> klass);

}
