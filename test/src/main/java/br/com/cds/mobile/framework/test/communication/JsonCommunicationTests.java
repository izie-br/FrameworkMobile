package br.com.cds.mobile.framework.test.communication;


import java.util.Iterator;

import android.test.ActivityInstrumentationTestCase2;
import br.com.cds.mobile.framework.FrameworkException;
import br.com.cds.mobile.framework.comunication.HttpJsonDao;
import br.com.cds.mobile.framework.test.Author;
import br.com.cds.mobile.framework.test.TestActivity;

public class JsonCommunicationTests extends
ActivityInstrumentationTestCase2<TestActivity> {

	public JsonCommunicationTests() {
		super("br.com.cds.mobile.framework.test", TestActivity.class);
	}

	public void testGetJson(){
		HttpJsonDao<Author> authorsDao = new HttpJsonDao<Author>(
				"http://10.0.2.2:8000/", "authors/get_json/", "",
				new Author()
		);
		Iterator<Author> authorIterator = null;
		try {
			authorIterator = authorsDao.query(null, new Author());
		} catch (FrameworkException e) {
			throw new RuntimeException(e);
		}
		if(!authorIterator.hasNext())
			fail();
		authorIterator.next();
		if(!authorIterator.hasNext())
			fail();
		authorIterator.next();
	}

}
