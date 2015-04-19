package com.quantium.mobile.framework.libtests;

import com.quantium.mobile.framework.utils.DateUtil;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DateUtilsTest {

    @SuppressWarnings("deprecation")
    @Test
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
