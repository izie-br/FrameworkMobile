package com.quantium.mobile.framework.communication;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import java.util.List;
import java.util.Map;

public abstract class ParametersSerializer {

    public abstract List<NameValuePair> serialize(Map<String, Object> map);

    public abstract HttpEntity getEntity(Map<String, Object> map) throws Exception;

}
