package com.quantium.mobile.framework.communication;

import java.util.Iterator;

public abstract class ObjectListCommunication<T> extends SerializedCommunication{

	/**
	 *p Armazena um iterador de objetos para enviar
	 *
	 * @param iterator iterador de colecao de objetos pare enviar
	 */
	public abstract void setIterator(Iterator<T> iterator);

}
