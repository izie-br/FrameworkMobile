package com.quantium.mobile.framework.test.db;

import android.test.ActivityInstrumentationTestCase2;
import com.quantium.mobile.framework.test.TestActivity;

public class DbTests extends ActivityInstrumentationTestCase2<TestActivity> {

    public DbTests() {
        super("com.quantium.mobile.framework.test", TestActivity.class);
    }

//	public void testCreateDb(){
//		String path =
//				BaseApplication.getContext().getApplicationInfo().dataDir +
//				"/databases/" + DB.DB_NOME;
//		File dbFile = new File(path);
//		if(dbFile.exists())
//			dbFile.delete();
//		if(new File(path).exists())
//			fail();
//		DB.getDb();
//	}


}
