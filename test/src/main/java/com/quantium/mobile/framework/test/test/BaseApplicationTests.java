package com.quantium.mobile.framework.test.test;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import com.quantium.mobile.framework.BaseApplication;
import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.test.TestActivity;

public class BaseApplicationTests extends
        ActivityInstrumentationTestCase2<TestActivity> {

    public BaseApplicationTests() {
        super("com.quantium.mobile.framework.test", TestActivity.class);
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

