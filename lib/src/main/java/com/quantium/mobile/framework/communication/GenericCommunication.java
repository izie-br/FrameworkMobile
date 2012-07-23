package com.quantium.mobile.framework.communication;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.quantium.mobile.framework.ErrorCode;
import com.quantium.mobile.framework.FrameworkException;
import com.quantium.mobile.framework.Tarefa;
import com.quantium.mobile.framework.logging.LogPadrao;


public abstract class GenericCommunication implements Communication {

	private static final int DEFAULT_BUFFER = 1024;
	protected static final int CONNECTION_RETRY_COUNT = 5;
	private static final int SO_TIMEOUT = 90000;
	private static final int CONNECTION_TIMEOUT = 15000;
	protected static final String USER_AGENT = "quantium_mobile";

	private static boolean connected = true;
	private static ConnectionStatusChangeListener connectionListeners[];

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
				listener.onConectado();
			else
				listener.onDesconectado();
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

	/* (non-Javadoc)
	 * @see com.quantium.mobile.framework.communication.Communication#setCharset(java.lang.String)
	 */
	@Override
	public abstract void setCharset (String charset);

	/* (non-Javadoc)
	 * @see com.quantium.mobile.framework.communication.Communication#setParameters(java.util.Map)
	 */
	@Override
	public abstract void setParameters (Map<String,String> parameters);

	/* (non-Javadoc)
	 * @see com.quantium.mobile.framework.communication.Communication#setParameter(java.lang.String, java.lang.String)
	 */
	@Override
	public abstract void setParameter (String key, String value);

	/* (non-Javadoc)
	 * @see com.quantium.mobile.framework.communication.Communication#setURL(java.lang.String)
	 */
	@Override
	public abstract void setURL (String url);

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
		return "application/x-www-form-urlencoded";
	}

	public HttpResponse post(String url, Map<String, String> parametros)
	throws IOException
	{
		HttpResponse response = null;

		try {
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, getConnectionTimeout());
			HttpConnectionParams.setSoTimeout(httpParameters, getSoTimeout());
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("User-Agent", getUserAgent());
			httpPost.setHeader("Accept", getAcceptHeader());
			httpPost.setHeader("Content-Type", getContentType());
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			if (parametros != null) {
				Iterator<String> iterator = parametros.keySet().iterator();
				while (iterator.hasNext()) {
					String chave = iterator.next();
					String valor = parametros.get(chave);
					nameValuePairs.add(new BasicNameValuePair(chave, valor));
				}
			}
			// TODO criar um entity aqui
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			response = httpclient.execute(httpPost);
		} catch (IOException e) {
			GenericCommunication.setConnected(false);
			throw e;
		}
		GenericCommunication.setConnected(true);
		return response;
	}


	public static boolean downloadFile(
			String urlString, String path, String arquivo,
			Tarefa<?, ?> tarefa, String sincronia
	) throws FrameworkException {
		try {
			// LogPadrao.d("Com:" + urlString);
			HttpURLConnection c;
			try {
				URL url = new URL(urlString);
				c = (HttpURLConnection) url.openConnection();
				c.setRequestMethod("GET");
				c.setDoOutput(true);
			} catch (MalformedURLException e) {
				LogPadrao.e(e);
				return false;
			} catch (ProtocolException e){
				LogPadrao.e (e);
				return false;
			}
			// LogPadrao.d("c.getContentLength():" + c.getContentLength());
			long contentLength = c.getContentLength();
	
			File folder = new File(path);
			folder.mkdirs();
			File file = new File(folder, arquivo);
			if (!file.exists()) {
				file.createNewFile();
			} else {
				if ( file.length() == c.getContentLength())
					return false;
				throw new FrameworkException(ErrorCode.UNABLE_TO_CREATE_EXISTING_FILE);
			}
			// LogPadrao.d("outputFile.exists():" + outputFile.exists());
			// LogPadrao.d("outputFile.length():" + outputFile.length());
			// LogPadrao.d("c.getContentLength():" + c.getContentLength());
			InputStream is = c.getInputStream();;
			c.connect();
			FileOutputStream fos = new FileOutputStream(file);
	
			byte[] buffer = new byte[DEFAULT_BUFFER];
			int len1 = 0;
			int i = 0;
			while ((len1 = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len1);
				// LogPadrao.d("i++, (int) contentLength / 1024:" + (i++) +
				// "/"
				// +
				// ((int) contentLength / 1024));
				if (tarefa != null) {
					if (contentLength > 0) {
						tarefa.publicaProgresso(i++/(contentLength/1024));
					} else {
						tarefa.publicaProgresso(i++/(8000/1024));
					}
				}
			}
			fos.close();
			is.close();
			// TODO mover isto para cima
			if (!GenericCommunication.isConnected())
				GenericCommunication.connected = true;
			return true;
		} catch (IOException e){
			throw new RuntimeException(e);
		}
	}


}
