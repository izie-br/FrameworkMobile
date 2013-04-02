package com.quantium.mobile.framework.communication;


import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.quantium.mobile.framework.logging.LogPadrao;


public abstract class GenericCommunication implements Communication {

	protected static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";
//	private static final int DEFAULT_BUFFER = 1024;
	protected static final int CONNECTION_RETRY_COUNT = 5;
	private static final int SO_TIMEOUT = 90000;
	private static final int CONNECTION_TIMEOUT = 15000;
	protected static final String USER_AGENT = "quantium_mobile";

	public static final byte GET = 0;
	public static final byte POST = 1;
	public static final byte PUT = 2;
	public static final byte DELETE = 3;
//	private static final String UNABLE_TO_CREATE_EXISTING_FILE =
//			"UNABLE_TO_CREATE_EXISTING_FILE";

	private static boolean connected = true;
	private static ConnectionStatusChangeListener connectionListeners[];
	private ParametersSerializer serializer =
			new IndexedKeyParametersSerializer("%1$s[%2$s]");
	private String contentType = DEFAULT_CONTENT_TYPE;

	/**
	 * Retorna o estado da conexao
	 * 
	 * @return
	 */
	public static boolean isConnected() {
		return connected;
	}

	/**
	 * Setter de estado da conexao. Executa os listeners de alteracao de estado
	 * de conexao
	 */
	public static void setConnected(boolean connected) {
		// se nao ha mudanca: return
		if (connected == GenericCommunication.connected)
			return;
		GenericCommunication.connected = connected;
		// se nao ha listeners, retorna
		if (connectionListeners == null)
			return;
		// execucao dos listeners
		for (ConnectionStatusChangeListener listener : connectionListeners) {
			if (connected)
				listener.onConnected();
			else
				listener.onDisconnected();
		}
	}

	public static void addConnectionStatusChangeListener(ConnectionStatusChangeListener listener) {
		int oldSize = connectionListeners == null ? 0 : connectionListeners.length;
		ConnectionStatusChangeListener old[] = connectionListeners;
		connectionListeners = new ConnectionStatusChangeListener[ oldSize+1 ];
//		for(int i=0;i<size;i++)
//			mecListeners[i] = old[i];
		System.arraycopy(old, 0, connectionListeners, 0, oldSize);
		connectionListeners[oldSize] = listener;
	}

	public abstract String getAcceptHeader();
	public abstract Map<String, Object> getParameters();
	public abstract void setParameter(String key, Object value);

	public String getUserAgent(){
		return USER_AGENT;
	}

	public int getConnectionTimeout() {
		return CONNECTION_TIMEOUT;
	}

	public int getSoTimeout() {
		return SO_TIMEOUT;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public ParametersSerializer getParameterSerializer(){
		return serializer;
	}

	public void setParameterSerializer(ParametersSerializer s){
		this.serializer = s;
	}

	protected HttpResponse get(String url, Map<String, Object> parametros)
			throws IOException
	{
		HttpResponse response = execute(GET, url, parametros);
		return response;
	}

	protected HttpResponse post(String url, Map<String, Object> parametros)
	throws IOException
	{
		HttpResponse response = execute(POST, url, parametros);
		return response;
	}

	protected HttpResponse put(String url, Map<String, Object> parametros)
	throws IOException
	{
		HttpResponse response = execute(PUT, url, parametros);
		return response;
	}
	
	protected HttpResponse delete(String url, Map<String, Object> parametros)
	throws IOException
	{
		HttpResponse response = execute(DELETE, url, parametros);
		return response;
	}	
	protected HttpResponse execute(byte method, String url,
			Map<String, Object> parametros)
			throws IOException
	{
		if(url == null){
			throw new IllegalArgumentException("URL cannot be null");
		}
		HttpResponse response = null;

		try {
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, getConnectionTimeout());
			HttpConnectionParams.setSoTimeout(httpParameters, getSoTimeout());
			HttpClient httpclient = getHttpClient(httpParameters);
			String contentType = getContentType();

			HttpRequestBase httpRequest = requestForMethod(method);
			httpRequest.setURI(URI.create(url));
			httpRequest.setHeader("User-Agent", getUserAgent());
			httpRequest.setHeader("Accept", getAcceptHeader());
			ParametersSerializer serializer = getParameterSerializer();
			setRequestParameters(method, httpRequest, parametros, contentType, serializer);

			response = httpclient.execute(httpRequest);
		} catch (IOException e) {
			GenericCommunication.setConnected(false);
			throw e;
		} catch (Exception e){
			LogPadrao.e(e);
			throw new RuntimeException(e);
		}
		GenericCommunication.setConnected(true);
		return response;
	}

	public HttpClient getHttpClient(HttpParams httpParameters) {
		HttpClient httpclient = new DefaultHttpClient(httpParameters);
		return httpclient;
	}

	protected void setRequestParameters(
			byte method, HttpRequestBase httpRequest,
			Map<String, Object> parametros, String contentType,
			ParametersSerializer serializer)
			throws Exception {
		switch(method){
		case GET:
		case DELETE:
			HttpRequestBase httpGet = (HttpRequestBase)httpRequest;
			URI uri = httpGet.getURI();
			String url = String.format(
					"%s://%s%s",
					uri.getScheme(), uri.getAuthority(), uri.getPath());
			StringBuilder urlAndQstr = new StringBuilder(url);
			boolean first = true;
			URLCodec codec = new URLCodec();
			if (parametros != null) {
				List<NameValuePair> paramlist = serializer.serialize(parametros);
				Iterator<NameValuePair> iterator = paramlist.iterator();
				while (iterator.hasNext()) {
					NameValuePair pair = iterator.next();
					String chave = pair.getName();
					String valor = pair.getValue();
					if (chave == null || valor == null)
						continue;
					//
					urlAndQstr.append( (first)? '?' : '&');
					first = false;
					//
					try {
						urlAndQstr.append(codec.encode(chave));
						urlAndQstr.append('=');
						urlAndQstr.append(codec.encode(valor));
					} catch (EncoderException e) {
						throw new RuntimeException(e);
					}
				}
			}
			httpGet.setURI(URI.create(urlAndQstr.toString()));
			break;
		default:
			HttpEntityEnclosingRequest httpPost = (HttpEntityEnclosingRequest)httpRequest;
			httpPost.setHeader("Content-Type", contentType);
			if (parametros != null) {
				httpPost.setEntity(serializer.getEntity(parametros));
			}
		}
	}

	private static HttpRequestBase requestForMethod(byte method){
		if (method == GET)
			return new HttpGet();
		if (method == PUT)
			return new HttpPut();
		if (method == POST )
			return new HttpPost();
		if (method == DELETE)
			return new HttpDelete();
		throw new RuntimeException();
	}

//	public static boolean downloadFile(
//			String urlString, String path, String arquivo,
//			Tarefa<?, ?> tarefa, String sincronia
//	) throws RuntimeException {
//		try {
//			// LogPadrao.d("Com:" + urlString);
//			HttpURLConnection c;
//			try {
//				URL url = new URL(urlString);
//				c = (HttpURLConnection) url.openConnection();
//				c.setRequestMethod("GET");
//				c.setDoOutput(true);
//			} catch (MalformedURLException e) {
//				LogPadrao.e(e);
//				return false;
//			} catch (ProtocolException e){
//				LogPadrao.e (e);
//				return false;
//			}
//			// LogPadrao.d("c.getContentLength():" + c.getContentLength());
//			long contentLength = c.getContentLength();
//	
//			File folder = new File(path);
//			folder.mkdirs();
//			File file = new File(folder, arquivo);
//			if (!file.exists()) {
//				file.createNewFile();
//			} else {
//				if ( file.length() == c.getContentLength())
//					return false;
//				throw new RuntimeException(UNABLE_TO_CREATE_EXISTING_FILE);
//			}
//			// LogPadrao.d("outputFile.exists():" + outputFile.exists());
//			// LogPadrao.d("outputFile.length():" + outputFile.length());
//			// LogPadrao.d("c.getContentLength():" + c.getContentLength());
//			InputStream is = c.getInputStream();;
//			c.connect();
//			FileOutputStream fos = new FileOutputStream(file);
//	
//			byte[] buffer = new byte[DEFAULT_BUFFER];
//			int len1 = 0;
//			int i = 0;
//			while ((len1 = is.read(buffer)) != -1) {
//				fos.write(buffer, 0, len1);
//				// LogPadrao.d("i++, (int) contentLength / 1024:" + (i++) +
//				// "/"
//				// +
//				// ((int) contentLength / 1024));
//				if (tarefa != null) {
//					if (contentLength > 0) {
//						tarefa.publicaProgresso(i++/(contentLength/1024));
//					} else {
//						tarefa.publicaProgresso(i++/(8000/1024));
//					}
//				}
//			}
//			fos.close();
//			is.close();
//			// TODO mover isto para cima
//			if (!GenericCommunication.isConnected())
//				GenericCommunication.connected = true;
//			return true;
//		} catch (IOException e){
//			throw new RuntimeException(e);
//		}
//	}


}
