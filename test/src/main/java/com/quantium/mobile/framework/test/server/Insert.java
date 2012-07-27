package com.quantium.mobile.framework.test.server;

import org.json.JSONException;

public class Insert {
	private static final String keystoarray [] = { "objects", "list" };


	private Object application;
	private String dataStr;
	private String classname;

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public Object getApplication() {
		return application;
	}

	public void setApplication(Object application) {
		this.application = application;
	}

	public String getData() {
		return dataStr;
	}

	public void setData(String data) {
		this.dataStr = data;
	}

	public String getResponse() {
		if (application == null)
			throw new RuntimeException("application NULL");
		org.json.JSONArray savedObjects;
			savedObjects = (org.json.JSONArray) ServerBeanUtils.getAttribute(
					application,
					classname
			);
    if (savedObjects == null ) {
        savedObjects = new org.json.JSONArray ();
        ServerBeanUtils.setAttribute(application,classname, savedObjects);
    }
		try {
    org.json.JSONArray jsonArray = null;

    if (keystoarray == null)
             jsonArray = new org.json.JSONArray (dataStr);
    else {
        org.json.JSONObject jsonObject = new org.json.JSONObject(dataStr);
        for (String key : keystoarray ) {
            Object obj = jsonObject.get (key);
            if (obj instanceof org.json.JSONArray ) {
                jsonArray = (org.json.JSONArray)obj;
                break;
            }
            jsonObject = (org.json.JSONObject)obj;
        }
    }
    for (int i = 0; i < jsonArray.length (); i++) {
        savedObjects.put (jsonArray.get (i));
    }
    if (keystoarray != null) {
        org.json.JSONObject json = new org.json.JSONObject ();
        json.put("status","success");
        org.json.JSONObject current = json;
        for (int i=0; i < keystoarray.length; i++) {
            String key = keystoarray[i];
            if (i == keystoarray.length -1) {
                current.put("quantity", jsonArray.length());
                current.put(key, jsonArray);
            } else {
                org.json.JSONObject last = current;
                current = new org.json.JSONObject();
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
