package com.quantium.mobile.framework.communication;

import java.util.Map;

public abstract class SerializedCommunication extends GenericCommunication{

	/**
	 * Remove o mapa de parametros do corpo anterior e troca pelo mapa
	 * de parametros especificado.
	 *
	 * @param parameters novo mapa de parametros
	 */
	public abstract void setSerializedBodyData(Map<String,Object> bodyParameters);

	/**
	 * Altera o parametro para onde vai o corpo
	 *
	 * @param body chave onde vai o corpo
	 */
	public abstract void setSerializedBodyParameter(String parameter);

	/**
	 * Altera as chaves para o array de objetos no corpo
	 * da resposta.
	 *
	 * @param keys
	 */
	public abstract void setKeysToObjectList(String...keysToObject);

}
