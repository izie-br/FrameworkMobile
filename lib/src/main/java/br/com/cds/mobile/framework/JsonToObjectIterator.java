package br.com.cds.mobile.framework;

import java.io.Reader;
import java.util.Iterator;

import org.json.JSONException;

public class JsonToObjectIterator<T extends JsonSerializable<T>> implements Iterator<T>{

	private static final String ERR_PROTOTYPE_NULL = "Prototipo nao pode ser null";


	StreamJsonIterator jsonIterator;
	private T prototype;

	public JsonToObjectIterator(Reader reader,T prototype) {
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
		try {
			return prototype.jsonToObjectWithPrototype(jsonIterator.next());
		} catch (JSONException e) {
			throw new RuntimeException(e);   /* impossivel */
		}
	}

	@Override
	public void remove() {
		jsonIterator.remove();
	}

	

}
