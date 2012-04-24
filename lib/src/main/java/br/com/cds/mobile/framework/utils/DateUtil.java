package br.com.cds.mobile.framework.utils;

import java.util.Date;

public class DateUtil {

	private static final long DIA = 24 * 60 * 60 * 1000;
	private static final long HORA = 60 * 60 * 1000;
	private static final long MES = 30 * 24 * 60 * 60 * 1000;
	private static final long MINUTO = 60 * 1000;
	private static final long SEGUNDO = 1000;

	/**
	 * formato YYYY-MM-DD HH:mm:SS
	 */
	public static final String FORMATO_TIMESTAMP = "%04d-%02d-%02d %02d:%02d:%02d";
	public static final String FORMATO_DATE = "%04d-%02d-%02d";

	// substrings com fim nao inclusivo
	private static int STRING_TO_DATE_ANO[] = {0,4};
	private static int STRING_TO_DATE_MES[] = {5,7};
	private static int STRING_TO_DATE_DIA[] = {8,10};
	private static int STRING_TO_DATE_HORAS[] = {11,13};
	private static int STRING_TO_DATE_MINUTOS[] = {14,16};
	private static int STRING_TO_DATE_SEGUNDOS[] = {17};

	public static Date adicionaMeses(Date date, int meses) {
		long atual = date.getTime();
		long periodoAdicional = MES * meses;
		return new Date(atual + periodoAdicional);
	}

	public static Date adicionaDias(Date date, int dias) {
		long atual = date.getTime();
		long periodoAdicional = DIA * dias;
		return new Date(atual + periodoAdicional);
	}

	public static Date subtraiDias(Date date, int dias) {
		return adicionaDias(date, dias * -1);
	}

	public static Date subtraiMeses(Date date, int meses) {
		return adicionaMeses(date, meses * -1);
	}

	public static Date adicionaHoras(Date date, int horas) {
		long atual = date.getTime();
		long periodoAdicional = HORA * horas;
		return new Date(atual + periodoAdicional);
	}

	public static Date subtraiHoras(Date date, int horas) {
		return adicionaHoras(date, horas * -1);
	}

	public static Date adicionaMinutos(Date date, int minutos) {
		long atual = date.getTime();
		long periodoAdicional = MINUTO * minutos;
		return new Date(atual + periodoAdicional);
	}

	public static Date subtraiMinutos(Date date, int minutos) {
		return adicionaMinutos(date, minutos * -1);
	}

	public static Date adicionaSegundos(Date date, int segundos) {
		long atual = date.getTime();
		long periodoAdicional = SEGUNDO * segundos;
		return new Date(atual + periodoAdicional);
	}

	public static Date subtraiSegundos(Date date, int segundos) {
		return adicionaSegundos(date, segundos * -1);
	}

	public static Date adicionaMSs(Date date, long mss) {
		long atual = date.getTime();
		long periodoAdicional = mss;
		return new Date(atual + periodoAdicional);
	}

	public static Date subtraiMSs(Date date, long mss) {
		return adicionaMSs(date, mss * -1);
	}

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
				data.getDate(), (data.getMonth() +1), (data.getYear() +1900),
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
				data.getDate(), (data.getMonth() +1), (data.getYear() +1900)
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
