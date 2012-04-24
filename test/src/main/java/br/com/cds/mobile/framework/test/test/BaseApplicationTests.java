package br.com.cds.mobile.framework.test.test;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import br.com.cds.mobile.framework.BaseApplication;
import br.com.cds.mobile.framework.logging.LogPadrao;
import br.com.cds.mobile.framework.test.TestActivity;

public class BaseApplicationTests extends
		ActivityInstrumentationTestCase2<TestActivity> {

	public BaseApplicationTests() {
		super("br.com.cds.mobile.framework.test", TestActivity.class);
	}

	public void testActivity() {
		TestActivity activity = getActivity();
		assertNotNull(activity);
	}

	public void testContext() {
		Context ctx = BaseApplication.getContext();
		assertNotNull(ctx);
	}


	public void testLog() {
		
		LogPadrao.i("mess");
	}

}

