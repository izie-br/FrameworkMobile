package com.quantium.mobile.framework.communication;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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

public class JsonCommunicationResponse<T> implements ObjectListCommunicationResponse<T>{

	private String keysToObjectList[];
	private Reader reader;
	private JsonSerializable<?> prototype;

	private Map<String,Object> map;
	private Iterator<T> iterator;

	public JsonCommunicationResponse(Reader reader, String...keys){
		this.reader = reader;
		this.keysToObjectList = keys;
	}


	public void setPrototype(JsonSerializable<?> prototype) {
		this.prototype = prototype;
	}

//	public void setIterator(Iterator<?> iterator) {
//		this.iterator = iterator;
//	}

	@Override
	public Reader getReader() {
		return reader;
	}

	@SuppressWarnings("unchecked")
	private void checkOutput() {
		if (map == null && iterator == null) {
			map = new HashMap<String, Object>();
			iterator = (Iterator<T>) parseResponse(map);
		}
	}

	@Override
	public Map<String, Object> getResponseMap() {
		checkOutput();
		return map;
	}

	@Override
	public void setKeysToObjectList(String... keysToObject) {
		this.keysToObjectList = keysToObject;
	}

	@Override
	public Iterator<T> getIterator() {
		checkOutput();
		return iterator;
	}

	private Iterator<?> parseResponse(
			Map<String, Object> responseOutput
	){
		try {
			LogPadrao.d("squi");
			if(keysToObjectList != null && keysToObjectList.length >0) {
				JSONObject json = new FrameworkJSONTokener(reader).nextJSONObject();
				LogPadrao.d("json:: %s", json.toString());
				JSONArray array = extractJSONArrayAndMap(json, keysToObjectList, responseOutput);
				reader = new InputStreamReader(
						new ByteArrayInputStream(
								array.toString().getBytes("UTF-8")
						),
						"UTF-8"
				);
			}
			if (prototype != null) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				JsonToObjectIterator iter = new JsonToObjectIterator(
						reader,
						prototype
				);
				return iter;
			}
			return null;
		} catch (JSONException e) {
			LogPadrao.e(e);
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			// impossivel
			LogPadrao.e(e);
			throw new RuntimeException(e);
		}
	}

	private static JSONArray extractJSONArrayAndMap (
			JSONObject json,
			String keysToObjectList[],
			Map<String, Object> responseOutput
	)
			throws JSONException
	{
		JSONObject current = json;
		for (int i = 0; i < keysToObjectList.length -1; i++){
			current = current.getJSONObject(keysToObjectList[i]);
		}
		String key = keysToObjectList[keysToObjectList.length -1];
		JSONArray array = current.getJSONArray(key);
		current.remove(key);
		return array;
	}

	private static void jsonToMap (JSONObject json, Map<String, Object> out)
			throws JSONException
	{
		@SuppressWarnings("unchecked")
		Iterator<String> it = json.keys();
		while (it.hasNext()){
			String key = it.next();
			LogPadrao.d("%s encontrada", key);
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
			jsonToMap( (JSONObject)value, map);
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
