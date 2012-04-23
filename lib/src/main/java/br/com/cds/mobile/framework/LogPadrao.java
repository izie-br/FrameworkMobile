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
import java.lang.ref.SoftReference;
import java.util.Date;

import br.com.cds.mobile.framework.utils.DateUtil;

import android.os.Environment;
import android.util.Log;

public class LogPadrao {

	private static final String LOG_FILE_FORMAT = "%s_log.txt";
	private static final String LOG_FOLDER_FORMAT = "%s/data/%s/logs";
	private static final String LEVEL_ERROR = "erro";
	private static final String LEVEL_INFO = "info";
	private static final String TAG = BaseApplication.getContext().getPackageName();

	// TODO remover variaveis static para ser reentrant
	private static SoftReference<PrintWriter> fileLog;
	private static LogEntry logEntryPrototype;
	private static boolean debug = true;
	//private static JanelaPadrao janela;

	public static String getLogPath() {
		return String.format(
				LOG_FOLDER_FORMAT,
				Environment.getDataDirectory(),
				BaseApplication.getContext().getPackageName()
		);
	}

	public static String getDefaultLogFile() {
		return String.format(LOG_FILE_FORMAT, DateUtil.dateToString(new Date()) );
	}

	public static PrintWriter getLog() throws FileNotFoundException{
		PrintWriter fileLogPw = (fileLog == null) ? null : fileLog.get();
		if (fileLogPw == null) {
			String logPath = getLogPath();
			File pasta = new File(logPath);
			if (!pasta.exists()) {
				pasta.mkdirs();
			}
			fileLogPw = openLogFile(logPath+'/'+getDefaultLogFile());
			fileLog = new SoftReference<PrintWriter>(fileLogPw);
		}
		return fileLogPw;
	}

	private static PrintWriter openLogFile(String path) throws FileNotFoundException {
		File logFile = new File(path);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(
					new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(logFile),
									"UTF-8"
							)
					)
			);
		} catch (UnsupportedEncodingException e) {  // Este erro nao deve acontecer em hipotese alguma
			throw new RuntimeException(e);          // a menos que a plataforma nao suporte UTF-8!  O_o
		}
		return pw;
	}

	private static void writeLog(String message) {
		try {
			getLog().print(message);
			getLog().println();
		} catch (FileNotFoundException e) {
			// TODO avisar o usuario
			//   - ou ocorreu um erro catastrofico no sistema de logs
			//   - ou o sdcard encheu ... AVISAR
		}
	}

	public static void i(String message){
		Log.i(TAG,message);
		if(logEntryPrototype!=null){
			LogEntry entry = logEntryPrototype.cloneFor(message);
			entry.setLevel(LEVEL_INFO);
			entry.save();
		}
	}

	public static void e(String message) {
		Log.e(TAG,message);
		if(logEntryPrototype!=null){
			LogEntry entry = logEntryPrototype.cloneFor(message);
			entry.setLevel(LEVEL_ERROR);
			entry.save();
		}
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

//	public static void gravarErro(Throwable t, boolean forcar) {
//		try {
//			erroBO.gravarErro(t, forcar);
//		} catch (Throwable t2) {
//			t2.printStackTrace();
//		}
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