package com.quantium.mobile.framework.communication;

import java.io.Reader;
import java.util.Map;

interface SerializedCommunicationResponse {

	public Reader getReader();
	public Map<String, Object> getResponseMap();

}
