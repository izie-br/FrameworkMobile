package com.quantium.mobile.framework.communication;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.FrameworkJSONTokener;
import com.quantium.mobile.framework.MapToObjectIterator;
import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.utils.JSONUtils;

public class JsonCommunicationResponse implements SerializedCommunicationResponse{

	private Reader reader;

	private Object json;

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
				FrameworkJSONTokener tokener = new FrameworkJSONTokener(reader);
				char c = tokener.nextClean();
				tokener.back();
				json =
					(c == '{') ?
						tokener.nextJSONObject() :
					(c == '[') ?
						tokener.nextJSONArray()  :
					// default
						null;
			} catch (JSONException e) {
				LogPadrao.e(e);
			}
		}
	}

	@Override
	public Map<String, Object> getResponseMap() {
		checkOutput();
		if (json instanceof JSONObject)
			return JSONUtils.desserializeJsonObject((JSONObject) json);
		if (json instanceof JSONArray){
			Object list = JSONUtils.desserializeJsonArray((JSONArray) json);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(null, list);
			return map;
		}
		return null;
	}

//	public JSONObject getJson(){
//		return json;
//	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> Iterator<T> getIterator(DAO<T> dao, String... keysToObjectList) {
		if (dao == null)
			return null;
		checkOutput();
		try {
			if(keysToObjectList != null && keysToObjectList.length >0) {
				int l = keysToObjectList.length;
				Object current = json;
				JSONObject last = (JSONObject) json;
				String key = null;
				for (int i = 0; i < l; i++) {
					key = keysToObjectList[i];
					last = (JSONObject)current;
					current = last.get(key);
				}
				
				if (key != null)
					last.remove(key);
				return new MapToObjectIterator(
						new StringReader(current.toString()),
						dao
				);
			} else if (json instanceof JSONArray) {
				return new MapToObjectIterator(
						new StringReader(json.toString()),
						dao
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

}
