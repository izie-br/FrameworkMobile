package com.quantium.mobile.framework.communication;

import java.util.Iterator;
import java.util.Map;

import com.quantium.mobile.framework.FrameworkException;

public interface SerializedCommunication extends Communication{

	/**
	 * Remove o mapa de parametros do corpo anterior e troca pelo mapa
	 * de parametros especificado.
	 *
	 * @param parameters novo mapa de parametros
	 */
	public abstract void setSerializedBodyData(Map<String,Object> bodyParameters);

	/**
	 * Altera o parametro para onde o corpo sera inserido
	 *
	 * @param body chave onde vai o corpo
	 */
	public abstract void setSerializedBodyParameter(String parameter);

	/**
	 * Adiciona um par key/value ao corpo serializado
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void setSerializedParameter(String key, Object value);

	public void setIterator(Iterator<?> iterator, String...keysToObjectList);

	public SerializedCommunicationResponse post () throws FrameworkException;

}
