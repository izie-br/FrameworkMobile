package com.quantium.mobile.framework.test.server;

import org.json.JSONException;
import org.json.JSONObject;

public class QueryServerBean {

	public static final String keystoarray [] = { "list" };

	private Object application;
	private String classname;

	public Object getApplication() {
		return application;
	}

	public void setApplication(Object application) {
		this.application = application;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getResponse() {
    Object obj = ServerBeanUtils.getAttribute(application, classname);
    if (obj == null )
        obj = new org.json.JSONArray ();
    if (keystoarray != null) {
        org.json.JSONObject json = new org.json.JSONObject ();
			JSONObject current = json;
			try {
				for (int i=0;; i++) {
					String key = keystoarray[i];
					if (i == keystoarray.length-1){
						current.put(key, obj);
						break;
					}
					JSONObject last = current;
					current = new JSONObject();
					last.put(key, current);
				}
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
        return json.toString ();
    } else {
        return obj.toString ();
    }
	}

}