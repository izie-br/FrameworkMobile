package br.com.cds.mobile.framework.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;

public class StringUtil {

	public static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * Converte a colecao em csv.
	 * Escapa os caracteres " em duas aspas ("").
	 * Todas strings que tiverm virgulas ou newlines ficam entre aspas duplas ("string, string").
	 * @param collection colecao de objetos
	 * @return csv com caracteres escapados
	 */
	public static String collectionToCSV(Collection<?> collection) {
		StringBuilder retorno = new StringBuilder();
		if (collection.size() == 0)
			return "";
		Iterator<?> it = collection.iterator();
		for (;;) {
			String objStr = it.next()
					.toString()
					.replaceAll("\\\"", "\"\"");
			if(objStr.contains(",")||objStr.contains("\n")){
				retorno.append('"');
				retorno.append(objStr);
				retorno.append('"');
			}
			else
				retorno.append(objStr);
			if(it.hasNext())
				retorno.append(",");
			else
				break;
		}
		return retorno.toString();
	}

//	/**
//	 * Transforma o stream UTF-8 em uma string
//	 * @param is
//	 * @return
//	 * @throws IOException
//	 */
//	public static String convertStreamToString(InputStream is) throws IOException {
//		if (is == null)
//			return "";
//		Writer writer = new StringWriter();
//		char[] buffer = new char[1024];
//		try {
//			Reader reader = new BufferedReader(new InputStreamReader(is, DEFAULT_ENCODING));
//			int n;
//			while ((n = reader.read(buffer)) != -1) {
//				writer.write(buffer, 0, n);
//			}
//		} finally {
//			is.close();
//		}
//		return writer.toString();
//	}
//
//	public static String assetToString(Context context, String assetFile) throws IOException {
//		InputStream input = context.getAssets().open(assetFile);
//		int size = input.available();
//		byte[] buffer = new byte[size];
//		input.read(buffer);
//		input.close();
//		return new String(buffer);
//	}

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


//	/**
//	 * 
//	 * @param nomeArquivo nome do aquivo
//	 * @param conteudo
//	 * @throws IOException
//	 */
//	public static void escreverNoArquivo(String nomeArquivo, String conteudo) throws IOException {
//		File root = Environment.getDataDirectory();
//		if (root.canWrite()) {
//			File file = new File(nomeArquivo);
//			file.mkdirs();
//			BufferedWriter out = new BufferedWriter(
//					new OutputStreamWriter(
//							new FileOutputStream(file),
//							DEFAULT_ENCODING
//					)
//			);
//			try{
//				out.write(conteudo);
//			} finally {
//				out.close();
//			}
//		}
//		// BufferedWriter out = new BufferedWriter(new FileWriter(
//		// "/sdcard/flora/fvm/anexos/" + nomeArquivo + "." + extensao));
//		// out.write(conteudo);
//		// out.close();
//	}
//
//	public static String readFileAsString(String filePath) throws java.io.IOException {
//		StringBuffer fileData = new StringBuffer(1000);
//		BufferedReader reader = new BufferedReader(new FileReader(filePath));
//		// int len = 0;
//		try {
//			char[] buf = new char[1024];
//			int numRead = 0;
//			while ((numRead = reader.read(buf)) != -1) {
//				String readData = String.valueOf(buf, 0, numRead);
//				// len += readData.length();
//				// System.out.println("********************************len:" +
//				// len);
//				fileData.append(readData);
//				buf = new char[1024];
//			}
//			reader.close();
//		} catch (OutOfMemoryError e) {
//			e.printStackTrace();
//		}
//		return fileData.toString();
//	}

//	public static CharSequence formataDistancia(float distancia) {
//		String unidade = " m";
//		if (distancia > 1000) {
//			distancia = (float) (distancia * 0.001);
//			unidade = " km";
//		}
//		return formataMoeda(distancia) + unidade;
//	}

//	public static CharSequence formataMoeda(double valor) {
//		return formataMoeda(valor, "");
//	}
//
//	public static CharSequence formataMoeda(double valor, String prefixo) {
//		String valorFormatado = formataValor(valor);
//		return prefixo + valorFormatado;
//	}
//
//	public static CharSequence formataMoeda(float valor, String prefixo) {
//		String valorFormatado = formataValor(valor);
//		return prefixo + valorFormatado;
//	}

//	public static String formataValor(double valor) {
//		return DecimalFormat.getCurrencyInstance().format(valor);
//		// NumberFormat.getNumberInstance(Locale.getDefault()).fo
//		// String retorno = NumberFormat.getNumberInstance().format(
//		// new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP));
//		// return new BigDecimal(valor).setScale(2,
//		// RoundingMode.HALF_UP).toString().replace('.', ',');
//	}

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


	public static boolean isNull(String string) {
		if (string == null) {
			return true;
		}
		if (string.trim().equals("")) {
			return true;
		}
		return false;
	}

//	public static String lpad(int campoInt, int tamanho){
//		return String.format("%0"+tamanho+"d", campoInt);
//	}

//	public static String lpad(int campoInt, int tamanho, char caracter) {
//		String campo = String.valueOf(campoInt);
//		StringBuilder temp = new StringBuilder(campo);
//		if (tamanho > campo.length()) {
//			for (int i = 0; i < (tamanho - campo.length()); i++) {
//				temp.insert(0, caracter);
//			}
//		}
//		return temp.toString();
//	}

	public static String lpad(String campo, int tamanho, char caracter) {
		StringBuilder temp = new StringBuilder(campo);
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

//	public static String removeUltimoCaracter(String string) {
//		return string.substring(0, string.length() - 1);
//	}

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
			md.update(text.getBytes(DEFAULT_ENCODING), 0, text.length());
			sha1hash = md.digest();
			return convertToHex(sha1hash);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
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

}