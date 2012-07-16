package com.quantium.mobile.framework.communication;

import java.util.Iterator;

public interface SerializedCommunicationResponse<T> extends CommunicationResponse {

	void setKeysToObjectList(String...keysToObject);
	Iterator<T> getIterator();
}
