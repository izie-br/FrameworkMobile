package com.quantium.mobile.framework.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {


    /**
     * formato YYYY-MM-DD HH:mm:SS
     */
    public static final String FORMATO_TIMESTAMP = "%04d-%02d-%02d %02d:%02d:%02d";
    public static final String FORMATO_DATE = "%04d-%02d-%02d";

    // substrings com fim nao inclusivo
//	private static int STRING_TO_DATE_ANO[] = {0,4};
//	private static int STRING_TO_DATE_MES[] = {5,7};
//	private static int STRING_TO_DATE_DIA[] = {8,10};
//	private static int STRING_TO_DATE_HORAS[] = {11,13};
//	private static int STRING_TO_DATE_MINUTOS[] = {14,16};
//	private static int STRING_TO_DATE_SEGUNDOS[] = {17,19};
//
//	public static final int FORMATO_TIMESTAMP_LENGTH =
//		STRING_TO_DATE_SEGUNDOS[1];

    public static Date adicionaMeses(Date date, int meses) {
        return add(date, Calendar.MONTH, meses);
    }

    private static Date add(Date date, int field, int toIncrement) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, toIncrement);
        return calendar.getTime();
    }

    private static Date subtract(Date date, int field, int toIncrement) {
        return add(date, field, toIncrement * -1);
    }

    public static Date adicionaDias(Date date, int dias) {
        return add(date, Calendar.DATE, dias);
    }

    public static Date subtraiDias(Date date, int dias) {
        return subtract(date, Calendar.DATE, dias);
    }

    public static Date subtraiMeses(Date date, int meses) {
        return subtract(date, Calendar.MONTH, meses);
    }

    public static Date adicionaHoras(Date date, int horas) {
        return add(date, Calendar.HOUR_OF_DAY, horas);
    }

    public static Date subtraiHoras(Date date, int horas) {
        return subtract(date, Calendar.HOUR_OF_DAY, horas);
    }

    public static Date adicionaMinutos(Date date, int minutos) {
        return add(date, Calendar.MINUTE, minutos);
    }

    public static Date subtraiMinutos(Date date, int minutos) {
        return subtract(date, Calendar.MINUTE, minutos);
    }

    public static Date adicionaSegundos(Date date, int segundos) {
        return add(date, Calendar.SECOND, segundos);
    }

    public static Date subtraiSegundos(Date date, int segundos) {
        return subtract(date, Calendar.SECOND, segundos);
    }

    public static Date adicionaMSs(Date date, int mss) {
        return add(date, Calendar.MILLISECOND, mss);
    }

    public static Date subtraiMSs(Date date, int mss) {
        return subtract(date, Calendar.MILLISECOND, mss);
    }

    @SuppressWarnings("deprecation")
    public static String timestampToString(Date date) {
        if (date == null)
            return null;
        return String.format(FORMATO_TIMESTAMP,
                date.getYear() + 1900,
                date.getMonth() + 1,
                date.getDate(),
                date.getHours(),
                date.getMinutes(),
                date.getSeconds()
        );
    }

    @SuppressWarnings("deprecation")
    public static String dateToString(Date date) {
        if (date == null)
            return null;
        return String.format(FORMATO_DATE,
                date.getYear() + 1900,
                date.getMonth() + 1,
                date.getDate()
        );
    }

    public static Date stringToDate(String dateString) {
        if (dateString == null)
            return null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
        }
        throw new IllegalArgumentException("Unknown Date format:" + dateString);
    }

    @SuppressWarnings("deprecation")
    public static String timestampToStringFormatada(Date data) {
        if (data == null) {
            return null;
        }
//		return StringUtil.lpad(data.getDate(), 2, '0') + "/" + StringUtil.lpad(data.getMonth() + 1, 2, '0') + "/" + (data.getYear() + 1900)
//				+ " " + StringUtil.lpad(data.getHours(), 2, '0') + ":" + StringUtil.lpad(data.getMinutes(), 2, '0') + ":"
//				+ StringUtil.lpad(data.getSeconds(), 2, '0');
        return String.format(
                "%02d/%02d/%04d %02d:%02d:%02d",
                data.getDate(), (data.getMonth() + 1), (data.getYear() + 1900),
                data.getHours(), data.getMinutes(), data.getSeconds()
        );
    }

    @SuppressWarnings("deprecation")
    public static String dateToStringFormatada(Date data) {
        if (data == null) {
            return null;
        }
        return String.format(
                "%02d/%02d/%04d",
                data.getDate(), (data.getMonth() + 1), (data.getYear() + 1900)
        );
    }

//	public static String dateToStringBanco(Date data) {
//	if (data == null) {
//		return null;
//	}
//	return (data.getYear() + 1900) + "-" + lpad(data.getMonth() + 1, 2, '0') + "-" + lpad(data.getDate(), 2, '0')
//			+ " " + lpad(data.getHours(), 2, '0') + ":" + lpad(data.getMinutes(), 2, '0') + ":"
//			+ lpad(data.getSeconds(), 2, '0');
//}

//public static String dateToStringBancoTruncado(Date data) {
//	if (data == null) {
//		return null;
//	}
//	return (data.getYear() + 1900) + "-" + lpad(data.getMonth() + 1, 2, '0') + "-" + lpad(data.getDate(), 2, '0');
//}

//public static String dateToStringFormatada(Date data) {
//	if (data == null) {
//		return null;
//	}
//	return lpad(data.getDate(), 2, '0') + "/" + lpad(data.getMonth() + 1, 2, '0') + "/" + (data.getYear() + 1900);
//}

//	/**
//	 * 
//	 * @param date
//	 * @param patern
//	 * @return Retorna uma dada do tipo
//	 */
//	@SuppressWarnings("deprecation")
//	public static String dateToStringFormatadaHoraMin(Date data) {
//		if (data == null) {
//			return null;
//		}
//		Calendar c = Calendar.getInstance();
//		c.setTime(data);
//		return lpad(data.getDate(), 2, '0') + "/" + lpad(data.getMonth() + 1, 2, '0') + "/" + (data.getYear() + 1900)
//				+ " " + lpad(c.get(Calendar.HOUR_OF_DAY), 2, '0') + ":" + lpad(c.get(Calendar.MINUTE), 2, '0') + ":"
//				+ lpad(c.get(Calendar.SECOND), 2, '0');
//	}

//	public static String formataData(String data) {
//		if (data.length() > 8) {
//			String dia = data.substring(8, 10);
//			String mes = data.substring(5, 7);
//			String ano = data.substring(0, 4);
//			return dia + "/" + mes + "/" + ano;
//		}
//		return null;
//
//	}
//
//	public static String formataDataBanco(String localeString) {
//		String dia = localeString.substring(0, 2);
//		String mes = localeString.substring(3, 5);
//		String ano = localeString.substring(6, 10);
//		return ano + "-" + mes + "-" + dia;
//	}

}
