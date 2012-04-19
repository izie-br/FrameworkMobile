package br.com.cds.mobile.framework;

import android.os.AsyncTask;

public abstract class Tarefa<U, S> extends AsyncTask<U, Integer, S> {

	
	private Throwable error;

	public Throwable getError(){
		return error;
	}

	/**
	 * Preenche a barra de progresso
	 * @param clampf double entre 0 e 1
	 */
	public abstract void publicaProgresso(double clampf);

}
