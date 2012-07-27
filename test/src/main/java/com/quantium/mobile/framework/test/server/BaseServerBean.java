package com.quantium.mobile.framework.test.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BaseServerBean {

	private Object application;

	public Object getApplication() {
		return application;
	}

	public void setApplication(Object application) {
		this.application = application;
	}

	public void setAttribute(String name, Object value) {
		Method method;
		try {
			if (value == null) {
				method = application.getClass()
						.getMethod("removeAttribute", String.class);
				method.invoke(application, name);
			} else {
				method = application.getClass()
					.getMethod("setAttribute", String.class, Object.class);
				method.invoke(application, name, value);
			}
		}
		catch (NoSuchMethodException e)     { throw new RuntimeException(e); }
		catch (InvocationTargetException e) { throw new RuntimeException(e); }
		catch (IllegalAccessException e)    { throw new RuntimeException(e); }
		
	}

	public Object getAttribute(String name) {
		Method getAttribute;
		try {
			getAttribute = application.getClass()
				.getMethod("getAttribute", String.class);
			return getAttribute.invoke(application, name);
		}
		catch (NoSuchMethodException e)     { throw new RuntimeException(e); }
		catch (InvocationTargetException e) { throw new RuntimeException(e); }
		catch (IllegalAccessException e)    { throw new RuntimeException(e); }
	}

}
