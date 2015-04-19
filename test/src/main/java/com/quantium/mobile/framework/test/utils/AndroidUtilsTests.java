package com.quantium.mobile.framework.test.utils;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.DatePicker;
import com.quantium.mobile.framework.BaseApplication;
import com.quantium.mobile.framework.test.R;
import com.quantium.mobile.framework.test.TestActivity;
import com.quantium.mobile.framework.utils.AndroidUtils;

import java.util.Date;

public class AndroidUtilsTests extends
        ActivityInstrumentationTestCase2<TestActivity> {

    boolean lock = true;

    public AndroidUtilsTests() {
        super("com.quantium.mobile.framework.test", TestActivity.class);
    }

    public void testGetResourceByName() {
        Context ctx = BaseApplication.getContext();
        int id = R.id.testTextView;
        String resName = "id/testTextView";
        int utilId = AndroidUtils.getResourceByName(ctx, resName);
        assertEquals(id, utilId);
    }

    public void testDatePicker() {
        final DatePicker datePicker = (DatePicker) getActivity()
                .findViewById(R.id.testDatePicker);
        final Date now = new Date();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                AndroidUtils.preencheDatePicker(datePicker, now);
                lock = false;
            }
        };
        getActivity().runOnUiThread(runnable);
        while (lock) {
            Thread.yield();
        }
        Date dpDate = AndroidUtils.datePickerToDate(datePicker);
        assertEquals(now.getDate(), dpDate.getDate());
        assertEquals(now.getMonth(), dpDate.getMonth());
        assertEquals(now.getYear(), dpDate.getYear());
    }

}
