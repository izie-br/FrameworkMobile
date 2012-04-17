package br.com.cds.mobile.framework.utils;

import java.util.Date;

public class DateUtil {

	private static final long DIA = 24 * 60 * 60 * 1000;
	private static final long HORA = 60 * 60 * 1000;
	private static final long MES = 30 * 24 * 60 * 60 * 1000;
	private static final long MINUTO = 60 * 1000;
	private static final long SEGUNDO = 1000;

	// private static final int ANO = 12 * MES;

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
	public static String timestampToStringFormatada(Date data) {
		if (data == null) {
			return null;
		}
		//TODO refazer com string format
		return StringUtil.lpad(data.getDate(), 2, '0') + "/" + StringUtil.lpad(data.getMonth() + 1, 2, '0') + "/" + (data.getYear() + 1900)
				+ " " + StringUtil.lpad(data.getHours(), 2, '0') + ":" + StringUtil.lpad(data.getMinutes(), 2, '0') + ":"
				+ StringUtil.lpad(data.getSeconds(), 2, '0');
	}

	//TODO conferir isso aqui
	public static Date stringToTimestamp(String dateTime) {
//		try {
			return SQLiteUtils.stringToDate(dateTime);
//			return stringToDateFormato(dateTime, "yyyy-MM-dd KK:mm:ss");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return stringToDateFormato(dateTime, "yyyy-MM-dd");
//		}

	}


}
