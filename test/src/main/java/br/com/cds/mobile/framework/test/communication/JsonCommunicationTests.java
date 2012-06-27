package br.com.cds.mobile.framework.test.communication;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang.RandomStringUtils;

import android.test.ActivityInstrumentationTestCase2;
import br.com.cds.mobile.framework.FrameworkException;
import br.com.cds.mobile.framework.comunication.HttpJsonDao;
import br.com.cds.mobile.framework.test.gen.Author;
import br.com.cds.mobile.framework.test.TestActivity;

public class JsonCommunicationTests extends
ActivityInstrumentationTestCase2<TestActivity> {

	private static final String URL = "http://10.0.2.2:9090/";

	private static final String CLASSNAME_PARAM = "classname";
	private static final String METHOD_PARAM = "method";
	
	public JsonCommunicationTests() {
		super("br.com.cds.mobile.framework.test", TestActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		new HttpJsonDao<Author>(new Author())
			.setURL(URL)
			.setParameter(METHOD_PARAM, "clear")
			.setParameter(CLASSNAME_PARAM, Author.class.getSimpleName())
			.send();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		new HttpJsonDao<Author>(new Author())
		.setURL(URL)
			.setParameter(METHOD_PARAM, "clear")
			.setParameter(CLASSNAME_PARAM, Author.class.getSimpleName())
		.send();
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
			new HttpJsonDao<Author>(new Author())
				.setURL(URL)
				.setParameter(METHOD_PARAM, "insert")
				.setParameter(CLASSNAME_PARAM, Author.class.getSimpleName())
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
				.setURL(URL)
				.setParameter(METHOD_PARAM, "query")
				.setParameter(CLASSNAME_PARAM, Author.class.getSimpleName())
				.setKeysToObjectArray("list");
		Iterator<Author> it = null;
		try {
			it = authorsDao.send();
		} catch (FrameworkException e) {
			fail();
		}
		
		comparaAuthors(list, it);
	}

}
