package com.quantium.mobile.framework.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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


public class HttpJsonDao<T extends JsonSerializable<T>> extends ObjectListCommunication<T> {

	private static final String ACCEPT_HEADER ="application/json";

	private String url;
	private T prototype;
	private Iterator<T> iterator;
	private Map<String,String> parameters;
	private Map<String,Object> bodyParameters;
	private String body;
	private String keysToObjectArray [];
	private String charset = StringUtil.DEFAULT_ENCODING;

	/**
	 * @see HttpJsonDao#setPrototype(JsonSerializable)
	 */
	public HttpJsonDao (T prototype) {
		this.prototype = prototype;
	}

	/**
	 * @see HttpJsonDao#setIterator(Iterator)
	 */
	public HttpJsonDao (Iterator<T> iterator) {
		this.iterator = iterator;
	}


	public void setCharset (String charset) {
		this.charset = charset;
	}

	/**
	 * <p>
	 *   Armazena o prototipo para deserializacao dos objetos
	 *   recebidos comunicacao.
	 * </p>
	 * <p>
	 *   O metodo destes objetos é chamado, e seus atributos serão
	 *   valores padrão na deserialização.
	 * </p>
	 * <p>
	 *   Se for NULL, não será feita a deserialização.
	 * </p>
	 *
	 * @param prototype prototipo para deserialização
	 */
	public void setPrototype (T prototype) {
		this.prototype = prototype;
	}

	public void setIterator (Iterator<T> iterator) {
		this.iterator = iterator;
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

	public void setKeysToObjectList(String...keys) {
		this.keysToObjectArray = keys;
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
			if (key != except){
				target.put(key.toString(), parameters.get(key));
			}
		}

	}

	public ObjectListCommunicationResponse<T> send () throws FrameworkException{
		try{
			HttpResponse response = null;
			int connectionTries = 0;
			for(;;){
				String exceptions [] = new String[CONNECTION_RETRY_COUNT+1];
				try{
					JSONArray jsonarray = null;
					if (iterator != null ){
						jsonarray = new JSONArray();
						while (iterator.hasNext()) {
							jsonarray.put(iterator.next().toJson());
						}
						String json;
						if (keysToObjectArray == null || keysToObjectArray.length ==0) {
							json = jsonarray.toString();
						} else {
							JSONObject obj = new JSONObject();
							Map<?,?> bodyMap = bodyParameters;
							JSONObject current = obj;
							for (int i = 0; ; i++) {
								if (bodyMap != null ){
									putSerializedBodyParameters(obj, bodyMap, keysToObjectArray[i]);
									Object innerMap = bodyMap.get(keysToObjectArray[i]);
									bodyMap = (innerMap != null && innerMap instanceof Map) ?
											//
											(Map<?,?>)innerMap :
											//
											null;
								}

								if (i == keysToObjectArray.length -1) {
									current.put(keysToObjectArray[i], jsonarray);
									break;
								}
								current = new JSONObject();
								obj.put(keysToObjectArray[i], current);
							}
							json = obj.toString();
						}
						getParameters().put(body, json);
					}
					response = post(url, getParameters());
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
			return new JsonCommunicationResponse<T>(
					getReader(response),
					prototype,
					keysToObjectArray
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

	protected T jsonToObject(T prototype, JSONObject jsonObject) throws JSONException{
		return prototype.jsonToObjectWithPrototype(jsonObject);
	}

	protected JSONObject objetoToJson(T objeto){
		return objeto.toJson();
	}

	public String getAcceptHeader() {
		return ACCEPT_HEADER;
	}

}
