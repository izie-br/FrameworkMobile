package br.com.cds.mobile.framework.comunication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.cds.mobile.framework.ErrorCode;
import br.com.cds.mobile.framework.FrameworkException;
import br.com.cds.mobile.framework.JsonSerializable;
import br.com.cds.mobile.framework.JsonToObjectIterator;
import br.com.cds.mobile.framework.logging.LogPadrao;
import br.com.cds.mobile.framework.utils.StringUtil;

public class HttpJsonDao<T extends JsonSerializable<T>> extends GenericComunicacao {

	private static final int SO_TIMEOUT = 90000;
	private static final int CONNECTION_TIMEOUT = 15000;
	private static final String LAST_SYNC = "dtUltimaAtualizacao";
	private static final String PASSWORD = "senha";
	private static final String LOGIN = "email";
	private static final String MAC = "mac";
	private static final String IMEI = "imei";
	private static final String JSON = "json";

	// TODO firefox - ubuntu - i686!  ???? o_O
	private static final String USER_AGENT =
			"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6)"+
			" Gecko/20061201 Firefox/2.0.0.6 (Ubuntu-feisty)";
	private static final String ACCEPT_HEADER =
			"text/html,application/xml,application/xhtml+xml,"+
			"text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";


	//TODO paginar
	//private int page;
	//private int offset;
	private String url;
	private String methodGet;
	private String methodPost;
	private String encoding = StringUtil.DEFAULT_ENCODING;

	public HttpJsonDao(
			String url, String methodGet, String methodPost,
			T prototype
	) {
		super();
		this.url = url;
		this.methodGet = methodGet;
		this.methodPost = methodPost;
	}

	/**
	 * Override para metodo "get" dinamico
	 */
	protected String getMethodGet(){
		return methodGet;
	}

	/**
	 * Override para metodo "post" dinamico
	 */
	protected String getMethodPost(){
		return methodPost;
	}

	protected String getUrl(){
		return url;
	}

	public Iterator<T> query(HashMap<String, String> parametros,T prototype) throws FrameworkException{
		try{
			HttpResponse response = null;
			int i = 1;
			for(;;){
				try{
				response = post(getUrl(), getMethodGet(), parametros);
				} catch (RuntimeException e){
				}
				i++;
				if(response!=null)
					break;
				if(i>TENTATIVAS_DE_CONEXAO)
					throw new FrameworkException(ErrorCode.NETWORK_COMMUNICATION_ERROR);
			}
			return getObjectsFromResponse(prototype, response);
		} catch (IOException e) {
			LogPadrao.e(e);
			throw new FrameworkException(ErrorCode.UNKNOWN_EXCEPTION);
		}

	}

	protected Iterator<T> getObjectsFromResponse(T prototype,
			HttpResponse response) throws IOException {
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			InputStream instream = entity.getContent();

			Header contentEncoding = response.getFirstHeader("Content-Encoding");
			if (contentEncoding != null && contentEncoding.getValue().toLowerCase().contains("gzip")) {
				instream = new GZIPInputStream(instream);
			}
			//if (debug) {
			//	LogPadrao.d("entity.getContentLength():" + entity.getContentLength());
			//}

			GenericComunicacao.setConectado(true);

			try {
				// TODO ler ate chegas a lista de objetos
				return new JsonToObjectIterator<T>(
						new InputStreamReader(instream, encoding),
						prototype
				);
			} catch (UnsupportedEncodingException e) {
				LogPadrao.e (e);
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}

	protected String getLastSyncParameter() {
		return LAST_SYNC;
	}

	protected String getPasswordParameter() {
		return PASSWORD;
	}

	protected String getLoginParameter() {
		return LOGIN;
	}

	protected String getMacParameter() {
		return MAC;
	}

	protected String getImeiParameter() {
		return IMEI;
	}

	protected String getJsonObjectsParameter(){
		return JSON;
	}

	public void enviar(String imei, String mac, String email, String senha, T objeto) {
		ArrayList<T> objetos = new ArrayList<T>(1);
		objetos.add(objeto);
		enviarMultiplos(imei, mac, email, senha, objetos.iterator(),objeto);
	}

	public void enviarMultiplos(String imei, String mac, String email, String senha, Iterator<T> objetos) {
		enviarMultiplos(imei, mac, email, senha, objetos, null);
	}

	public Iterator<T> enviarMultiplos(String imei, String mac, String email, String senha, Iterator<T> objetos,T prototype) {
		HashMap<String, String> parametrosEnviar = new HashMap<String, String>();
		try {
			parametrosEnviar.put(getImeiParameter(), imei);
			parametrosEnviar.put(getMacParameter(), mac);
			if (email != null) {
				parametrosEnviar.put(getLoginParameter(), email);
			}
			if (senha != null) {
				parametrosEnviar.put(getPasswordParameter(), senha);
			}
			// TODO refazer iterando
			parametrosEnviar.put(getJsonObjectsParameter(), objetosToJson(objetos).toString());
			HttpResponse response = post(getUrl(), getMethodPost(), parametrosEnviar);
			if (response.getStatusLine().getStatusCode()!=200)
				//TODO
				return null;
			if (prototype!=null)
				return getObjectsFromResponse(prototype, response);
			return null;
		} catch (RuntimeException re) {
			LogPadrao.e (re);
			throw re;
		} catch (Throwable e) {
			LogPadrao.e (e);
			throw new RuntimeException(e);
		} finally {
			parametrosEnviar.remove(getJsonObjectsParameter());
			parametrosEnviar = null;
		}
	}

	public HttpResponse post(
			String url,  String metodo, 
			HashMap<String, String> parametros
	) throws IOException {
		HttpResponse response = null;

		try {
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParameters, SO_TIMEOUT);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpPost httpPost = new HttpPost(url + metodo);
			httpPost.setHeader("User-Agent", USER_AGENT);
			httpPost.setHeader("Accept", ACCEPT_HEADER);
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			//if (debug) {
			//	String urlDebug = url + metodo;
			//	String urlForm = "<form action='" + url + metodo + "' method='post'>";
			//	Set<Entry<String, String>> set = parametros.entrySet();
			//	Iterator<Entry<String, String>> i = set.iterator();
			//	Entry<String, String> entrada = null;
			//	while (i.hasNext()) {
			//		entrada = i.next();
			//		urlForm += "<br><input type='text' name='" + entrada.getKey() + "' value='" + entrada.getValue()
			//				+ "' />";
			//		urlDebug += "/" + entrada.getKey() + "/" + entrada.getValue();
			//	}
			//	urlForm += "<br><input type='submit' value='ok'/><br></form>";
			//	LogPadrao.d("Com:" + urlDebug);
			//	LogPadrao.d("Form:" + urlForm);
			//}
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			if (parametros != null) {
				for (
						Iterator<String> iterator = parametros.keySet().iterator();
						iterator.hasNext();
				) {
					String chave = iterator.next();
					String valor = parametros.get(chave);
					nameValuePairs.add(new BasicNameValuePair(chave, valor));
				}
			}
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			GenericComunicacao.setConectado(true);

			response = httpclient.execute(httpPost);
			GenericComunicacao.setConectado(true);
		} catch (IOException e) {
			GenericComunicacao.setConectado(false);
			throw e;
		}
		return response;
	}

	protected T jsonToObject(T prototype, JSONObject jsonObject) throws JSONException{
		return prototype.jsonToObjectWithPrototype(jsonObject);
	}

	protected JSONObject objetosToJson(Iterator<T> objetos) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		while( objetos.hasNext()) {
			jsonArray.put(objetoToJson(objetos.next()));
		}
		jsonObject.put("list", jsonArray);
		return jsonObject;
	}

	protected JSONObject objetoToJson(T objeto){
		return objeto.toJson();
	}

}
