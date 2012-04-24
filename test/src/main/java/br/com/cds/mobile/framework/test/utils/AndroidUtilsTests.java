package br.com.cds.mobile.framework.test.utils;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import br.com.cds.mobile.framework.BaseApplication;
import br.com.cds.mobile.framework.test.TestActivity;
import br.com.cds.mobile.framework.test.R;
import br.com.cds.mobile.framework.utils.AndroidUtils;

public class AndroidUtilsTests extends 
		ActivityInstrumentationTestCase2<TestActivity> {

	public AndroidUtilsTests(){
		super("br.com.cds.mobile.framework.test", TestActivity.class);
	}

	public void testGetResourceByName(){
		Context ctx = BaseApplication.getContext();
		int id = R.id.testTextView;
		String resName = "id/testTextView";
		int utilId = AndroidUtils.getResourceByName(ctx, resName);
		assertEquals(id, utilId);
	}

}
