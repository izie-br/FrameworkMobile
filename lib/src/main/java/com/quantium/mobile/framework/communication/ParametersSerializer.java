package com.quantium.mobile.framework.communication;

import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

public abstract class ParametersSerializer {

	public abstract List<NameValuePair> serialize(Map<String,Object> map);
	public abstract HttpEntity getEntity(Map<String,Object> map) throws Exception;

}
