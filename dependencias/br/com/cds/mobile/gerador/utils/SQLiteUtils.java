package br.com.cds.mobile.gerador.utils;

import java.util.Date;


public class SQLiteUtils {

	/**
	 * formato YYYY-MM-DD HH:mm:SS
	 */
	private static final String FORMATO_DATA = "%4d-%2d-%2d %2d:%2d:%2d";

//	public static String dateToString(Calendar cal){
//		return String.format(FORMATO_DATA,
//				cal.get(Calendar.YEAR),
//				cal.get(Calendar.MONTH) + 1,
//				cal.get(Calendar.DAY_OF_MONTH),
//				cal.get(Calendar.HOUR_OF_DAY),
//				cal.get(Calendar.MINUTE),
//				cal.get(Calendar.SECOND)
//		);
//	}

	@SuppressWarnings("deprecation")
	public static String dateToString(Date date){
		return String.format(FORMATO_DATA,
				date.getYear()+1900,
				date.getMonth()+1,
				date.getDate(),
				date.getHours(),
				date.getMinutes(),
				date.getSeconds()
		);
	}


}
