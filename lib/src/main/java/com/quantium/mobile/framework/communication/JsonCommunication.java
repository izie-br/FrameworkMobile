package com.quantium.mobile.framework.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
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
import com.quantium.mobile.framework.JsonSerializable;
import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.utils.StringUtil;

public class JsonCommunication extends GenericCommunication
		implements SerializedCommunication
{
	private static final String ACCEPT_HEADER ="application/json";

	private String url;
	private Map<String,String> parameters;
	private Map<String,Object> bodyParameters;
	private String body;
	private CommunicationObjectList lists [];
	private String charset = StringUtil.DEFAULT_ENCODING;

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

	@Override
	public void setSerializedParameter(String key, Object value) {
		if (this.bodyParameters == null)
			bodyParameters = new HashMap<String, Object>();
		bodyParameters.put(key, value);
	}

	public void setSerializedBodyParameter(String body) {
		this.body = body;
	}

	public void setURL (String url) {
		this.url = url;
	}

	public void setIterator(Iterator<?> iterator, String...keysToObjectList) {
		if (iterator == null /*|| ! iterator.hasNext()*/)
			return;
		int listSize = lists == null ? 0 : lists.length;
		CommunicationObjectList newlists[] = new CommunicationObjectList[listSize +1];
		if (listSize > 0){
			System.arraycopy(lists, 0, newlists, 0, listSize);
		}
		CommunicationObjectList list = new CommunicationObjectList();
		list.iterator = iterator;
		list.keys = keysToObjectList;
		newlists[listSize] = list;
		lists = newlists;
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

	public SerializedCommunicationResponse post () throws FrameworkException{
		try{
			HttpResponse response = null;
			String exceptions [] = new String[CONNECTION_RETRY_COUNT+1];
			Map<String, String> params = getParameters();

			// refazer isso
			if (!DEFAULT_CONTENT_TYPE.equals(getContentType()))
				body = BODY_ONLY_PARAMETER;
			if (body != null){
				params.put(body, jsonRequestString());
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
					getReader(response)
			);
		} catch (JSONException e) {
			LogPadrao.e(e);
			throw new FrameworkException(ErrorCode.UNKNOWN_EXCEPTION);
		} catch (IOException e) {
			LogPadrao.e(e);
			throw new FrameworkException(ErrorCode.UNKNOWN_EXCEPTION);
		}
	}

	protected Reader getReader ( HttpResponse response)
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

	public String jsonRequestString() throws JSONException{
			JSONObject obj = new JSONObject();
			Map<?,?> bodyMap = bodyParameters;
			JSONObject current = obj;

			String keysToObjectList []= lists == null ? null :lists[0].keys;
			LogPadrao.d("json:: %d", lists == null ? 0 : lists.length);

			if (keysToObjectList == null || keysToObjectList.length ==0) {
				if (bodyMap != null )
					putSerializedBodyParameters(obj, bodyMap, null);
//				else
//					LogPadrao.e("ignorando iterador");
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
						current.put(keysToObjectList[i], getJsonArray(lists[0].iterator));
						break;
					}
					current = new JSONObject();
					obj.put(keysToObjectList[i], current);
				}
			}
			return obj.toString();

		}

		private JSONArray getJsonArray(Iterator<?> iterator){
			JSONArray array = new JSONArray();
			while (iterator.hasNext()){
				Object obj = iterator.next();
				if (! (obj instanceof JsonSerializable) ){
					LogPadrao.e("%s %s nao eh jsonserializable", obj.getClass().getName(), obj.toString());
				} else {
					JsonSerializable<?> jsonObj = (JsonSerializable<?>)obj;
					array.put(jsonObj.toJson());
				}
			}
			return array;
		}

	private static class CommunicationObjectList {
			String keys [];
			Iterator<?> iterator;
		}


	public String getAcceptHeader() {
		return ACCEPT_HEADER;
	}


}
