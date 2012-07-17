package com.quantium.mobile.framework.communication;

import java.util.Iterator;

public interface ObjectListCommunication<T> extends SerializedCommunication{

	/**
	 * Altera as chaves para o array de objetos no corpo
	 * da resposta.
	 *
	 * @param keys
	 */
	public abstract void setKeysToObjectList(String...keysToObject);

	/**
	 *p Armazena um iterador de objetos para enviar
	 *
	 * @param iterator iterador de colecao de objetos pare enviar
	 */
	public abstract void setIterator(Iterator<T> iterator);

}
