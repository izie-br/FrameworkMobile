package com.quantium.mobile.framework.libandroidtest.communication;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.communication.*;
import com.quantium.mobile.framework.libandroidtest.User;
import com.quantium.mobile.framework.libandroidtest.UserMapDAO;
import com.quantium.mobile.framework.libandroidtest.server.Array;
import com.quantium.mobile.framework.libandroidtest.server.Echo;
import com.quantium.mobile.framework.utils.StringUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class JsonCommunicationIT {

    public static final String METHOD_PARAM = "__method__";
    public static final String CLASSNAME_PARAM = "classname";
    private static final String URL = "http://127.0.0.1:9091";
    private static final String PLAIN_TEXT_URL = URL + "/text_plain";
    private static final DAO<User> USER_DAO = new UserMapDAO();

    @Before
    public void setUp() throws Exception {
        clear();
    }

    private void clear() {
        GenericCommunication comm = new JsonCommunication();
        comm.setURL(URL + "/clear");
        comm.setParameter(CLASSNAME_PARAM, User.class.getSimpleName());
        comm.post();
    }

    @After
    public void tearDown() throws Exception {
        //clear();
    }

    @Test
    public void testJsonCommunicationPOST() {
        JsonCommunication jsonComm = new JsonCommunication();
        String param1 = "param1";
        String param2 = "param2";
        String val1 = "val1";
        String val2 = "val2";

        jsonComm.setURL(URL + "/echo");
        Map<String, Object> params = jsonComm.getParameters();
        params.put(param1, val1);
        params.put(param2, val2);
        try {
            Map<String, Object> map = jsonComm.post().getResponseMap();
            Iterator<String> iterator = params.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object object = map.get(key);
                assertNotNull(key + " eh null", object);
                String value = object.toString();
                assertEquals(params.get(key), value);
            }
            assertEquals("POST", map.get(METHOD_PARAM));
            Object obj = map.remove(Echo.ERROR_KEY);
            assertNull(obj);
            assertEquals(params.size() + 1, map.size());
        } catch (Exception e) {
            fail(StringUtil.getStackTrace(e));
        }
    }

    @Test
    public void testJsonCommunicationGET() {
        JsonCommunication jsonComm = new JsonCommunication();
        String param1 = "param1";
        String param2 = "param2";
        String val1 = "val1";
        String val2 = "val2";

        jsonComm.setURL(URL + "/echo");
        Map<String, Object> params = jsonComm.getParameters();
        params.put(param1, val1);
        params.put(param2, val2);
        try {
            Map<String, Object> map = jsonComm.get().getResponseMap();
            Iterator<String> iterator = params.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object object = map.get(key);
                assertNotNull(key + " eh null", object);
                String value = object.toString();
                assertEquals(params.get(key), value);
            }
            assertEquals("GET", map.get(METHOD_PARAM));
            Object obj = map.remove(Echo.ERROR_KEY);
            assertNull(obj);
            assertEquals(params.size() + 1, map.size());
        } catch (Exception e) {
            fail(StringUtil.getStackTrace(e));
        }
    }

    @Test
    public void testJsonCommunicationPUT() {
        JsonCommunication jsonComm = new JsonCommunication();
        String param1 = "param1";
        String param2 = "param2";
        String val1 = "val1";
        String val2 = "val2";

        jsonComm.setURL(URL + "/echo");
        Map<String, Object> params = jsonComm.getParameters();
        params.put(param1, val1);
        params.put(param2, val2);
        try {
            Map<String, Object> map = jsonComm.put().getResponseMap();
            Iterator<String> iterator = params.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object object = map.get(key);
                assertNotNull(key + " eh null", object);
                String value = object.toString();
                assertEquals(params.get(key), value);
            }
            assertEquals("PUT", map.get(METHOD_PARAM));
            Object obj = map.remove(Echo.ERROR_KEY);
            assertNull(obj);
            assertEquals(params.size() + 1, map.size());
        } catch (Exception e) {
            fail(StringUtil.getStackTrace(e));
        }
    }

    @Test
    public void testJsonCommunicationDELETE() {
        JsonCommunication jsonComm = new JsonCommunication();
        String param1 = "param1";
        String param2 = "param2";
        String val1 = "val1";
        String val2 = "val2";

        jsonComm.setURL(URL + "/echo");
        Map<String, Object> params = jsonComm.getParameters();
        params.put(param1, val1);
        params.put(param2, val2);
        try {
            Map<String, Object> map = jsonComm.delete().getResponseMap();
            Iterator<String> iterator = params.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object object = map.get(key);
                assertNotNull(key + " eh null", object);
                String value = object.toString();
                assertEquals(params.get(key), value);
            }
            assertEquals("DELETE", map.get(METHOD_PARAM));
            Object obj = map.remove(Echo.ERROR_KEY);
            assertNull(obj);
            assertEquals(params.size() + 1, map.size());
        } catch (Exception e) {
            fail(StringUtil.getStackTrace(e));
        }
    }

    @Test
    public void testJsonArrayResponse() {
        JsonCommunication jsonComm = new JsonCommunication();

        jsonComm.setURL(URL + "/array");
        try {
            Map<String, Object> map = jsonComm.post().getResponseMap();
            assertEquals(1, map.size());
            Object listObject = map.get(null);
            if (listObject instanceof List) {
                List<?> list = (List<?>) listObject;
                assertEquals(Array.ARRAY.length, list.size());
                for (Object obj : Array.ARRAY) {
                    assertTrue(list.contains(obj));
                }
            } else {
                fail();
            }
        } catch (Exception e) {
            fail(StringUtil.getStackTrace(e));
        }
    }

    @Test
    public void testEnviarJson() {
        ArrayList<User> list = new ArrayList<User>();
        int count = 5;
        for (int i = 0; i < count; i++) {
            User a = new User();
            a.setName(RandomStringUtils.randomAscii(25));
            list.add(a);
        }

        Iterator<User> received = saveOnServer(list);

        comparaUsers(list, received);
    }

    protected void comparaUsers(ArrayList<User> list, Iterator<User> received) {
        if (received == null)
            fail();
        received_loop:
        for (int i = 0; i < list.size(); i++) {
            if (!received.hasNext())
                fail();
            User receivedUser = received.next();
            for (User a : list)
                if (a.getName().equals(receivedUser.getName()))
                    continue received_loop;
            fail();
        }
    }

    protected Iterator<User> saveOnServer(ArrayList<User> list) {
        GenericCommunication comm = new JsonCommunication();
        comm.setURL(URL + "/insert");
        comm.setParameterSerializer(new InnerJsonParametersSerializer());
        comm.setParameter(CLASSNAME_PARAM, User.class.getSimpleName());
        HashMap<String, Object> objects = new HashMap<String, Object>();
        HashMap<String, Object> jsonBody = new HashMap<String, Object>();
        objects.put("list", list);
        jsonBody.put("objects", objects);
        comm.setParameter("json", jsonBody);

        Iterator<User> received = null;
        try {
            SerializedCommunicationResponse resp = comm.post();
            Map<String, Object> responseMap = resp.getResponseMap();
            assertEquals("success", responseMap.get("status"));

            received = resp.getIterator(USER_DAO, "objects", "list");

            @SuppressWarnings("unchecked")
            HashMap<String, Object> objectsMap =
                    (HashMap<String, Object>) responseMap.get("objects");
            assertNotNull(objectsMap);
            assertEquals(list.size(), objectsMap.get("quantity"));
            // TODO resolver esta "feature"
            //      o objeto nao eh removido do json
//			assertNull(objectsMap.get("list"));

        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
        return received;
    }

    @Test
    public void testGetJson() {
        ArrayList<User> list = new ArrayList<User>();
        int count = 5;
        for (int i = 0; i < count; i++) {
            User a = new User();
            a.setName(RandomStringUtils.randomAscii(25));
            list.add(a);
        }

        saveOnServer(list);

        GenericCommunication authorsDao = new JsonCommunication();
        authorsDao.setURL(URL + "/query");
        authorsDao.setParameter(CLASSNAME_PARAM, User.class.getSimpleName());
        Iterator<User> it = null;
        try {
            it = authorsDao.get().getIterator(USER_DAO, "list");
        } catch (RuntimeException e) {
            fail(StringUtil.getStackTrace(e));
        }

        comparaUsers(list, it);
    }

    @Test
    public void testJsonPlainTextCommunication() {
        GenericCommunication comm = new JsonCommunication();
        comm.setContentType("application/json");
        comm.setParameterSerializer(new JsonParametersSerializer());
        Map<String, Object> map = comm.getParameters();
        String param1 = "param1";
        String val1 = "val1";
        map.put(param1, val1);
        comm.setURL(PLAIN_TEXT_URL);
        Map<String, Object> respMap = comm.post().getResponseMap();
        assertEquals(val1, respMap.get(param1));
    }

}
