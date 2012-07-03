package com.quantium.mobile.framework.test.utils;

import java.util.Date;
import com.quantium.mobile.framework.utils.DateUtil;
import android.test.ActivityInstrumentationTestCase2;
import com.quantium.mobile.framework.test.TestActivity;

public class DateUtilsTests extends 
        ActivityInstrumentationTestCase2<TestActivity>
    {

    public DateUtilsTests(){
        super("com.quantium.mobile.framework.test", TestActivity.class);
    }


    public void testStringToDate() {
        Date now = new Date();
        String dateStr = DateUtil.timestampToString(now);
        Date nowCopy = DateUtil.stringToDate(dateStr);
        assertEquals(now.getYear(), nowCopy.getYear());
        assertEquals(now.getMonth(), nowCopy.getMonth());
        assertEquals(now.getDate(), nowCopy.getDate());
        assertEquals(now.getHours(), nowCopy.getHours());
        assertEquals(now.getMinutes(), nowCopy.getMinutes());
        assertEquals(now.getSeconds(), nowCopy.getSeconds());
    }

}
