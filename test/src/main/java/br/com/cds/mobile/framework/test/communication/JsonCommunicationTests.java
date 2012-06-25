package br.com.cds.mobile.framework.test.communication;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.test.ActivityInstrumentationTestCase2;
import br.com.cds.mobile.framework.FrameworkException;
import br.com.cds.mobile.framework.comunication.HttpJsonDao;
import br.com.cds.mobile.framework.test.gen.Author;
import br.com.cds.mobile.framework.test.TestActivity;

public class JsonCommunicationTests extends
ActivityInstrumentationTestCase2<TestActivity> {

	private static final String URL = "http://10.0.2.2:8000/";
	private static final String URL_CLEAR = URL+"authors/clear/";

	public JsonCommunicationTests() {
		super("br.com.cds.mobile.framework.test", TestActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		requestTo(URL_CLEAR);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		requestTo(URL_CLEAR);
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
		HttpJsonDao<Author> authorsDao = new HttpJsonDao<Author>(new Author())
				.setURL(URL+"authors/save_json/")
				.setBodyKey("json")
				.setKeysToObjectArray("list")
				.setIterator(list.iterator());


		Iterator<Author> received = null;
		try {
			received = authorsDao.send();
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

		HttpJsonDao<Author> authorsDao = new HttpJsonDao<Author>(new Author())
				.setURL(URL + "authors/get_json/")
				.setKeysToObjectArray("list");
		Iterator<Author> it = null;
		try {
			it = authorsDao.send();
		} catch (FrameworkException e) {
			fail();
		}
		
		comparaAuthors(list, it);
	}

	protected void requestTo(String url) {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
		HttpConnectionParams.setSoTimeout(httpParameters, 3000);
		HttpClient httpclient = new DefaultHttpClient(httpParameters);
		HttpResponse response = null;

		HttpPost post = new HttpPost(url);
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
		try {
			response = httpclient.execute(post);
		} catch (IOException e){
			fail(e.getMessage());
		}
		assertEquals(200,response.getStatusLine().getStatusCode());
	}

}
