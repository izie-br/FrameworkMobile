package com.quantium.mobile.framework;

import java.util.Map;

public interface MapSerializable<T> {

	void toMap(Map<String,Object> map);
	Map<String,Object> toMap();

}
