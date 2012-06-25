package br.com.cds.mobile.framework.comunication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import br.com.cds.mobile.framework.FrameworkJSONTokener;
import br.com.cds.mobile.framework.logging.LogPadrao;
import br.com.cds.mobile.framework.utils.StringUtil;

public class HttpJsonDao<T extends JsonSerializable<T>> extends GenericComunicacao {

	private static final String HTTP_REQUEST_ENCODING =
			"application/x-www-form-urlencoded";
	private static final int SO_TIMEOUT = 90000;
	private static final int CONNECTION_TIMEOUT = 15000;
	// TODO firefox - ubuntu - i686!  ???? o_O
	private static final String USER_AGENT =
			"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6)" +
			" Gecko/20061201 Firefox/2.0.0.6 (Ubuntu-feisty)";
	// TODO deve ser "application/json"
	private static final String ACCEPT_HEADER =
			"text/html,application/xml,application/xhtml+xml," +
			"text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";

	private String url;
	private T prototype;
	private Iterator<T> iterator;
	private Map<String,String> parameters;
	private Map<String,Object> bodyParameters;
	private String body;
	private String keysToObjectArray [];
	private String charset = StringUtil.DEFAULT_ENCODING;

	private Map<String,Object> responseOutput;

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


	/**
	 * Altera o encoding da comunicação
	 *
	 * @param charset charset (default "UTF-8")
	 */
	public HttpJsonDao<T> setCharset (String charset) {
		this.charset = charset;
		return this;
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
	public HttpJsonDao<T> setPrototype (T prototype) {
		this.prototype = prototype;
		return this;
	}

	/**
	 *p Armazena um iterador de objetos para enviar
	 *
	 * @param iterator iterador de colecao de objetos pare enviar
	 */
	public HttpJsonDao<T> setIterator (Iterator<T> iterator) {
		this.iterator = iterator;
		return this;
	}

	/**
	 * Remove o mapa de parametros anterior e troca pelo mapa
	 * de parametros especificado.
	 *
	 * @param parameters novo mapa de parametros
	 */
	public HttpJsonDao<T> setParameters (Map<String,String> parameters) {
		this.parameters = parameters;
		return this;
	}

	/**
	 * Adiciona ou altera o parametro de especificado.
	 *
	 * @param key chave (nome do parametro)
	 * @param value valor do parametro
	 */
	public HttpJsonDao<T> setParameter (String key, String value) {
		this.getParameters().put(key, value);
		return this;
	}

	/**
	 * Remove o mapa de parametros do corpo anterior e troca pelo mapa
	 * de parametros especificado.
	 *
	 * @param parameters novo mapa de parametros
	 */
	public HttpJsonDao<T> setBodyParameters (Map<String,Object> bodyParameters) {
		this.bodyParameters = bodyParameters;
		return this;
	}

	/**
	 * Adiciona ou altera o parametro de corpo especificado.
	 *
	 * @param key chave (nome do parametro)
	 * @param value valor do parametro
	 */
	public HttpJsonDao<T> setBodyParameter (String key, String value) {
		this.getBodyParameters().put(key, value);
		return this;
	}

	/**
	 * Altera as chaves para o array de objetos no corpo
	 * da resposta.
	 *
	 * @param keys
	 */
	public HttpJsonDao<T> setKeysToObjectArray(String...keys) {
		this.keysToObjectArray = keys;
		return this;
	}

	/**
	 * Altera a chaves para onde vai o corpo
	 *
	 * @param body chave onde vai o corpo
	 */
	public HttpJsonDao<T> setBodyKey(String body) {
		this.body = body;
		return this;
	}


	/**
	 * Altera a url completa da requisicao
	 *
	 * @param url URL
	 */
	public HttpJsonDao<T> setURL (String url) {
		this.url = url;
		return this;
	}

	/**
	 * <p>
	 *   Insere um Map responseOutput com os dados do JSON anteriores ao 
	 *   array de objetos lido pelo iterador.
	 * </p>
	 * @param map
	 * @return
	 */
	public HttpJsonDao<T> setResponseOutputMap(Map<String, Object> map) {
		this.responseOutput = map;
		return this;
	}

	/**
	 * Gets the bodyParameters for this instance.
	 *
	 * @return The bodyParameters.
	 */
	public Map<String,Object> getBodyParameters(){
		if (this.bodyParameters == null)
			this.bodyParameters = new HashMap<String,Object>(2);
		return this.bodyParameters;
	}

	/**
	 * Parametros
	 *
	 * @return The parameters.
	 */
	public Map<String,String> getParameters(){
		if (this.parameters == null)
			this.parameters = new HashMap<String,String>(2);
		return this.parameters;
	}

	/**
	 * <p>Busca no servidor por JSON com array de objetos.</p>
	 * <p>
	 *   Opcionalmente, outros parametros podem ser
	 *   recebidos.
	 * </p>
	 * <p>Warning: Todo conteudo apos o array de objetos sera ignorado.</p>
	 * 
	 * @param parametros parametros HTTP para a busca
	 * @param prototype prototipo usado para deserializacao
	 * @param responseOutput saida da copia da resposta sem array de objetos
	 * @param keysToObjectArray "XPATH" para chegar ao array de objetos
	 * 
	 * @return iterador do array de objetos, retirado do stream recebido do servidor
	 * @throws FrameworkException
	 */
	public Iterator<T> send () throws FrameworkException{
		try{
			HttpResponse response = null;
			int connectionTries = 1;
			for(;;){
				Exception exceptions [] = new Exception[TENTATIVAS_DE_CONEXAO+1];
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
							JSONObject current = obj;
							for (int i = 0; ; i++) {
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
					exceptions[connectionTries] = e;
				}
				connectionTries++;
				if(response!=null)
					break;
				if(connectionTries>TENTATIVAS_DE_CONEXAO){
					for (int j = 0; j < connectionTries; j++)
						LogPadrao.e(exceptions[j]);
					throw new FrameworkException(ErrorCode.NETWORK_COMMUNICATION_ERROR);
				}
			}
			return parseResponse(
					response, prototype,
					keysToObjectArray, responseOutput
			);
		} catch (JSONException e) {
			LogPadrao.e(e);
			throw new FrameworkException(ErrorCode.UNKNOWN_EXCEPTION);
		} catch (IOException e) {
			LogPadrao.e(e);
			throw new FrameworkException(ErrorCode.UNKNOWN_EXCEPTION);
		}
	}

	protected Iterator<T> parseResponse (
		HttpResponse response,
		T prototype,
		String keysToArray[],
		Map<String, Object> responseOutput
	) throws IOException
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
				fillResponseOutput(keysToArray, responseOutput, reader);
				return new JsonToObjectIterator<T>(
						reader,
						prototype
				);
			} catch (JSONException e) {
				LogPadrao.e(e);
				throw new RuntimeException(e);
			} catch (UnsupportedEncodingException e) {
				LogPadrao.e (e);
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}

	/**
	 * <p>
	 *   Preenche o Map responseOutput com os dados do JSON anteriores ao 
	 *   array de objetos lido pelo iterador.
	 * </p>
	 * <p>
	 *   Alem disso, este metodo move a posicao de leitura do stream para
	 *   o array de objetos da request, indicadao pelas keysToArray.
	 * </p>
	 *
	 * @param keysToArray  chaves do json que levam ate o array de objetos
	 * @param responseOutput  map para excrever o conteudo da resposta
	 * @param reader  reader do stream recebido do servidor
	 */
	private boolean fillResponseOutput (
			String[] keysToArray,
			Map<String,Object> responseOutput,
			InputStreamReader reader
	) throws JSONException
	{
		FrameworkJSONTokener tokener = new FrameworkJSONTokener(reader);
		int keysToArrayIndex = 0;
		char c;
		String key;
		c = tokener.nextClean();
		if (keysToArray == null || keysToArray.length == 0)
			return (c=='[');
		for(;;) {
			// chave de abertura
			if (c != '{')
				throw new JSONException("json incompleto");

			// key
			c = tokener.nextClean();
			switch (c) {
			case 0:
				throw new JSONException("json incompleto");
			case '}':
				return false;
			default:
				tokener.back();
				key = tokener.nextValue().toString();
			}

			// espacador ":", "=" ou "=>"
			c = tokener.nextClean();
			switch (c) {
			case '=':
				if (tokener.next() != '>') {
					tokener.back();
				}
				/* fall through */
			case ':':
				break;
			default:
				throw new JSONException("json incompleto");
			}

			// conferir se eh uma das chaves que levam aos objetos
			if ( key.equals(keysToArray[keysToArrayIndex]) ) {
				if (responseOutput != null) {
					HashMap<String, Object> newObject =
						new HashMap<String, Object>(2);
					responseOutput.put(key, newObject );
					responseOutput = newObject;
				}
				c = tokener.nextClean();
				if (
					keysToArrayIndex == (keysToArray.length -1) &&
					c == '['
				){
					return true;
				}
				if (c != '{')
					throw new JSONException("json incompleto");
				keysToArrayIndex++;
				continue;
			}
			responseOutput.put(key, tokener.nextValue());
			switch (tokener.nextClean()) {
			case ';':
			case ',':
				if (tokener.nextClean() == '}') {
					return false;
				}
				tokener.back();
				break;
			case '}':
				return false;
			default:
				throw new JSONException("json incompleto");
			}
		} // for
	}

	protected String getUserAgent() {
		return USER_AGENT;
	}

	protected String getAcceptHeader() {
		return ACCEPT_HEADER;
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
			httpPost.setHeader("Content-Type", HTTP_REQUEST_ENCODING);
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
			GenericComunicacao.setConectado(false);
			throw e;
		}
		GenericComunicacao.setConectado(true);
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

	private static int getConnectionTimeout() {
		return CONNECTION_TIMEOUT;
	}

	private static int getSoTimeout() {
		return SO_TIMEOUT;
	}
}
