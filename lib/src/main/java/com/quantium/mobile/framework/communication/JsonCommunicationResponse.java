package com.quantium.mobile.framework.communication;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quantium.mobile.framework.FrameworkJSONTokener;
import com.quantium.mobile.framework.JsonSerializable;
import com.quantium.mobile.framework.JsonToObjectIterator;
import com.quantium.mobile.framework.logging.LogPadrao;

public class JsonCommunicationResponse implements SerializedCommunicationResponse{

	private Reader reader;

	private JSONObject json;

	public JsonCommunicationResponse(Reader reader){
		this.reader = reader;
	}

	@Override
	public Reader getReader() {
		return reader;
	}

	private void checkOutput() {
		if (json == null ) {
			try {
				json = new FrameworkJSONTokener(reader).nextJSONObject();
			} catch (JSONException e) {
				LogPadrao.e(e);
			}
		}
	}

	@Override
	public Map<String, Object> getResponseMap() {
		checkOutput();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			desserializeJsonObject(json, map);
		} catch (JSONException e) {
			LogPadrao.e(e);
			return null;
		}
		return map;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> Iterator<T> getIterator(T prototype, String... keysToObjectList) {
		if ( !( prototype instanceof JsonSerializable) )
			return null;
		checkOutput();
		try {
			if(keysToObjectList != null && keysToObjectList.length >0) {
				int l = keysToObjectList.length;
				Object current = json;
				JSONObject last = json;
				String key = null;
				for (int i = 0; i < l; i++) {
					key = keysToObjectList[i];
					last = (JSONObject)current;
					current = last.get(key);
				}
				
				if (key != null)
					last.remove(key);
				return new JsonToObjectIterator(
						new StringReader(current.toString()),
						(JsonSerializable)prototype
				);
			}
		}catch (Exception e){
			LogPadrao.e(e);
		}
		return null;
	}


	@Override
	public Iterator<Object> getIterator(String... keysToObjectList) {
//		if ( !( prototype instanceof JsonSerializable) )
//			return null;
//		checkOutput();
//		try {
//			if(keysToObjectList != null && keysToObjectList.length >0) {
//				Object current = json;
//				JSONObject last = json;
//				String key = null;
//				for (int i = 0; i < keysToObjectList.length; i++) {
//					key = keysToObjectList[i];
//					last = (JSONObject)current;
//					current = last.get(key);
//				}
//				return new Json
//			}
//		}catch (Exception e){
//			LogPadrao.e(e);
//		}
		return null;
	}

/*	private JSONArray getJsonAray(String... keysToObjectList){
		checkOutput();
		try {
			if(keysToObjectList != null && keysToObjectList.length >0) {
				Object current = json;
				JSONObject last = json;
				String key = null;
				for (int i = 0; i < keysToObjectList.length; i++) {
					key = keysToObjectList[i];
					last = (JSONObject)current;
					current = last.get(key);
				}
				return (JSONArray) current;
			}
		}catch (Exception e){
			LogPadrao.e(e);
		}
		return null;
	}
*/
//	private Iterator<?> parseResponse(
//			Map<String, Object> responseOutput
//	){
//		try {
//			JSONObject json = new FrameworkJSONTokener(reader).nextJSONObject();
//			desserializeJsonObject(json, responseOutput);
//			if (prototype != null) {
//				@SuppressWarnings({ "unchecked", "rawtypes" })
//				JsonToObjectIterator iter = new JsonToObjectIterator(
//						reader,
//						prototype
//				);
//				return iter;
//			}
//			return null;
//		} catch (JSONException e) {
//			LogPadrao.e(e);
//			throw new RuntimeException(e);
//		}
//	}

//	private static JSONArray extractJSONArray (
//			JSONObject json,
//			String keysToObjectList[]
//	)
//			throws JSONException
//	{
//		JSONObject current = json;
//		for (int i = 0; i < keysToObjectList.length -1; i++){
//			current = current.getJSONObject(keysToObjectList[i]);
//		}
//		String key = keysToObjectList[keysToObjectList.length -1];
//		JSONArray array = current.getJSONArray(key);
//		current.remove(key);
//		return array;
//	}

	private static void desserializeJsonObject (JSONObject json, Map<String, Object> out)
			throws JSONException
	{
		@SuppressWarnings("unchecked")
		Iterator<String> it = json.keys();
		while (it.hasNext()){
			String key = it.next();
			out.put(
					key,
					desserializeJsonValue(json.get(key))
			);
		}
	}

	private static Object desserializeJsonValue (Object value)
			throws JSONException
	{
		if (value instanceof JSONArray)
			return desserializeJsonArray( (JSONArray)value );
		if (value instanceof JSONObject) {
			Map<String,Object> map = new HashMap<String, Object>();
			desserializeJsonObject( (JSONObject)value, map);
			return map;
		}
		return value;
	}

	private static List<Object> desserializeJsonArray(JSONArray jsonArray)
			throws JSONException
	{
		List<Object> list = new ArrayList<Object>();
		for (int i=0; i < jsonArray.length(); i++){
			list.add(desserializeJsonValue(jsonArray.get(i)));
		}
		return list;
	}


}
