package com.quantium.mobile.framework.communication;

public interface Communication {

	/**
	 * Altera o encoding da comunicação
	 *
	 * @param charset charset (default "UTF-8")
	 */
	public abstract void setCharset(String charset);

	/**
	 * Adiciona ou altera o parametro de especificado.
	 *
	 * @param key chave (nome do parametro)
	 * @param value valor do parametro
	 */
	public abstract void setParameter(String key, Object value);

	/**
	 * Altera a url completa da requisicao
	 *
	 * @param url URL
	 */
	public abstract void setURL(String url);

	public SerializedCommunicationResponse post () throws RuntimeException;
	public SerializedCommunicationResponse get () throws RuntimeException;
}