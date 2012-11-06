package com.quantium.mobile.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.quantium.mobile.framework.query.QuerySet;

public class LazyInvocationHandler<T> implements InvocationHandler {

	QuerySet<T> querySet;
	Object key;
	String getter;

	T lazyInstance = null;

	public LazyInvocationHandler(QuerySet<T> querySet, Object fk, String getter){
		if (querySet == null)
			throw new RuntimeException();
		this.querySet = querySet;
		this.key = fk;
		this.getter = getter;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable
	{
		if (lazyInstance == null){
			if (method.getName().equals(this.getter)){
				return this.key;
			}
			lazyInstance = querySet.first();
			if (lazyInstance == null)
				throw new RuntimeException(String.format(
						"Object %s with id %s not found",
						querySet.getTable().getName(),
						(key == null)? "null" : key.toString() ));
		}
		return method.invoke(lazyInstance, args);
	}

}