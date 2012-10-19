package com.quantium.mobile.framework.logging;

/**
 * <p>LogPadrao do framework.</p>
 * <p>Usa o nome do pacote como a TAG nas chamadas de Log::(d|i|e).</p>
 * <p>Pode gravar as chamadas aos metodos "i","e" se um prototpo for
 *    registrado</p>
 * @author Igor Bruno Pereira Soares
 */
public abstract class LogPadrao {

	private static LogPadrao instance;

	public static void setLogImplementation(LogPadrao implementation){
		LogPadrao.instance = implementation;
	}

	protected abstract void setLogEntryPrototype(LogEntry logEntryPrototype);
	protected abstract void info(String message, Object...args);
	protected abstract void error(String message, Object...args);
	protected abstract void error(Throwable t);
	protected abstract void debug(String message, Object...args);


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
		if (instance != null)
			instance.setLogEntryPrototype(logEntryPrototype);
	}

	/**
	 * Escreve a mensagem em log, no nivel "info"
	 * @param message mensagem, ou opcionalmente uma string formato, estilo printf
	 * @param args argumentos da string formato
	 */
	public static void i(String message, Object...args){
		if (instance != null)
			instance.info(message, args);
	}

	/**
	 * Escreve a mensagem em log, no nivel "error"
	 * @param message mensagem, ou opcionalmente uma string formato, estilo printf
	 * @param args argumentos da string formato
	 */
	public static void e(String message, Object...args){
		if (instance != null)
			instance.error(message, args);
	}

	/**
	 * Escreve o stacktrace do erro em um log de nivel "error"
	 * @param t
	 */
	public static void e(Throwable t){
		if (instance != null)
			instance.error(t);
	}

	/**
	 * Faz a chamada a Log.d com a mensagem, ou string formato com argumentos
	 * @param message mensagem, ou opcionalmente uma string formato, estilo printf
	 * @param args argumentos da string formato
	 */
	public static void d(String message, Object...args){
		if (instance != null)
			instance.debug(message, args);
	}

}