package br.com.cds.mobile.framework.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import android.content.Context;
import android.os.Environment;
//import br.com.cds.mobile.flora.com.GenericComunicacao;

public class StringUtil {

	static Hashtable<Integer, String> mesExtenso = new Hashtable<Integer, String>();

	public static String arrayListToCSV(ArrayList<?> lista) {
		String retorno = "";
		if (lista.size() == 0) {
			return retorno;
		}
		for (Object object : lista) {
			retorno += object.toString() + ",";
		}
		retorno = retorno.substring(0, retorno.length() - 1);
		return retorno;
	}

	public static String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

//	public static String convertStreamToString(InputStream is, int length) throws IOException {
//		byte[] imageData = new byte[length];
//		int buffersize = (int) Math.ceil(length / (double) GenericComunicacao.SIZE);
//		int downloaded = 0;
//		for (int i = 1; i < GenericComunicacao.SIZE; i++) {
//			int read = is.read(imageData, downloaded, buffersize);
//			downloaded += read;
//		}
//		is.read(imageData, downloaded, length - downloaded);
//
//		return new String(imageData);
//	}

	public static String assetToString(Context context, String assetFile) throws IOException {
		InputStream input = context.getAssets().open(assetFile);
		int size = input.available();
		byte[] buffer = new byte[size];
		input.read(buffer);
		input.close();
		return new String(buffer);
	}

	/**
	 * Defines a custom format for the stack trace as String.
	 */
	// public static String getCustomStackTrace(Throwable aThrowable) {
	// // add the class name and any message passed to constructor
	// final StringBuilder result = new StringBuilder("BOO-BOO: ");
	// result.append(aThrowable.toString());
	// final String NEW_LINE = System.getProperty("line.separator");
	// result.append(NEW_LINE);
	//
	// // add each element of the stack trace
	// for (StackTraceElement element : aThrowable.getStackTrace()) {
	// result.append(element);
	// result.append(NEW_LINE);
	// }
	// return result.toString();
	// }

//	public static String dateToStringBanco(Date data) {
//		if (data == null) {
//			return null;
//		}
//		return (data.getYear() + 1900) + "-" + lpad(data.getMonth() + 1, 2, '0') + "-" + lpad(data.getDate(), 2, '0')
//				+ " " + lpad(data.getHours(), 2, '0') + ":" + lpad(data.getMinutes(), 2, '0') + ":"
//				+ lpad(data.getSeconds(), 2, '0');
//	}

//	public static String dateToStringBancoTruncado(Date data) {
//		if (data == null) {
//			return null;
//		}
//		return (data.getYear() + 1900) + "-" + lpad(data.getMonth() + 1, 2, '0') + "-" + lpad(data.getDate(), 2, '0');
//	}

//	public static String dateToStringFormatada(Date data) {
//		if (data == null) {
//			return null;
//		}
//		return lpad(data.getDate(), 2, '0') + "/" + lpad(data.getMonth() + 1, 2, '0') + "/" + (data.getYear() + 1900);
//	}

	/**
	 * 
	 * @param date
	 * @param patern
	 * @return Retorna uma dada do tipo
	 */
	@SuppressWarnings("deprecation")
	public static String dateToStringFormatadaHoraMin(Date data) {
		// TODO usar format
		// TODO passar para o DateUtils
		if (data == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(data);
		return lpad(data.getDate(), 2, '0') + "/" + lpad(data.getMonth() + 1, 2, '0') + "/" + (data.getYear() + 1900)
				+ " " + lpad(c.get(Calendar.HOUR_OF_DAY), 2, '0') + ":" + lpad(c.get(Calendar.MINUTE), 2, '0') + ":"
				+ lpad(c.get(Calendar.SECOND), 2, '0');
	}

	public static void escreverNoArquivo(String nomeArquivo, String conteudo) throws IOException {
		File root = Environment.getDataDirectory();
		if (root.canWrite()) {
			File file = new File(nomeArquivo);
			file.mkdirs();
			FileWriter writer = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(writer);
			out.write(conteudo);
			out.close();
		}
		// BufferedWriter out = new BufferedWriter(new FileWriter(
		// "/sdcard/flora/fvm/anexos/" + nomeArquivo + "." + extensao));
		// out.write(conteudo);
		// out.close();
	}

	public static String readFileAsString(String filePath) throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		// int len = 0;
		try {
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				// len += readData.length();
				// System.out.println("********************************len:" +
				// len);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return fileData.toString();
	}

	public static String formataData(String data) {
		if (data.length() > 8) {
			String dia = data.substring(8, 10);
			String mes = data.substring(5, 7);
			String ano = data.substring(0, 4);
			return dia + "/" + mes + "/" + ano;
		}
		return null;

	}

	public static String formataDataBanco(String localeString) {
		String dia = localeString.substring(0, 2);
		String mes = localeString.substring(3, 5);
		String ano = localeString.substring(6, 10);
		return ano + "-" + mes + "-" + dia;
	}

	public static CharSequence formataDistancia(float distancia) {
		String unidade = " m";
		if (distancia > 1000) {
			distancia = (float) (distancia * 0.001);
			unidade = " km";
		}
		return formataMoeda(distancia) + unidade;
	}

	public static CharSequence formataMoeda(double valor) {
		return formataMoeda(valor, "");
	}

	public static CharSequence formataMoeda(double valor, String prefixo) {
		String valorFormatado = formataValor(valor);
		return prefixo + valorFormatado;
	}

	public static CharSequence formataMoeda(float valor, String prefixo) {
		String valorFormatado = formataValor(valor);
		return prefixo + valorFormatado;
	}

	public static String formataValor(double valor) {
		return DecimalFormat.getCurrencyInstance().format(valor);
		// NumberFormat.getNumberInstance(Locale.getDefault()).fo
		// String retorno = NumberFormat.getNumberInstance().format(
		// new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP));
		// return new BigDecimal(valor).setScale(2,
		// RoundingMode.HALF_UP).toString().replace('.', ',');
	}

	// public static String formatarValor(String valor) {
	// String parteInteira = null;
	// String parteDecimal = "00";
	// String retorno = null;
	//
	// if (valor.indexOf(".") >= 1) {
	// parteInteira = valor.substring(0, valor.indexOf("."));
	// parteDecimal = valor.substring(valor.indexOf(".") + 1);
	//
	// if (parteDecimal.length() > 1) {
	// parteDecimal = parteDecimal.substring(0, 2);
	// } else {
	// parteDecimal = rpad(parteDecimal, 2, "0");
	// }
	// } else {
	// parteInteira = valor;
	// }
	//
	// retorno = "," + parteDecimal;
	//
	// while (parteInteira.replace('-', ' ').trim().length() > 3) {
	// if (retorno.charAt(0) != ',') {
	// retorno = "." + retorno;
	// }
	//
	// retorno = parteInteira.substring(parteInteira.length() - 3,
	// parteInteira.length()) + retorno;
	// parteInteira = parteInteira.substring(0, parteInteira.length() - 3);
	// }
	//
	// if (retorno.charAt(0) != ',') {
	// retorno = "." + retorno;
	// }
	//
	// retorno = parteInteira + retorno;
	//
	// return retorno;
	// }

	public static int getAno(String data) {
		return Integer.parseInt(data.substring(0, 4));
	}

	//
	// public static String md5(String s) {
	// try {
	// if (s.length() == 32) {
	// return s;
	// }
	// // Create MD5 Hash
	// MessageDigest digest = java.security.MessageDigest
	// .getInstance("MD5");
	// digest.update(s.getBytes());
	// byte messageDigest[] = digest.digest();
	//
	// // Create Hex String
	// StringBuffer hexString = new StringBuffer();
	// for (int i = 0; i < messageDigest.length; i++)
	// hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
	// return hexString.toString();
	//
	// } catch (NoSuchAlgorithmException e) {
	// e.printStackTrace();
	// }
	// return "";
	// }

//	public static String getDataCabecalho(Date data) {
//		String dataBanco = formataDataBanco(dateToStringBanco(data));
//		return getMesExtenso(getMes(dataBanco)) + " de " + getAno(dataBanco);
//	}

	public static int getDia(String data) {
		return Integer.parseInt(data.substring(8, 10));
	}

	public static int getMes(String data) {
		return Integer.parseInt(data.substring(5, 7).trim());
	}

//	public static String getMesExtenso(int mes) {
//		if (mes == 1)
//			return "Janeiro";
//		if (mes == 2)
//			return "Fevereiro";
//		if (mes == 3)
//			return "Marco";
//		if (mes == 4)
//			return "Abril";
//		if (mes == 5)
//			return "Maio";
//		if (mes == 6)
//			return "Junho";
//		if (mes == 7)
//			return "Julho";
//		if (mes == 8)
//			return "Agosto";
//		if (mes == 9)
//			return "Setembro";
//		if (mes == 10)
//			return "Outubro";
//		if (mes == 11)
//			return "Novembro";
//		if (mes == 12)
//			return "Dezembro";
//		return "Nao Sei";
//	}

	public static String getStackTrace(Throwable aThrowable) {
		// aThrowable.printStackTrace();
		if (aThrowable == null) {
			return null;
		}
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	public static boolean isNull(String string) {
		if (string == null) {
			return true;
		}
		if (string.trim().equals("")) {
			return true;
		}
		return false;
	}

	public static String lpad(int campoInt, int tamanho, char caracter) {
		String campo = String.valueOf(campoInt);
		StringBuffer temp = new StringBuffer(campo);
		if (tamanho > campo.length()) {
			for (int i = 0; i < (tamanho - campo.length()); i++) {
				temp.insert(0, caracter);
			}
		}
		return temp.toString();
	}

	public static String lpad(String campo, int tamanho, char caracter) {
		StringBuffer temp = new StringBuffer(campo);
		if (tamanho > campo.length()) {
			for (int i = 0; i < (tamanho - campo.length()); i++) {
				temp.insert(0, caracter);
			}
		}
		return temp.toString();
	}

	// public static String md5(String in) {
	// if (in == null) {
	// return null;
	// }
	// if (in.length() == 32) {
	// return in;
	// }
	// MessageDigest digest;
	// try {
	// digest = MessageDigest.getInstance("MD5");
	// digest.reset();
	// digest.update(in.getBytes());
	// byte[] a = digest.digest();
	// int len = a.length;
	// StringBuilder sb = new StringBuilder(len << 1);
	// for (int i = 0; i < len; i++) {
	// sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
	// sb.append(Character.forDigit(a[i] & 0x0f, 16));
	// }
	// return sb.toString();
	// } catch (NoSuchAlgorithmException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }

	public static String removeUltimoCaracter(String string) {
		return string.substring(0, string.length() - 1);
	}

	public static String rpad(String str, int len, String pad) {
		String novoTexto = "";

		if (str.trim().equals("")) {
			for (int i = 0; i < len; i++) {
				novoTexto += pad;
			}
		} else {
			if (str.length() > len) {
				novoTexto = str.substring(0, len);
			} else {
				for (int i = 0; i < (len - str.length()); i++) {
					novoTexto += pad;
				}

				novoTexto = str + novoTexto;
			}
		}

		return novoTexto;
	}

	public static Date stringToDate(String dateTime) {
		return stringToDateFormato(dateTime, "yyyy-MM-dd");
	}

	public static Date stringToDateFormato(String dateTime, String formato) {
		if (dateTime == null) {
			return null;
		}
		if (dateTime.equals("null")) {
			return null;
		}
		if (dateTime.trim().length() == 0) {
			return null;
		}
		Date date = null;
		SimpleDateFormat iso8601Format = new SimpleDateFormat(formato);
		try {
			date = iso8601Format.parse(dateTime);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return date;
	}

	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	public static String SHA1(String text) {
		try {
			if (text == null) {
				return null;
			}
			MessageDigest md;
			md = MessageDigest.getInstance("SHA-1");
			byte[] sha1hash = new byte[40];
			md.update(text.getBytes("iso-8859-1"), 0, text.length());
			sha1hash = md.digest();
			return convertToHex(sha1hash);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String underscoreToCamelCase(String s) {
		if (s.equals("id")) {
			return s;
		}
		String[] parts = s.split("_");
		StringBuilder camelCase = new StringBuilder();
		for (String part : parts) {
			camelCase.append(toProperCase(part));
		}
		String camelCaseString = camelCase.toString();
		camelCaseString = Character.toLowerCase(camelCaseString.charAt(0)) + camelCaseString.substring(1);
		return camelCaseString;
	}

	public static String toProperCase(String s) {
		if (s.length() > 0) {
			return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
		} else {
			return s;
		}
	}

	public static String splitCamelCase(String s) {
		return s.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
				"(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
	}

	public static String camelCaseToLowerUnderscore(String camelCase) {
		return splitCamelCase(camelCase).replace(' ', '_').toLowerCase();
	}

	public static String toBase64(String string) {
		if (string == null) {
			return null;
		}
		return new String(Base64Coder.encode(string.getBytes()));
	}

	public static String fromBase64(String string) {
		return new String(Base64Coder.decode(string));
	}

//	public static String objectToString(Object object) {
//		StringBuilder sb = new StringBuilder();
//		Field[] fieldList = object.getClass().getDeclaredFields();
//		Field fld = null;
//		sb.append(object.getClass().getName());
//		sb.append("[");
//		for (int i = 0; i < fieldList.length; i++) {
//			fld = fieldList[i];
//			Method getter = Reflection.getGetter(object.getClass(), fld);
//			if (getter == null) {
//				Log.d("objectToString", "getter nulo:" + fld.getName());
//			} else {
//				try {
//					sb.append(fld.getName() + ":" + getter.invoke(object) + ",");
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		sb.append("]");
//		return sb.toString();
//	}

	public static String stringToMd5(String dado) {
		String sen = "";
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		BigInteger hash = new BigInteger(1, md.digest(dado.getBytes()));
		sen = hash.toString(16);
		return lpad(sen, 32, '0');
	}

//	public static String getDiaDaSemana(Date dataRota) {
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(dataRota);
//		int diaDaSemana = cal.get(Calendar.DAY_OF_WEEK);
//		switch (diaDaSemana) {
//		case Calendar.SUNDAY:
//			return "Domingo";
//		case Calendar.MONDAY:
//			return "Segunda-feira";
//		case Calendar.TUESDAY:
//			return "Terca-feira";
//		case Calendar.WEDNESDAY:
//			return "Quarta-feira";
//		case Calendar.THURSDAY:
//			return "Quinta-feira";
//		case Calendar.FRIDAY:
//			return "Sexta-feira";
//		case Calendar.SATURDAY:
//			return "Sabado";
//		default:
//			break;
//		}
//		return "";
//	}

}