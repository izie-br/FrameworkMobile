package com.quantium.mobile.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.query.QuerySet;

public class LazyInvocationHandler<T> implements InvocationHandler {

	QuerySet<T> querySet;
	Object key;
	String getter;
	BaseModelFacade modelFacade;
	T lazyInstance = null;
	Class<? extends BaseGenericVO> klass;

	public LazyInvocationHandler(Class<? extends BaseGenericVO> klass, BaseModelFacade modelFacade, QuerySet<T> querySet, Object fk, String getter){
		if (querySet == null)
			throw new RuntimeException();
		this.querySet = querySet;
		this.klass = klass;
		this.key = fk;
		this.getter = getter;
		this.modelFacade = modelFacade;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable
	{
		if (lazyInstance == null){
			if (method.getName().equals(this.getter)){
				return this.key;
			}
			try {
				lazyInstance = querySet.first();
			} catch (Exception e) {
				throw new LazyLoadException(e);
			}
			if (lazyInstance == null){
				//tentando procurar um serverId para este temp id;
				Object serverId = modelFacade.getIdServerById(key, klass);
				if(serverId == null){
					LogPadrao.e(new LazyLoadException(String.format(
							"Object's server id %s from id %s not found",
							querySet.getTable().getName(),
							(key == null)? "null" : key.toString() )));
					return BaseModelFacade.NULL_LAZY;
				}else{
					BaseGenericVO serverObj = modelFacade.get(klass, serverId);
					if(serverObj == null){
						LogPadrao.e(new LazyLoadException(String.format(
								"Object %s server id %s not found",
								querySet.getTable().getName(),
								(serverId == null)? "null" : serverId.toString() )));
                        return BaseModelFacade.NULL_LAZY;
					} else {
						modelFacade.updatePrimaryKey(serverObj, key);
						modelFacade.updatePrimaryKey(serverObj, serverId);
					}

				}
			}
		}
		return method.invoke(lazyInstance, args);
	}

	public static class LazyLoadException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public LazyLoadException() {
			super();
		}

		public LazyLoadException(String message, Throwable cause) {
			super(message, cause);
		}

		public LazyLoadException(String message) {
			super(message);
		}

		public LazyLoadException(Throwable cause) {
			super(cause);
		}

		
	}

}
