package com.quantium.mobile.framework.utils;

import java.io.*;
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
			if(
					objStr.contains(",")||
					objStr.contains("\n")||
					objStr.contains("\r")||
					objStr.contains("\"")
			){
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

	public static boolean isNull(String string) {
		if (string == null) {
			return true;
		}
		if (string.trim().equals("")) {
			return true;
		}
		return false;
	}

	public static String lpad(String campo, int tamanho, char caracter) {
		StringBuilder temp = new StringBuilder(campo);
		if (tamanho > campo.length()) {
			for (int i = 0; i < (tamanho - campo.length()); i++) {
				temp.insert(0, caracter);
			}
		}
		return temp.toString();
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

//	private static String convertToHex(byte[] data) {
//		StringBuffer buf = new StringBuffer();
//		for (int i = 0; i < data.length; i++) {
//			int halfbyte = (data[i] >>> 4) & 0x0F;
//			int two_halfs = 0;
//			do {
//				if ((0 <= halfbyte) && (halfbyte <= 9))
//					buf.append((char) ('0' + halfbyte));
//				else
//					buf.append((char) ('a' + (halfbyte - 10)));
//				halfbyte = data[i] & 0x0F;
//			} while (two_halfs++ < 1);
//		}
//		return buf.toString();
//	}
//
//	public static String SHA1(String text) {
//		try {
//			if (text == null) {
//				return null;
//			}
//			MessageDigest md;
//			md = MessageDigest.getInstance("SHA-1");
//			byte[] sha1hash = new byte[40];
//			md.update(text.getBytes(DEFAULT_ENCODING), 0, text.length());
//			sha1hash = md.digest();
//			return convertToHex(sha1hash);
//		} catch (NoSuchAlgorithmException e) {     // Estes erros soh ocorrem se
//			throw new RuntimeException(e);         // a plataforma nao suportar
//		} catch (UnsupportedEncodingException e) { // UTF-8 OU SHA1! O_o
//			throw new RuntimeException(e);
//		}
//	}
//
//	public static String toBase64(String string) {
//		if (string == null) {
//			return null;
//		}
//		try {
//			return new String(Base64Coder.encode(string.getBytes(DEFAULT_ENCODING)));
//		} catch (UnsupportedEncodingException e) {// Estes erros soh ocorrem se a
//			throw new RuntimeException(e);        // plataforma nao suportar UTF-8! O_o
//		}
//	}
//
//	public static String fromBase64(String string) {
//		return new String(Base64Coder.decode(string));
//	}

	public static String toMd5(String dado) {
		String sen = "";
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        BigInteger hash = null;
        try {
            hash = new BigInteger(1, md.digest(dado.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        sen = hash.toString(16);
		return lpad(sen, 32, '0');
	}

	/**
	 * 
	 * @param aThrowable exception ou erro
	 * @return stacktrace em uma string
	 */
	public static String getStackTrace(Throwable aThrowable) {
		if (aThrowable == null)
			return null;
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}
	
	/**
	 * 
	 * @param reader Reader com informações a serem lidas
	 * @return o conteúdo do reader em uma string
	 * @throws IOException 
	 */
	public static String readerToString(Reader reader) throws IOException {
		if (reader == null) {
			return null;
		}
		reader.mark(0);
		reader.reset();
		StringBuilder builder = new StringBuilder();
		int data = reader.read();
		while (data != -1) {
			builder.append((char) data);
			data = reader.read();
		}
		return builder.toString();
	}


}