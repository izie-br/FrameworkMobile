package br.com.cds.mobile.framework.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import br.com.cds.mobile.framework.BaseApplication;

import android.util.Log;

public class LogPadrao {

	private static final String TAG = BaseApplication.getContext().getPackageName();

	// TODO remover variaveis static para ser reentrant
	private static LogEntry logEntryPrototype;
	private static boolean debug = true;
	//private static JanelaPadrao janela;

	public static void setPrototype(LogEntry logEntryPrototype){
		LogPadrao.logEntryPrototype = logEntryPrototype;
	}

	public static void i(String message){
		Log.i(TAG,message);
		if(logEntryPrototype!=null){
			LogEntry logEntry = logEntryPrototype.clone();
			logEntry.log(LogEntry.LEVEL_INFO,message);
			logEntry.save();
		}
	}

	public static void e(String message) {
		Log.e(TAG,message);
		if(logEntryPrototype!=null){
			LogEntry logEntry = logEntryPrototype.clone();
			logEntry.log(LogEntry.LEVEL_ERROR,message);
			logEntry.save();
		}
	}

	public static void e(Throwable t){
		e(getStackTrace(t));
	}

	public static void d(String message){
		if(debug)
			Log.d(TAG, message);
	}

	public static String getStackTrace(Throwable aThrowable) {
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