package com.quantium.mobile.framework.libandroidtest.server;

import org.json.JSONArray;

public class Array extends BaseServerBean {

	public static final Object ARRAY[] = {
		"Val1",
		9.9
	};

	@Override
	public String getResponse() {
		JSONArray json = new JSONArray();
		for (Object obj : ARRAY){
			json.put(obj);
		}
		return json.toString();
	}

}
