package br.com.cds.mobile.framework.utils;

import java.util.Calendar;
import java.util.Date;

import br.com.cds.mobile.framework.config.Aplicacao;
import android.content.Context;
import android.widget.DatePicker;

public class AndroidUtils {

	public static int getResourceByName(Context context, String name){
		return context.getResources().getIdentifier(name, null, Aplicacao.getContext().getPackageName());
	}

	public static Date datePickerToDate(DatePicker dpData) {
		return DateUtil.stringToTimestamp(datePickerToString(dpData));
	}

	public static Date datePickerToDate(DatePicker dpData, boolean endOfDay) {
		String timestamp = datePickerToString(dpData);
		if (endOfDay) {
			timestamp += " 23:59:59";
		} else {
			timestamp += " 00:00:00";
		}
		return DateUtil.stringToTimestamp(timestamp);
	}

	//TODO  refazer isto com stringBuffer
	public static String datePickerToString(DatePicker date) {
		String dia = StringUtil.lpad(date.getDayOfMonth(), 2, '0');
		String mes = StringUtil.lpad(date.getMonth() + 1, 2, '0');
		String ano = StringUtil.lpad(date.getYear(), 4, '0');
		return ano + "-" + mes + "-" + dia;
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
