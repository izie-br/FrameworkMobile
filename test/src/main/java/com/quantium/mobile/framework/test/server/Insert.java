package com.quantium.mobile.framework.test.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Insert extends BaseServerBean {

	private static final String keystoarray [] = { "objects", "list" };


	private String dataStr;
	private String classname;

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getJson() {
		return dataStr;
	}

	public void setJson(String data) {
		this.dataStr = data;
	}

	public String getResponse() {
		JSONArray savedObjects;
			savedObjects = (JSONArray) getAttribute(classname);
		if (savedObjects == null ) {
			savedObjects = new JSONArray ();
			setAttribute(classname, savedObjects);
		}
		try {
			JSONArray jsonArray = null;
			
			if (keystoarray == null)
				jsonArray = new JSONArray (dataStr);
			else {
				JSONObject jsonObject =
						new JSONObject(dataStr);
				for (String key : keystoarray ) {
					Object obj = jsonObject.get (key);
					if (obj instanceof JSONArray ) {
						jsonArray = (JSONArray)obj;
						break;
					}
					jsonObject = (JSONObject)obj;
				}
			}
			for (int i = 0; i < jsonArray.length (); i++) {
				savedObjects.put (jsonArray.get (i));
			}
			if (keystoarray != null) {
				JSONObject json = new JSONObject ();
				json.put("status","success");
				JSONObject current = json;
				for (int i=0; i < keystoarray.length; i++) {
					String key = keystoarray[i];
					if (i == keystoarray.length -1) {
						current.put("quantity", jsonArray.length());
						current.put(key, jsonArray);
					} else {
						JSONObject last = current;
						current = new JSONObject();
						last.put(key, current);
					}
				}
				return json.toString ();
			} else {
				return jsonArray.toString ();
			}
		}catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

}
