package com.quantium.mobile.framework.communication;

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
	 * Altera o parametro para onde vai o corpo
	 *
	 * @param body chave onde vai o corpo
	 */
	public abstract void setSerializedBodyParameter(String parameter);


	public SerializedCommunicationResponse send () throws FrameworkException;

}
