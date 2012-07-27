package com.quantium.mobile.framework.test.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONArray;

import android.test.ActivityInstrumentationTestCase2;
import com.quantium.mobile.framework.FrameworkException;
import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.communication.HttpJsonDao;
import com.quantium.mobile.framework.communication.JsonCommunication;
import com.quantium.mobile.framework.communication.ObjectListCommunicationResponse;
import com.quantium.mobile.framework.test.gen.Author;
import com.quantium.mobile.framework.test.TestActivity;

public class JsonCommunicationTests extends
ActivityInstrumentationTestCase2<TestActivity> {

	private static final String URL = "http://10.0.2.2:9091/";

	private static final String CLASSNAME_PARAM = "classname";
	private static final String METHOD_PARAM = "method";
	
	public JsonCommunicationTests() {
		super("com.quantium.mobile.framework.test", TestActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		HttpJsonDao<Author> dao = new HttpJsonDao<Author>(new Author());
		dao.setURL(URL);
		dao.setParameter(METHOD_PARAM, "clear");
		dao.setParameter(CLASSNAME_PARAM, Author.class.getSimpleName());
		dao.send();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		HttpJsonDao<Author> dao = new HttpJsonDao<Author>(new Author());
		dao.setURL(URL);
		dao.setParameter(METHOD_PARAM, "clear");
		dao.setParameter(CLASSNAME_PARAM, Author.class.getSimpleName());
		dao.send();
	}

	public void testJsonCommunication() {
		JsonCommunication jsonComm = new JsonCommunication();
		String param1 = "param1";
		String param2 = "param2";
		String val1 = "val1";
		String val2 = "val2";

		jsonComm.setURL(URL);
		HashMap<String,String> params = new HashMap<String, String>();
		params.put(METHOD_PARAM, "echo");
		params.put(param1, val1);
		params.put(param2, val2);
		jsonComm.setParameters(params);
		try {
			Map<String,Object> map = jsonComm.send().getResponseMap();
			Iterator<String> iterator = params.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				Object object = map.get(key);
				assertNotNull(key + " eh null", object);
				String value = object.toString();
				assertEquals (params.get(key), value);
			}
			assertEquals(map.size(), params.size());
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
		HttpJsonDao<Author> authorsDao =
			new HttpJsonDao<Author>(new Author());
			authorsDao.setURL(URL);
			authorsDao.setParameter(METHOD_PARAM, "insert");
			authorsDao.setParameter(CLASSNAME_PARAM, Author.class.getSimpleName());
			authorsDao.setSerializedBodyParameter("json");
			authorsDao.setKeysToObjectList("objects","list");
			authorsDao.setIterator(list.iterator());

		Iterator<Author> received = null;
		try {
			ObjectListCommunicationResponse<Author> resp = authorsDao.send();
			Map<String, Object> responseMap = resp.getResponseMap();
			assertEquals("success", responseMap.get("status"));

			@SuppressWarnings("unchecked")
			HashMap<String, Object> objectsMap =
					(HashMap<String, Object>) responseMap.get("objects");
			assertNotNull(objectsMap);
			assertEquals(list.size(), objectsMap.get("quantity"));
			assertNull(objectsMap.get("list"));

			received = resp.getIterator();
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

		HttpJsonDao<Author> authorsDao = new HttpJsonDao<Author>(new Author());
		authorsDao.setURL(URL);
		authorsDao.setParameter(METHOD_PARAM, "query");
		authorsDao.setParameter(CLASSNAME_PARAM, Author.class.getSimpleName());
		authorsDao.setKeysToObjectList("list");
		Iterator<Author> it = null;
		try {
			it = authorsDao.send().getIterator();
		} catch (FrameworkException e) {
			fail();
		}
		
		comparaAuthors(list, it);
	}

}
