package com.quantium.mobile.framework;

import java.util.Map;

public interface MapSerializable<T> {

	T mapToObjectWithPrototype(Map<String, Object> map);
	Map<String,Object> toMap();

}
