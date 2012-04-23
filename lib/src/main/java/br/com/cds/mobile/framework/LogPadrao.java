package br.com.cds.mobile.framework;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;

import br.com.cds.mobile.framework.utils.DateUtil;

import android.os.Environment;
import android.util.Log;

public class LogPadrao {

	private static final String LOG_FILE_FORMAT = "%s_log.txt";
	private static final String LOG_FOLDER_FORMAT = "%s/data/%s/logs";
	private static final String TAG = BaseApplication.getContext().getPackageName();

	// TODO remover variaveis static para ser reentrant
	private static PrintWriter fileLog;
	private static boolean debug = true;
	//private static JanelaPadrao janela;

	public static PrintWriter getFileLog() throws FrameworkException{
		if (fileLog == null) {
			File pasta = new File(getCaminhoLog());
			if (!pasta.exists()) {
				pasta.mkdirs();
			}
			fileLog = openLogFile();
		}
		return fileLog;
	}

	private static PrintWriter openLogFile() throws FrameworkException {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(
					new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(
											getCaminhoLog()+
											"/"+getArquivoLog()
									),
									"UTF-8"
							)
					)
			);
		} catch (UnsupportedEncodingException e) {  // Este erro nao deve acontecer em hipotese alguma
			throw new RuntimeException(e);          // a menos que a plataforma nao suporte UTF-8!  O_o
		} catch (FileNotFoundException e) {
			throw new FrameworkException(ErrorCode.LOG_FILE_NOT_FOUND);
		}
		return pw;
	}

	public static void i(String message){
		// TODO 
	}

	public static void e(String message) {
		try {
			getFileLog().println(message);
		} catch (FrameworkException e) {
			// refazer isso aqui
			gravarErro(e);
		}
		// TODO nao entendi 
//		log.append(message).append("\n");
//		if (log.length() > TAMANHO_MAXIMO_LOG) {
//			log.delete(0, log.length() - TAMANHO_MAXIMO_LOG);
//		}
//		if (debug) {
//			if (message.length() >= TAMANHO_MAXIMO_PARTE_LOG) {
//				for (int i = 0; i <= message.length() / TAMANHO_MAXIMO_PARTE_LOG; i++) {
//					int logInicial = i * TAMANHO_MAXIMO_PARTE_LOG;
//					int logFinal = logInicial + TAMANHO_MAXIMO_PARTE_LOG;
//					if (logInicial > TAMANHO_MAXIMO_LOG) {
//						return;
//					}
//					if (message.substring(logInicial).length() <= TAMANHO_MAXIMO_PARTE_LOG) {
//						Log.d("GenericActivity Mobile", message.substring(logInicial));
//					} else {
//						Log.d("GenericActivity Mobile", message.substring(logInicial, logFinal));
//					}
//				}
//			} else {
//				Log.d("GenericActivity Mobile", message);
//			}
//		} else {
//			Log.d("GenericActivity Mobile", message);
//		}
	}

	public static void e(Throwable t){
		e(getStackTrace(t));
	}

	public static void d(String message){
		if(debug)
			Log.d(TAG, message);
	}

//	public static void d(Throwable aThrowable) {
//		d(StringUtil.getStackTrace(aThrowable));
//	}

//	public static String getLogString() {
//		return log.toString();
//	}
//
//	public static void reset() {
//		log.setLength(0);
//	}

	public static void gravarErro(Throwable t) {
		// gravarErro(t,false);
	}

//	public static void gravarErro(Throwable t, boolean forcar) {
//		try {
//			erroBO.gravarErro(t, forcar);
//		} catch (Throwable t2) {
//			t2.printStackTrace();
//		}
//	}

	public static String getCaminhoLog() {
		return String.format(
				LOG_FOLDER_FORMAT,
				Environment.getDataDirectory(),
				BaseApplication.getContext().getPackageName()
		);
	}

	public static String getArquivoLog() {
		return String.format(LOG_FILE_FORMAT, DateUtil.dateToString(new Date()) );
	}

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