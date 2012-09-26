package com.quantium.mobile.framework.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import com.quantium.mobile.framework.ErrorCode;
import com.quantium.mobile.framework.FrameworkException;
import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.utils.StringUtil;

public class JsonCommunication extends GenericCommunication
{
	private static final String ACCEPT_HEADER ="application/json";

	private String url;
	private Map<String,Object> parameters;
//	private CommunicationObjectList lists [];
	private String charset = StringUtil.DEFAULT_ENCODING;

	public void setCharset (String charset) {
		this.charset = charset;
	}

//	public void setParameters (Map<String,Object> parameters) {
//		this.parameters = parameters;
//	}

	public Map<String,Object> getParameters(){
		if (this.parameters == null)
			this.parameters = new HashMap<String,Object>(2);
		return this.parameters;
	}

	public void setParameter (String key, Object value) {
		this.getParameters().put(key, value);
	}

//	public void setSerializedBodyData (Map<String,Object> bodyParameters) {
//		this.bodyParameters = bodyParameters;
//	}
//
//	@Override
//	public void setSerializedParameter(String key, Object value) {
//		if (this.bodyParameters == null)
//			bodyParameters = new HashMap<String, Object>();
//		bodyParameters.put(key, value);
//	}

//	public void setSerializedBodyParameter(String body) {
//		this.body = body;
//	}

	public void setURL (String url) {
		this.url = url;
	}

//	public void setIterator(Iterator<?> iterator, String...keysToObjectList) {
//		if (iterator == null /*|| ! iterator.hasNext()*/)
//			return;
//		int listSize = lists == null ? 0 : lists.length;
//		CommunicationObjectList newlists[] = new CommunicationObjectList[listSize +1];
//		if (listSize > 0){
//			System.arraycopy(lists, 0, newlists, 0, listSize);
//		}
//		CommunicationObjectList list = new CommunicationObjectList();
//		list.iterator = iterator;
//		list.keys = keysToObjectList;
//		newlists[listSize] = list;
//		lists = newlists;
//	}

//	public Map<String,Object> getBodyParameters(){
//		if (this.bodyParameters == null)
//			this.bodyParameters = new HashMap<String,Object>(2);
//		return this.bodyParameters;
//	}
//
//	private static void putSerializedBodyParameters(
//			JSONObject target,
//			Map<?,?> parameters,
//			String except
//	)
//			throws JSONException
//	{
//		for (Object key : parameters.keySet()){
//			if (key.equals(except)){
//				LogPadrao.e(
//						"parametro ignorado %s",
//						parameters.get(key).toString()
//				);
//			} else {
//				target.put(key.toString(), parameters.get(key));
//			}
//		}
//
//	}

	

	public SerializedCommunicationResponse post()throws FrameworkException{
		return execute(POST);
	}

	public SerializedCommunicationResponse get()throws FrameworkException{
		return execute(GET);
	}

	private SerializedCommunicationResponse execute (byte method)
			throws FrameworkException{
		try{
			HttpResponse response = null;
			String exceptions [] = new String[CONNECTION_RETRY_COUNT+1];
			Map<String, Object> rawparams = getParameters();

//			if (body != null){
//				params.put(body, jsonRequestString());
//			}
			int connectionTries = 0;
			for(;;){
				try{
					switch(method){
					case GET:
						response = get(url, rawparams);
						break;
					case POST:
						response = post(url, rawparams);
						break;
					default:
						LogPadrao.e("metodo http incorreto");
					}
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
//		} catch (JSONException e) {
//			LogPadrao.e(e);
//			throw new FrameworkException(ErrorCode.UNKNOWN_EXCEPTION, e);
		} catch (IOException e) {
			LogPadrao.e(e);
			throw new FrameworkException(ErrorCode.UNKNOWN_EXCEPTION, e);
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

//	public String jsonRequestString() throws JSONException{
//			JSONObject obj = new JSONObject();
//			Map<?,?> bodyMap = bodyParameters;
//			JSONObject current = obj;
//
//			String keysToObjectList []= lists == null ? null :lists[0].keys;
//			LogPadrao.d("json:: %d", lists == null ? 0 : lists.length);
//
//			if (keysToObjectList == null || keysToObjectList.length ==0) {
//				if (bodyMap != null )
//					putSerializedBodyParameters(obj, bodyMap, null);
////				else
////					LogPadrao.e("ignorando iterador");
//			} else {
//				for (int i = 0; ; i++) {
//					if (bodyMap != null ){
//						putSerializedBodyParameters(obj, bodyMap, keysToObjectList[i]);
//						Object innerMap = bodyMap.get(keysToObjectList[i]);
//						bodyMap = (innerMap != null && innerMap instanceof Map) ?
//								//
//								(Map<?,?>)innerMap :
//								//
//								null;
//					}
//
//					if (i == (keysToObjectList.length -1)) {
//						current.put(keysToObjectList[i], getJsonArray(lists[0].iterator));
//						break;
//					}
//					current = new JSONObject();
//					obj.put(keysToObjectList[i], current);
//				}
//			}
//			return obj.toString();
//
//		}

//		private JSONArray getJsonArray(Iterator<?> iterator){
//			JSONArray array = new JSONArray();
//			while (iterator.hasNext()){
//				Object obj = iterator.next();
//				if (! (obj instanceof MapSerializable) ){
//					LogPadrao.e("%s %s nao eh MapSerializable", obj.getClass().getName(), obj.toString());
//				} else {
//					MapSerializable<?> jsonObj = (MapSerializable<?>)obj;
//					array.put(JSONUtils.mapToJson(jsonObj.toMap()));
//				}
//			}
//			return array;
//		}
//
//	private static class CommunicationObjectList {
//			String keys [];
//			Iterator<?> iterator;
//		}


	public String getAcceptHeader() {
		return ACCEPT_HEADER;
	}


}
