package com.quantium.mobile.framework;

import java.util.Map;

public interface MapSerializable<T> {

	T mapToObject(Map<String, Object> map);
	T mapToObject(Map<String, Object> map, DAOFactory daoFactory);
	void toMap(Map<String,Object> map);
	Map<String,Object> toMap();

}
