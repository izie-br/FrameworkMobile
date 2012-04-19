package br.com.cds.mobile.framework.utils;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.widget.DatePicker;

public class AndroidUtils {

	public static int getResourceByName(Context context, String name){
		return context.getResources().getIdentifier(name, null, context.getPackageName());
	}

	public static Date datePickerToDate(DatePicker dpData) {
		return DateUtil.stringToDate(datePickerToString(dpData));
	}

	public static String datePickerToString(DatePicker date) {
		return String.format( DateUtil.FORMATO_DATE, date.getDayOfMonth(),date.getMonth()+1,date.getYear());
	}

	@SuppressWarnings("deprecation")
	public static void preencheDatePicker(DatePicker datePicker, Date data) {
		if (data == null) {
			return;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(data);
		datePicker.init(cal.getTime().getYear() + 1900, cal.getTime().getMonth(), cal.getTime().getDate(), null);
	}

}
