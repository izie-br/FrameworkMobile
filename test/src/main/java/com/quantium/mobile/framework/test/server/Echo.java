package com.quantium.mobile.framework.test.server;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class Echo extends BaseServerBean {

	private Map<?,?> map;

	public Map<?,?> getMap() {
		return map;
	}

	public void setMap(Map<?,?> map) {
		this.map = map;
	}

	public String getResponse() {
		JSONObject json = new JSONObject();
		if (map == null)
			return json.toString();
		for (Object keyObj : map.keySet()) {
			String key = keyObj.toString();
			try {
				String[] values = (String[])map.get(key);
				json.put(key, values[0]);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
		return json.toString();

	}

}
