package com.quantium.mobile.framework.test.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServerBeanUtils {

	public static void setAttribute(Object ctx, String name, Object value) {
		Method setAttribute;
		try {
			setAttribute = ctx.getClass()
				.getMethod("setAttribute", String.class, Object.class);
			setAttribute.invoke(ctx, name, value);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static Object getAttribute(Object ctx, String name) {
		Method getAttribute;
		try {
			getAttribute = ctx.getClass()
				.getMethod("getAttribute", String.class);
			return getAttribute.invoke(ctx, name);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
