package com.quantium.mobile.framework.libandroidtest.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public abstract class BaseServerBean {

	private Object application;
	private Map<?,?> map;

	public Map<?,?> getMap() {
		return map;
	}

	public void setMap(Map<?,?> map) {
		this.map = map;
		mapValues();
	}

	public String getParameter(String name) {
		String values[] = (String[])getMap().get(name);
		return (values != null && values.length >0)? values[0] : null;

	}

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

	private void mapValues(){
		if (map == null)
			return;
		for (Object keyObj :map.keySet()) {
			String key = keyObj.toString();
			String methodName =
					"set" +
					Character.toUpperCase(key.charAt(0)) +
					key.substring(1);
			try {
				Method m = this.getClass().getMethod(methodName, String.class);
				m.setAccessible(true);
				m.invoke(this, getParameter(key));
			}
			catch (SecurityException e) {}
			catch (NoSuchMethodException e) {}
			catch (InvocationTargetException e) {}
			catch (IllegalAccessException e) {}
		}
	}

	public abstract String getResponse();

}
