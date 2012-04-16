package br.com.cds.mobile.gerador.utils;

import java.util.Calendar;
import java.util.Date;


public class SQLiteUtils {


	private static final String FUNCAO_DATE = "datetime(%s)";

	/**
	 * formato YYYY-MM-DD HH:mm:SS
	 */
	private static final String FORMATO_DATA = "%4d-%2d-%2d %2d:%2d:%2d";

	// substrings com fim nao inclusivo
	private static int STRING_TO_DATE_ANO[] = {0,4};
	private static int STRING_TO_DATE_MES[] = {5,7};
	private static int STRING_TO_DATE_DIA[] = {8,10};
	private static int STRING_TO_DATE_HORAS[] = {11,13};
	private static int STRING_TO_DATE_MINUTOS[] = {14,16};
	private static int STRING_TO_DATE_SEGUNDOS[] = {17};

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

	public static String funcaoDate(String coluna){
		return String.format(FUNCAO_DATE,coluna);
	}

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

	@SuppressWarnings("deprecation")
	public static Date stringToDate(String dateString){
		return new Date(
				Integer.parseInt(dateString.substring(
						STRING_TO_DATE_ANO[0], STRING_TO_DATE_ANO[1]
				)) - 1900,
				Integer.parseInt(dateString.substring(
						STRING_TO_DATE_MES[0], STRING_TO_DATE_MES[1]
				)) -1 ,
				Integer.parseInt(dateString.substring(
						STRING_TO_DATE_DIA[0], STRING_TO_DATE_DIA[1]
				)),
				Integer.parseInt(dateString.substring(
						STRING_TO_DATE_HORAS[0], STRING_TO_DATE_HORAS[1]
				)),
				Integer.parseInt(dateString.substring(
						STRING_TO_DATE_MINUTOS[0], STRING_TO_DATE_MINUTOS[1]
				)),
				Integer.parseInt(dateString.substring(
						STRING_TO_DATE_SEGUNDOS[0], STRING_TO_DATE_SEGUNDOS[1]
				))
		);
	}

	public static int booleanToInteger(boolean b){
		return b ? 1 : 0;
	}

	public static boolean integerToBoolean(int i){
		return i!=0;
	}

	public static String parse(Object object) {
		if(CharSequence.class.isInstance(object))
			return object.toString();
		if(Number.class.isInstance(object))
				return object.toString();
		if(object instanceof Date)
			return dateToString((Date)object);
		if(Calendar.class.isInstance(object))
			return dateToString(((Calendar)object).getTime());
		if(object instanceof Boolean)
			return ""+booleanToInteger((Boolean)object);
		return null;
	}


}
