package com.quantium.mobile.framework;

import org.json.JSONException;
import org.json.JSONObject;

public interface JsonSerializable<T> {

	T jsonToObjectWithPrototype(JSONObject json) throws JSONException;
	JSONObject toJson();

}
