package com.quantium.mobile.framework.communication;

import com.quantium.mobile.framework.DAO;

import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

public interface SerializedCommunicationResponse {

    public Reader getReader();

    public Map<String, Object> getResponseMap();

    public <T> Iterator<T> getIterator(DAO<T> prototype, String... keysToObject);

    public Iterator<Object> getIterator(String... keysToObject);

}
