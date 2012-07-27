package com.quantium.mobile.framework.test.server;

import org.json.JSONException;
import org.json.JSONObject;

public class Echo extends BaseServerBean {

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
		return json.toString();

	}

}
