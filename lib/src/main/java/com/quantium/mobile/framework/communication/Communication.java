package com.quantium.mobile.framework.communication;

import java.util.Map;

public interface Communication {

	/**
	 * Altera o encoding da comunicação
	 *
	 * @param charset charset (default "UTF-8")
	 */
	public abstract void setCharset(String charset);

	/**
	 * Remove o mapa de parametros anterior e troca pelo mapa
	 * de parametros especificado.
	 *
	 * @param parameters novo mapa de parametros
	 */
	public abstract void setParameters(Map<String, String> parameters);

	/**
	 * Adiciona ou altera o parametro de especificado.
	 *
	 * @param key chave (nome do parametro)
	 * @param value valor do parametro
	 */
	public abstract void setParameter(String key, String value);

	/**
	 * Altera a url completa da requisicao
	 *
	 * @param url URL
	 */
	public abstract void setURL(String url);

}