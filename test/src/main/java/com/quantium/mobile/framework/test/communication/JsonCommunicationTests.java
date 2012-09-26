package com.quantium.mobile.framework.test.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.test.ActivityInstrumentationTestCase2;
import com.quantium.mobile.framework.FrameworkException;
import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.communication.GenericCommunication;
import com.quantium.mobile.framework.communication.JsonCommunication;
import com.quantium.mobile.framework.communication.SerializedCommunicationResponse;
import com.quantium.mobile.framework.test.gen.Author;
import com.quantium.mobile.framework.test.server.Echo;
import com.quantium.mobile.framework.test.server.RouterBean;
import com.quantium.mobile.framework.test.TestActivity;

public class JsonCommunicationTests extends
ActivityInstrumentationTestCase2<TestActivity> {

	private static final String URL = "http://10.0.2.2:9091/";
	private static final String PLAIN_TEXT_URL = URL + "text_plain.jsp";

	public JsonCommunicationTests() {
		super("com.quantium.mobile.framework.test", TestActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		clear();
	}

	private void clear() {
		GenericCommunication comm = new JsonCommunication();
		comm.setURL(URL);
		comm.setParameter(RouterBean.METHOD_PARAM, "clear");
		comm.setParameter(RouterBean.CLASSNAME_PARAM, Author.class.getSimpleName());
		comm.post();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		clear();
	}

	public void testJsonCommunication() {
		JsonCommunication jsonComm = new JsonCommunication();
		String param1 = "param1";
		String param2 = "param2";
		String val1 = "val1";
		String val2 = "val2";

		jsonComm.setURL(URL);
		Map<String,Object> params = jsonComm.getParameters();
		params.put(RouterBean.METHOD_PARAM, "echo");
		params.put(param1, val1);
		params.put(param2, val2);
		try {
			Map<String,Object> map = jsonComm.post().getResponseMap();
			Iterator<String> iterator = params.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				Object object = map.get(key);
				assertNotNull(key + " eh null", object);
				String value = object.toString();
				assertEquals (params.get(key), value);
			}
			Object obj = map.remove(Echo.ERROR_KEY);
			assertNull(obj);
			assertEquals(params.size() ,map.size());
		} catch (Exception e) {
			fail (LogPadrao.getStackTrace(e));
		}
	}

	public void testEnviarJson(){
		ArrayList<Author> list = new ArrayList<Author>();
		int count = 5;
		for(int i = 0; i< count; i++){
			Author a = new Author();
			a.setName(RandomStringUtils.randomAscii(25));
			list.add(a);
		}

		Iterator<Author> received = saveOnServer(list);

		comparaAuthors(list, received);
	}

	protected void comparaAuthors(ArrayList<Author> list, Iterator<Author> received) {
		if (received == null)
			fail ();
		received_loop:
		for(int i=0; i< list.size();i++){
			if(!received.hasNext())
				fail();
			Author receivedAuthor = received.next();
			for(Author a : list)
				if(a.getName().equals(receivedAuthor.getName()))
					continue received_loop;
			fail();
		}
	}

	protected Iterator<Author> saveOnServer(ArrayList<Author> list) {
		GenericCommunication comm = new JsonCommunication();
		comm.setURL(URL);
		comm.setParameter(RouterBean.METHOD_PARAM, "insert");
		comm.setParameter(RouterBean.CLASSNAME_PARAM, Author.class.getSimpleName());
		JSONArray jsonlist = new JSONArray();
		JSONObject objects = new JSONObject();
		JSONObject jsonBody = new JSONObject();
		try {
			for (Author author: list)
				jsonlist.put(new JSONObject(author.toMap()));
			objects.put("list", jsonlist);
			jsonBody.put("objects", objects);
		} catch (JSONException e) {
			fail(e.getMessage());
		}
		comm.setParameter("json", jsonBody.toString());

		Iterator<Author> received = null;
		try {
			SerializedCommunicationResponse resp = comm.post();
			Map<String, Object> responseMap = resp.getResponseMap();
			assertEquals("success", responseMap.get("status"));

			received = resp.getIterator(new Author(), "objects","list");

			@SuppressWarnings("unchecked")
			HashMap<String, Object> objectsMap =
					(HashMap<String, Object>) responseMap.get("objects");
			assertNotNull(objectsMap);
			assertEquals(list.size(), objectsMap.get("quantity"));
			// TODO resolver esta "feature"
			//      o objeto nao eh removido do json
//			assertNull(objectsMap.get("list"));

		} catch (FrameworkException e) {
			fail(e.getMessage());
		}
		return received;
	}

	public void testGetJson(){
		ArrayList<Author> list = new ArrayList<Author>();
		int count = 5;
		for(int i = 0; i< count; i++){
			Author a = new Author();
			a.setName(RandomStringUtils.randomAscii(25));
			list.add(a);
		}

		saveOnServer(list);

		GenericCommunication authorsDao = new JsonCommunication();
		authorsDao.setURL(URL);
		authorsDao.setParameter(RouterBean.METHOD_PARAM, "query");
		authorsDao.setParameter(RouterBean.CLASSNAME_PARAM, Author.class.getSimpleName());
		Iterator<Author> it = null;
		try {
			it = authorsDao.post().getIterator(new Author(), "list");
		} catch (FrameworkException e) {
			fail();
		}
		
		comparaAuthors(list, it);
	}

/*	public void testPlainTextCommunication(){
		SerializedCommunication comm = new JsonCommunication(){
			@Override
			public String getContentType() {
				return "text/plain";
			}
		};
		Map<String,Object> map = new HashMap<String, Object>();
		String param1 = "param1";
		String val1 = "val1";
		map.put(param1,val1);
		comm.setSerializedBodyData(map);
		comm.setURL(PLAIN_TEXT_URL);
		Map<String,Object> respMap = comm.post().getResponseMap();
		assertEquals(val1, respMap.get(param1));
	}
*/
}
