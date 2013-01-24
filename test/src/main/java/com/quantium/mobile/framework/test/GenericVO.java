package com.quantium.mobile.framework.test;

import java.util.Collection;
import java.util.Map;

import com.quantium.mobile.framework.validation.ValidationError;

public interface GenericVO {

	public abstract void toMap(Map<String, Object> map);

	public abstract Map<String, Object> toMap();

	public abstract void triggerObserver(String column);

	public abstract int hashCodeImpl();

	public abstract boolean equalsImpl(Object obj);

	public abstract Collection<ValidationError> validate ();

}