package com.quantium.mobile.framework.communication;

import java.util.Iterator;

public interface ObjectListCommunicationResponse<T> extends SerializedCommunicationResponse {

	void setKeysToObjectList(String...keysToObject);
	Iterator<T> getIterator();
}
