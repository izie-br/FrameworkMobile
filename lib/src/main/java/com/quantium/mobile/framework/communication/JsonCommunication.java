package com.quantium.mobile.framework.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quantium.mobile.framework.ErrorCode;
import com.quantium.mobile.framework.FrameworkException;
import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.utils.StringUtil;

public class JsonCommunication extends GenericCommunication
		implements SerializedCommunication
{
	private static final String ACCEPT_HEADER ="application/json";

	private String url;
	private Map<String,String> parameters;
	private Map<String,Object> bodyParameters;
	private String body = "json";
	private String keysToObjectList [];
	private String charset = StringUtil.DEFAULT_ENCODING;

	protected void _setKeysToObjectList(String keys[]) {
		this.keysToObjectList = keys;
	}
	protected JSONArray objectList(){
		return null;
	}

	public void setCharset (String charset) {
		this.charset = charset;
	}

	public void setParameters (Map<String,String> parameters) {
		this.parameters = parameters;
	}

	public void setParameter (String key, String value) {
		this.getParameters().put(key, value);
	}

	public void setSerializedBodyData (Map<String,Object> bodyParameters) {
		this.bodyParameters = bodyParameters;
	}

	public void setSerializedBodyParameter(String body) {
		this.body = body;
	}

	public void setURL (String url) {
		this.url = url;
	}

	public Map<String,Object> getBodyParameters(){
		if (this.bodyParameters == null)
			this.bodyParameters = new HashMap<String,Object>(2);
		return this.bodyParameters;
	}

	public Map<String,String> getParameters(){
		if (this.parameters == null)
			this.parameters = new HashMap<String,String>(2);
		return this.parameters;
	}

	private static void putSerializedBodyParameters(
			JSONObject target,
			Map<?,?> parameters,
			String except
	)
			throws JSONException
	{
		for (Object key : parameters.keySet()){
			if (key.equals(except)){
				LogPadrao.e(
						"parametro ignorado %s",
						parameters.get(key).toString()
				);
			} else {
				target.put(key.toString(), parameters.get(key));
			}
		}

	}

	public SerializedCommunicationResponse send () throws FrameworkException{
		try{
			HttpResponse response = null;
			String exceptions [] = new String[CONNECTION_RETRY_COUNT+1];
			JSONArray objectList = objectList();
			Map<String, String> params = getParameters();

			if (
					objectList != null &&
					(keysToObjectList == null || keysToObjectList.length ==0)
			) {
				params.put(body,objectList.toString());
				if (params != null && params.size() > 0)
					LogPadrao.e("parametros ignorados no envio");
			} else {
				JSONObject obj = new JSONObject();
				Map<?,?> bodyMap = bodyParameters;
				JSONObject current = obj;
				if (keysToObjectList == null || keysToObjectList.length ==0) {
					if (bodyMap != null )
						putSerializedBodyParameters(obj, bodyMap, null);
				} else {
					for (int i = 0; ; i++) {
						if (bodyMap != null ){
							putSerializedBodyParameters(obj, bodyMap, keysToObjectList[i]);
							Object innerMap = bodyMap.get(keysToObjectList[i]);
							bodyMap = (innerMap != null && innerMap instanceof Map) ?
									//
									(Map<?,?>)innerMap :
									//
									null;
						}
	
						if (i == (keysToObjectList.length -1)) {
							current.put(keysToObjectList[i], objectList);
							break;
						}
						current = new JSONObject();
						obj.put(keysToObjectList[i], current);
					}
				}
				params.put(body, obj.toString());
			}
			int connectionTries = 0;
			for(;;){
				try{
					response = post(url, params);
				} catch (RuntimeException e){
					exceptions[connectionTries] = LogPadrao.getStackTrace(e);
					connectionTries++;
				}
				if(response!=null)
					break;
				if(connectionTries>CONNECTION_RETRY_COUNT){
					for (int j = 0; j < connectionTries; j++)
						LogPadrao.e(exceptions[j]);
					throw new FrameworkException(ErrorCode.NETWORK_COMMUNICATION_ERROR);
				}
			}
			return new JsonCommunicationResponse(
					getReader(response),
					keysToObjectList
			);
		} catch (JSONException e) {
			LogPadrao.e(e);
			throw new FrameworkException(ErrorCode.UNKNOWN_EXCEPTION);
		} catch (IOException e) {
			LogPadrao.e(e);
			throw new FrameworkException(ErrorCode.UNKNOWN_EXCEPTION);
		}
	}

	protected InputStreamReader getReader ( HttpResponse response)
			throws IOException
	{
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			InputStream instream = entity.getContent();
			Header contentEncoding = response.getFirstHeader("Content-Encoding");
			if (contentEncoding != null && contentEncoding.getValue().toLowerCase().contains("gzip")) {
				instream = new GZIPInputStream(instream);
			}
			try {
				InputStreamReader reader =
						new InputStreamReader(instream, charset);
				return reader;
			} catch (UnsupportedEncodingException e) {
				LogPadrao.e (e);
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}

	public String getAcceptHeader() {
		return ACCEPT_HEADER;
	}


}
