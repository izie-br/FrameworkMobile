package br.com.cds.mobile.framework.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import br.com.cds.mobile.framework.BaseApplication;

import android.util.Log;

/**
 * <p>LogPadrao do framework.</p>
 * <p>Usa o nome do pacote como a TAG nas chamadas de Log::(d|i|e).</p>
 * <p>Pode gravar as chamadas aos metodos "i","e" se um prototpo for
 *    registrado</p>
 * @author Igor Bruno Pereira Soares
 */
public class LogPadrao {

	private static String tag;
	private static LogEntry logEntryPrototype;
	//private static JanelaPadrao janela;

	/**
	 * <p>Prototipo de LogEntry.</p>
	 * <p>A cada chamada dos metodos "i", "e", o prototipo eh clonado
	 *   usando <b>LogEntry::clone()</b> e um log eh escrito com
	 *   <b>LogEntry::log(level,message))</b> e gravado usando
	 *   <b>LogEntry::save()</b>
	 * </p>
	 * @param logEntryPrototype prototipo
	 */
	public static void setPrototype(LogEntry logEntryPrototype){
		LogPadrao.logEntryPrototype = logEntryPrototype;
	}

	/**
	 * Escreve a mensagem em log, no nivel "info"
	 * @param message mensagem, ou opcionalmente uma string formato, estilo printf
	 * @param args argumentos da string formato
	 */
	public static void i(String message, Object...args){
		if(args!=null&&args.length>0)
			message = String.format(message, args);
		Log.i(getTag(),message);
		if(logEntryPrototype!=null){
			LogEntry logEntry = logEntryPrototype.clone();
			logEntry.log(LogEntry.LEVEL_INFO,message);
			logEntry.save();
		}
	}

	/**
	 * Escreve a mensagem em log, no nivel "error"
	 * @param message mensagem, ou opcionalmente uma string formato, estilo printf
	 * @param args argumentos da string formato
	 */
	public static void e(String message, Object...args){
		if(args!=null&&args.length>0)
			message = String.format(message, args);
		Log.e(getTag(),message);
		if(logEntryPrototype!=null){
			LogEntry logEntry = logEntryPrototype.clone();
			logEntry.log(LogEntry.LEVEL_ERROR,message);
			logEntry.save();
		}
	}

	/**
	 * Escreve o stacktrace do erro em um log de nivel "error"
	 * @param t
	 */
	public static void e(Throwable t){
		e(getStackTrace(t));
	}

	/**
	 * Faz a chamada a Log.d com a mensagem, ou string formato com argumentos
	 * @param message mensagem, ou opcionalmente uma string formato, estilo printf
	 * @param args argumentos da string formato
	 */
	public static void d(String message, Object...args){
		if(args!=null&&args.length>0)
			message = String.format(message, args);
		Log.d(getTag(), message);
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

	private static String getTag(){
		if(tag==null)
			tag = BaseApplication.getContext().getPackageName();
		return tag;
	}

}