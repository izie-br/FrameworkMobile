package com.quantium.mobile.framework.communication;

import java.io.Reader;
import java.util.Iterator;

import com.quantium.mobile.framework.JsonSerializable;

public class HttpJsonDaoResponse<T extends JsonSerializable<T>> extends JsonCommunicationResponse
	implements ObjectListCommunicationResponse<T>{

	public HttpJsonDaoResponse(Reader reader, T prototype, String...keysToObject) {
		super(reader, keysToObject);
		super.setPrototype(prototype);
	}

	@Override
	public Iterator<T> getIterator() {
		@SuppressWarnings("unchecked")
		Iterator<T> iterator = (Iterator<T>) super.getIterator();
		return iterator;
	}

	@Override
	public void setKeysToObjectList(String... keysToObject) {
		super.setKeysToObjectList(keysToObject);
	}

}
