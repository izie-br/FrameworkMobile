package br.com.cds.mobile.framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.util.Log;

public class LogPadrao {

	private static final int TAMANHO_MAXIMO_PARTE_LOG = 4000;
	private static FileOutputStream fileLog;
	private static StringBuffer log = new StringBuffer();

	public static FileOutputStream getFileLog() throws FileNotFoundException {
		if (fileLog == null) {
			File pasta = new File(BOFacade.getInstance().getCaminhoLog());
			if (!pasta.exists()) {
				pasta.mkdirs();
			}
			fileLog = new FileOutputStream(BOFacade.getInstance().getCaminhoLog()
					+ BOFacade.getInstance().getArquivoLog());
		}
		return fileLog;
	}

	private static final int TAMANHO_MAXIMO_LOG = 100000;
	private static boolean logatudo = true;

	public static void d(String message) {
		try {
			getFileLog().write(message.getBytes());
			getFileLog().write("\n".getBytes());
		} catch (Throwable e) {
			BOFacade.getInstance().gravarErro(e);
		}
		log.append(message).append("\n");
		if (log.length() > TAMANHO_MAXIMO_LOG) {
			log.delete(0, log.length() - TAMANHO_MAXIMO_LOG);
		}
		if (logatudo) {
			if (message.length() >= TAMANHO_MAXIMO_PARTE_LOG) {
				for (int i = 0; i <= message.length() / TAMANHO_MAXIMO_PARTE_LOG; i++) {
					int logInicial = i * TAMANHO_MAXIMO_PARTE_LOG;
					int logFinal = logInicial + TAMANHO_MAXIMO_PARTE_LOG;
					if (logInicial > TAMANHO_MAXIMO_LOG) {
						return;
					}
					if (message.substring(logInicial).length() <= TAMANHO_MAXIMO_PARTE_LOG) {
						Log.d("GenericActivity Mobile", message.substring(logInicial));
					} else {
						Log.d("GenericActivity Mobile", message.substring(logInicial, logFinal));
					}
				}
			} else {
				Log.d("GenericActivity Mobile", message);
			}
		} else {
			Log.d("GenericActivity Mobile", message);
		}
	}

//	public static void d(Throwable aThrowable) {
//		d(StringUtil.getStackTrace(aThrowable));
//	}

	public static String getLogString() {
		return log.toString();
	}

	public static void reset() {
		log.setLength(0);
	}

	// public static String getLogErros() {
	// try {
	// String file = BOFacade.getInstance().getCaminhoLog() +
	// BOFacade.getInstance().getArquivoLog();
	// String content = StringUtil.readFileAsString(file);
	// // System.out.println("***********************************content:"
	// // + content.length());
	// File f = new File(file);
	// f.exists();
	// f.delete();
	// f.exists();
	// fileLog = null;
	// d("Log zerado!");
	// return StringUtil.toBase64(content);
	// } catch (IOException e) {
	// BOFacade.getInstance().gravarErro(e);
	// return "ERRO AO RECUPERAR O LOG.";
	// }
	// }
}