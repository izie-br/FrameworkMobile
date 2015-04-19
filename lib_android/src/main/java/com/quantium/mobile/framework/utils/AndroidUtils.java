package com.quantium.mobile.framework.utils;

import android.content.Context;
import android.widget.DatePicker;

import java.util.Date;

public class AndroidUtils {

    public static int getResourceByName(Context context, String name) {
        return context.getResources().getIdentifier(name, null, context.getPackageName());
    }

    @SuppressWarnings("deprecation")
    public static Date datePickerToDate(DatePicker dpData) {
        return new Date(dpData.getYear() - 1900, dpData.getMonth(), dpData.getDayOfMonth());
    }

//	private static String datePickerToString(DatePicker date) {
//		return String.format( DateUtil.FORMATO_DATE, date.getDayOfMonth(),date.getMonth()+1,date.getYear());
//	}

    @SuppressWarnings("deprecation")
    public static void preencheDatePicker(DatePicker datePicker, Date data) {
        if (data == null) {
            return;
        }
        datePicker.init(data.getYear() + 1900, data.getMonth(), data.getDate(), null);
    }

}
