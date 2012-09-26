package com.quantium.mobile.framework.communication;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.quantium.mobile.framework.utils.JSONUtils;

public class InnerJsonParametersSerializer extends ParametersSerializer{

	@Override
	public HttpEntity getEntity(Map<String, Object> map) throws UnsupportedEncodingException {
		List<NameValuePair> params = serialize(map);
		return new UrlEncodedFormEntity(params);
	}

	

	@Override
	public List<NameValuePair> serialize(Map<String, Object> map) {
		List<NameValuePair> params = new ArrayList<NameValuePair>(map.size());
		for (String k : map.keySet()){
			Object val = map.get(k);
			if (k != null || val != null)
				params.add( new BasicNameValuePair(
						k, JSONUtils.parseToJson(val).toString()));
		}
		return params;
	}

}
