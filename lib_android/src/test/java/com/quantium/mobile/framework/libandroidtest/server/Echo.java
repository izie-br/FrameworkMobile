package com.quantium.mobile.framework.libandroidtest.server;

import org.json.JSONException;
import org.json.JSONObject;

public class Echo extends BaseServerBean {

	public static final String ERROR_KEY = "error";

	public String getResponse() {
		JSONObject json = new JSONObject();
		if (getMap() == null)
			return json.toString();
		for (Object keyObj : getMap().keySet()) {
			String key = keyObj.toString();
			try {
				json.put(key, getParameter(key));
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			json.put(ERROR_KEY,JSONObject.NULL);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

}
