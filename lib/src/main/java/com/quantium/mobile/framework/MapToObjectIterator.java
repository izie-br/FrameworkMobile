package com.quantium.mobile.framework;

import java.io.Reader;
import java.util.Iterator;

import com.quantium.mobile.framework.utils.JSONUtils;

public class MapToObjectIterator<T extends MapSerializable<T>> implements Iterator<T>{

	private static final String ERR_PROTOTYPE_NULL = "Prototipo nao pode ser null";


	StreamJsonIterator jsonIterator;
	private T prototype;

	public MapToObjectIterator(Reader reader,T prototype) {
		jsonIterator = new StreamJsonIterator(reader);
		this.prototype = prototype;
		if(prototype==null)
			throw new RuntimeException(ERR_PROTOTYPE_NULL);
	}

	@Override
	public boolean hasNext() {
		return jsonIterator.hasNext();
	}

	@Override
	public T next() {
		return prototype.mapToObjectWithPrototype(
				JSONUtils.desserializeJsonObject(jsonIterator.next()));
	}

	@Override
	public void remove() {
		jsonIterator.remove();
	}

	

}
