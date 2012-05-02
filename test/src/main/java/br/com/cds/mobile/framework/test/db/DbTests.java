package br.com.cds.mobile.framework.test.db;

import java.io.File;


import android.test.ActivityInstrumentationTestCase2;
import br.com.cds.mobile.framework.BaseApplication;
import br.com.cds.mobile.framework.test.TestActivity;
import br.com.cds.mobile.framework.test.db.DB;

public class DbTests extends ActivityInstrumentationTestCase2<TestActivity> {

	public DbTests() {
		super("br.com.cds.mobile.framework.test", TestActivity.class);
	}

	public void testCreateDb(){
		String path =
				BaseApplication.getContext().getApplicationInfo().dataDir +
				"/databases/" + DB.DB_NOME;
		File dbFile = new File(path);
		if(dbFile.exists())
			dbFile.delete();
		if(new File(path).exists())
			fail();
		DB.getDb();
	}


}
