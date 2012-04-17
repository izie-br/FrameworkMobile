package br.com.cds.mobile.framework.utils;

import java.util.Calendar;
import java.util.Date;


public class SQLiteUtils {


//	private static final String FUNCAO_DATE = "datetime(%s)";

	/**
	 * formato YYYY-MM-DD HH:mm:SS
	 */
	private static final String FORMATO_TIMESTAMP = "%4d-%2d-%2d %2d:%2d:%2d";
	private static final String FORMATO_DATE = "%4d-%2d-%2d";

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

	@SuppressWarnings("deprecation")
	public static String timestampToString(Date date){
		return String.format(FORMATO_TIMESTAMP,
				date.getYear()+1900,
				date.getMonth()+1,
				date.getDate(),
				date.getHours(),
				date.getMinutes(),
				date.getSeconds()
		);
	}

	@SuppressWarnings("deprecation")
	public static String dateToString(Date date){
		return String.format(FORMATO_DATE,
				date.getYear()+1900,
				date.getMonth()+1,
				date.getDate()
		);
	}

	@SuppressWarnings("deprecation")
	public static Date stringToDate(String dateString){
		int ano = Integer.parseInt(dateString.substring(
				STRING_TO_DATE_ANO[0], STRING_TO_DATE_ANO[1]
		)) - 1900;
		int mes = Integer.parseInt(dateString.substring(
				STRING_TO_DATE_MES[0], STRING_TO_DATE_MES[1]
		)) -1;
		int dia = Integer.parseInt(dateString.substring(
				STRING_TO_DATE_DIA[0], STRING_TO_DATE_DIA[1]
		));
		int horas = 0, minutos = 0, segundos = 0;
		if(dateString.length()>=FORMATO_TIMESTAMP.length()){
			horas = Integer.parseInt(dateString.substring(
					STRING_TO_DATE_HORAS[0], STRING_TO_DATE_HORAS[1]
			));
			minutos = Integer.parseInt(dateString.substring(
					STRING_TO_DATE_MINUTOS[0], STRING_TO_DATE_MINUTOS[1]
			));
			segundos = Integer.parseInt(dateString.substring(
					STRING_TO_DATE_SEGUNDOS[0], STRING_TO_DATE_SEGUNDOS[1]
			));
		}
		return new Date( ano, mes, dia, horas, minutos, segundos );
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
			return timestampToString((Date)object);
		if(Calendar.class.isInstance(object))
			return timestampToString(((Calendar)object).getTime());
		if(object instanceof Boolean)
			return ""+booleanToInteger((Boolean)object);
		return null;
	}


}
