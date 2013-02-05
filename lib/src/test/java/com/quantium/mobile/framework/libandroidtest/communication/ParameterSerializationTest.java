package com.quantium.mobile.framework.libandroidtest.communication;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.quantium.mobile.framework.communication.IndexedKeyParametersSerializer;
import com.quantium.mobile.framework.communication.InnerJsonParametersSerializer;
import com.quantium.mobile.framework.communication.JsonParametersSerializer;
import com.quantium.mobile.framework.communication.ParametersSerializer;
import com.quantium.mobile.framework.utils.StringUtil;

public class ParameterSerializationTest {

	private static final String key1 = "key1";
	private static final String val1 = "val1";
	private static final String key2 = "key2";
	private static final String key21 = "key21";
	private static final String key22 = "key22";
	private static final String val22 = "val22";
	private static final String key211 = "key211";
	private static final String val211 = "val211";

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testJsonSerialization(){
		Map<String,Object> map = new HashMap<String, Object>();
		map.put(key1, val1);
		Map<String,Object> map2 = new HashMap<String, Object>();
		Map<String,Object> map21 = new HashMap<String, Object>();
		map21.put(key211, val211);
		map2.put(key21, map21);
		map2.put(key22, val22);
		map.put(key2, map2);
		ParametersSerializer serializer = new JsonParametersSerializer();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			serializer.getEntity(map).writeTo(bout);
			String jsonstr = new String(bout.toByteArray(),
					StringUtil.DEFAULT_ENCODING);
			JSONObject json = new JSONObject(jsonstr);
			assertEquals(val1, json.getString(key1));
			JSONObject json2 = json.getJSONObject(key2);
			assertEquals(val22, json2.get(key22));
			JSONObject json21 = json2.getJSONObject(key21);
			assertEquals(val211, json21.getString(key211));
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void testInnerJsonParameterSerialization(){
		Map<String,Object> map = new HashMap<String, Object>();
		map.put(key1, val1);
		Map<String,Object> map2 = new HashMap<String, Object>();
		Map<String,Object> map21 = new HashMap<String, Object>();
		map21.put(key211, val211);
		map2.put(key21, map21);
		map2.put(key22, val22);
		map.put(key2, map2);
		ParametersSerializer serializer = new InnerJsonParametersSerializer();
		List<NameValuePair> params = serializer.serialize(map);
		for (NameValuePair pair : params){
			if (pair.getName().equals(key1)){
				assertEquals(val1, pair.getValue());
			} else if (pair.getName().equals(key2)){
				try {
					JSONObject json2 = new JSONObject(pair.getValue());
					assertEquals(val22, json2.get(key22));
					JSONObject json21 = json2.getJSONObject(key21);
					assertEquals(val211, json21.getString(key211));
				} catch (JSONException e) {
					fail(e.getMessage());
				}
			} else {
				fail();
			}
		}
	}

	@Test
	public void testIndexedKeysParameterSerialization(){
		Map<String,Object> map = new HashMap<String, Object>();
		map.put(key1, val1);
		Map<String,Object> map2 = new HashMap<String, Object>();
		Map<String,Object> map21 = new HashMap<String, Object>();
		map21.put(key211, val211);
		map2.put(key21, map21);
		map2.put(key22, val22);
		map.put(key2, map2);
		ParametersSerializer serializer =
				new IndexedKeyParametersSerializer("%1$s[%2$s]");
		List<NameValuePair> params = serializer.serialize(map);
		for (NameValuePair pair : params){
			if (pair.getName().equals(key1)){
				assertEquals(val1, pair.getValue());
			}
			else if (pair.getName().equals( (key2+'['+key22+']') )){
				assertEquals(val22, pair.getValue());
			}
			else if (pair.getName().equals(key2+'['+key21+"]["+key211+']')){
				assertEquals(val211, pair.getValue());
			} else {
				fail("k: "+pair.getName()+ ", v="+pair.getValue());
			}
		}
	}

}
