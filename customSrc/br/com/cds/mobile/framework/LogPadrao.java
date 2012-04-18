package br.com.cds.mobile.framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;

import br.com.cds.mobile.framework.config.Constantes;
import br.com.cds.mobile.framework.utils.DateUtil;

import android.os.Environment;
import android.util.Log;

// TODO criar interface na lib
public class LogPadrao {

	private static final int TAMANHO_MAXIMO_PARTE_LOG = 4000;
	private static FileOutputStream fileLog;
	private static StringBuffer log = new StringBuffer();

	public static FileOutputStream getFileLog() throws FileNotFoundException {
		if (fileLog == null) {
			File pasta = new File(getCaminhoLog());
			if (!pasta.exists()) {
				pasta.mkdirs();
			}
			fileLog = new FileOutputStream(getCaminhoLog()
					+ getArquivoLog()
			);
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
			gravarErro(e);
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

	public static void gravarErro(Throwable t) {
		gravarErro(t,false);
	}

	public static void gravarErro(Throwable t, boolean forcar) {
		try {
//			erroBO.gravarErro(t, forcar);
		} catch (Throwable t2) {
			t2.printStackTrace();
		}
	}

	public static String getCaminhoLog() {
		return Environment.getDataDirectory() + Constantes.LOG_DIR;
	}

	public static String getArquivoLog() {
		return DateUtil.dateToString(new Date()) + "_log.txt";
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