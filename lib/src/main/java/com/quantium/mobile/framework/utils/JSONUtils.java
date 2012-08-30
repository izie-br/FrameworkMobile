package com.quantium.mobile.framework.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quantium.mobile.framework.logging.LogPadrao;

public class JSONUtils {

	public static Map<String,Object> desserializeJsonObject (JSONObject json){
		return desserializeJsonObject(json, new HashMap<String, Object>());
	}

	public static Map<String,Object> desserializeJsonObject (
			JSONObject json, Map<String, Object> out){
		@SuppressWarnings("unchecked")
		Iterator<String> it = json.keys();
		while (it.hasNext()){
			String key = it.next();
			try {
				out.put(key, desserializeJsonValue(json.get(key)));
			} catch (JSONException e) {
				LogPadrao.e(e);
			}
		}
		return out;
	}

	public static Object desserializeJsonValue (Object value)
			throws JSONException
	{
		if (value instanceof JSONArray)
			return desserializeJsonArray( (JSONArray)value, new ArrayList<Object>());
		if (value instanceof JSONObject) {
			return desserializeJsonObject( (JSONObject)value, new HashMap<String, Object>());
		}
		if ( JSONObject.NULL.getClass().getSimpleName().equals(
				value.getClass().getSimpleName()) ){
			return null;
		}
		return value;
	}

	public static List<Object> desserializeJsonArray(JSONArray jsonArray){
		return desserializeJsonArray(jsonArray, new ArrayList<Object>());
	}

	public static List<Object> desserializeJsonArray(
			JSONArray jsonArray, List<Object> list){
		for (int i=0; i < jsonArray.length(); i++){
			try{
				list.add(desserializeJsonValue(jsonArray.get(i)));
			} catch (JSONException e) {
				LogPadrao.e(e);
			}
		}
		return list;
	}

	public static JSONObject mapToJson(Map<String,Object> map){
		JSONObject json = new JSONObject();
		for (Map.Entry<String, Object> entry : map.entrySet()){
			String key = entry.getKey();
			Object value = entry.getValue();
			if (key != null && value != null)
				try {
						json.put(key, parseToJson(value));
				} catch (JSONException e) {
					LogPadrao.e(e);
					throw new RuntimeException(e);
				}
		}
		return json;
	}

	public static JSONArray collectionToJson(Collection<?> collection){
		Iterator<?> it = collection.iterator();
		JSONArray array = new JSONArray();
		while (it.hasNext()){
			array.put(parseToJson(it.next()));
		}
		return array;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object parseToJson(Object obj){
		return
			(obj instanceof Map)?
				mapToJson((Map)obj) :
			(obj instanceof Collection) ?
				collectionToJson((Collection)obj) :
				obj;
	}

}
