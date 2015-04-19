package com.quantium.mobile.framework;

import com.quantium.mobile.framework.query.QuerySet;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class LazyInvocationHandler<T extends BaseGenericVO> implements InvocationHandler {

    QuerySet<T> querySet;
    Object key;
    String getter;
    BaseModelFacade modelFacade;
    T lazyInstance = null;
    Class<T> klass;

    public LazyInvocationHandler(Class<T> klass, BaseModelFacade modelFacade, QuerySet<T> querySet, Object fk, String getter) {
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
            throws Throwable {
        if (lazyInstance == null) {
            if (method.getName().equals(this.getter)) {
                return this.key;
            }
            try {
                lazyInstance = querySet.first();
            } catch (Exception e) {
                throw new LazyLoadException(e);
            }
            if (lazyInstance == null) {
                lazyInstance = modelFacade.refresh(klass, (String) key);
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
