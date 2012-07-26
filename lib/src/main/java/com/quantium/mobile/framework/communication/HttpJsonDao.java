package com.quantium.mobile.framework.communication;

import java.util.Iterator;

import org.json.JSONArray;

import com.quantium.mobile.framework.FrameworkException;
import com.quantium.mobile.framework.JsonSerializable;

public class HttpJsonDao<T extends JsonSerializable<T>>
		extends JsonCommunication implements ObjectListCommunication<T>
{

	private T prototype;
	private Iterator<T> iterator;

	/**
	 * @see HttpJsonDao#setPrototype(JsonSerializable)
	 */
	public HttpJsonDao (T prototype) {
		this.prototype = prototype;
	}

	/**
	 * @see HttpJsonDao#setIterator(Iterator)
	 */
	public HttpJsonDao (Iterator<T> iterator) {
		this.iterator = iterator;
	}

	/**
	 * <p>
	 *   Armazena o prototipo para deserializacao dos objetos
	 *   recebidos comunicacao.
	 * </p>
	 * <p>
	 *   O metodo destes objetos é chamado, e seus atributos serão
	 *   valores padrão na deserialização.
	 * </p>
	 * <p>
	 *   Se for NULL, não será feita a deserialização.
	 * </p>
	 *
	 * @param prototype prototipo para deserialização
	 */
	public void setPrototype (T prototype) {
		this.prototype = prototype;
	}

	public void setIterator (Iterator<T> iterator) {
		this.iterator = iterator;
	}

	@Override
	protected JSONArray objectList() {
		JSONArray array = new JSONArray();
		if (iterator != null) {
			while (iterator.hasNext()) {
				T item = iterator.next();
				array.put(item.toJson());
			}
		}
		return array;
	}

	@Override
	public void setKeysToObjectList(String...keys) {
		super._setKeysToObjectList(keys);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObjectListCommunicationResponse<T> send () throws FrameworkException{
		JsonCommunicationResponse<T> resp =
				(JsonCommunicationResponse<T>) super.send();
		resp.setPrototype(prototype);
		return resp;
	}

}
