package br.com.cds.mobile.framework.comunication;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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
import org.json.JSONTokener;

import br.com.cds.mobile.framework.logging.FrameworkJSONTokener;
import br.com.cds.mobile.framework.logging.LogPadrao;
import br.com.cds.mobile.framework.utils.DateUtil;

public abstract class GenericComunicacaoJSON<T> extends GenericComunicacao {

	private String url;
	public float conteudo;
	public long timestampFim;
	public long timestampInicio;

	

	public GenericComunicacaoJSON(String url) {
		super();
		this.url = url;
	}

	public T buscar(String imei, String mac, String email, String senha, HashMap<String, String> filtros) {
		ArrayList<T> lista = listar(imei, mac, email, senha, filtros);
		if (lista == null || lista.size() == 0) {
			return null;
		}
		return lista.get(0);
	}

	private float calculaTaxaTransferencia() {
		return (conteudo * 1024) / ((timestampFim - timestampInicio) * 1000);
	}

	protected JSONArray conecta(String metodo, HashMap<String, String> parametros) throws JSONException,
			ClientProtocolException, IOException {
		int i = 1;
		//debug = debugPadrao;
		while (true) {
			try {
				timestampInicio = System.currentTimeMillis();
				JSONArray retorno = conecta(getUrl(), metodo, parametros);
				//debug = debugPadrao;
				timestampFim = System.currentTimeMillis();
				GenericComunicacao.setConectado(true);
				return retorno;
			} catch (ComunicacaoException com) {
				LogPadrao.d("Erro de comunicacao: %s",com.getMessage());
				// com.printStackTrace();
				if (i == GenericComunicacao.TENTATIVAS_DE_CONEXAO) {
					//debug = debugPadrao;
					GenericComunicacao.setConectado(false);
					throw com;
				}
				//if (debug) {
				//	com.printStackTrace();
				//}
				LogPadrao.d("Erro de Comunicacao. Tentando novamente:" + i);
				//debug = true;
				i++;
				continue;
			}
		}
	}

	protected JSONArray conecta(String url, String metodo, HashMap<String, String> parametros) throws JSONException {
		try {
			//if (debug) {
			//	if (parametros != null) {
			//		LogPadrao.d("ContentLength - Envio:" + parametros.toString().length());
			//	}
			//}
			HttpResponse response = getResponse(url, metodo, parametros);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();

				Header contentEncoding = response.getFirstHeader("Content-Encoding");
				if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
					instream = new GZIPInputStream(instream);
				}
				//if (debug) {
				//	LogPadrao.d("entity.getContentLength():" + entity.getContentLength());
				//}

				GenericComunicacao.setConectado(true);
				return streamToJson(instream, (int) entity.getContentLength());
			} else {
				throw new RuntimeException("Entidade vazia");
			}
		} catch (IOException e) {
			GenericComunicacao.setConectado(false);
			throw new ComunicacaoException(e);
		}

	}

	public ArrayList<T> verificarPendencias(String imei, String mac, String email, String senha, String modulo,
			Date dtUltimaSincronia) {
		try {
			HashMap<String, String> parametros = new HashMap<String, String>();
			parametros.put("imei", imei);
			parametros.put("mac", mac);
			if (email != null) {
				parametros.put("email", email);
			}
			if (senha != null) {
				parametros.put("senha", senha);
			}
			parametros.put("dtUltimaAtualizacao", DateUtil.dateToString(dtUltimaSincronia));
			ArrayList<T> objetos = processa(getMetodoVerificarPendencias(modulo), parametros);
			return objetos;
		} catch (RuntimeException re) {
			throw re;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void enviar(String imei, String mac, String email, String senha, T objeto) {
		ArrayList<T> objetos = new ArrayList<T>();
		objetos.add(objeto);
		enviarMultiplos(imei, mac, email, senha, objetos);
	}

	public void enviarMultiplos(String imei, String mac, String email, String senha, ArrayList<T> objetos) {
		HashMap<String, String> parametrosEnviar = new HashMap<String, String>();
		try {
			parametrosEnviar.put("imei", imei);
			parametrosEnviar.put("mac", mac);
			if (email != null) {
				parametrosEnviar.put("email", email);
			}
			if (senha != null) {
				parametrosEnviar.put("senha", senha);
			}
			parametrosEnviar.put("json", objetosToJson(objetos).toString());
			enviarMultiplos(parametrosEnviar);
		} catch (RuntimeException re) {
			throw re;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			parametrosEnviar.remove("json");
			parametrosEnviar = null;
		}
	}

	public void enviarMultiplos(HashMap<String, String> parametros) {
		try {
			processa(getMetodoEnviarMultiplos(), parametros);
		} catch (RuntimeException re) {
			throw re;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	protected abstract String getMetodoEnviarMultiplos();

	protected abstract String getMetodoListar();

	protected abstract String getMetodoVerificarPendencias(String modulo);

	public HttpResponse getResponse(String url, String metodo, HashMap<String, String> parametros) {
		HttpResponse response = null;

		try {
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 15000);
			HttpConnectionParams.setSoTimeout(httpParameters, 90000);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpPost httpPost = new HttpPost(url + metodo);
			// TODO firefox - ubuntu - i686!  ???? o_O
			httpPost.setHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux "
					+ "i686; en-US; rv:1.8.1.6) Gecko/20061201 Firefox/2.0.0.6 (Ubuntu-feisty)");
			httpPost.setHeader("Accept", "text/html,application/xml,"
					+ "application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
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
				for (Iterator<String> iterator = parametros.keySet().iterator(); iterator.hasNext();) {
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
			throw new ComunicacaoException(e);
		} catch (Throwable t) {
			LogPadrao.e(t);
		}
		return response;
	}

	protected String getUrl() {
		return url;
	}

	protected abstract T jsonToObject(JSONObject jsonObject) throws Throwable;

	public ArrayList<T> listar(String imei, String mac, String email, String senha, HashMap<String, String> filtros) {
		try {
			HashMap<String, String> parametros = new HashMap<String, String>();
			parametros.put("imei", imei);
			parametros.put("mac", mac);
			parametros.put("email", email);
			parametros.put("senha", senha);
			if (filtros != null) {
				parametros.putAll(filtros);
			}
			ArrayList<T> objetos = processa(getMetodoListar(), parametros);
			return objetos;
		} catch (RuntimeException re) {
			throw re;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	protected JSONObject objetosToJson(ArrayList<T> objetos) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for (T objeto : objetos) {
			jsonArray.put(objetoToJson(objeto));
		}
		jsonObject.put("list", jsonArray);
		return jsonObject;
	}

	protected abstract JSONObject objetoToJson(T objeto);

	protected ArrayList<T> processa(String metodo, HashMap<String, String> parametros) throws Throwable {
		JSONArray retorno = conecta(metodo, parametros);
		return trataRetorno(retorno);
	}

	protected JSONArray streamToJson(InputStream instream, int tamanho) throws JSONException, ClientProtocolException,
			IOException {
//		String resultString = null;
//		if (tamanho > 0) {
//			resultString = StringUtil.convertStreamToString(instream, tamanho);
//		} else {
//			resultString = StringUtil.convertStreamToString(instream);
//		}
//		if (debug) {
//			LogPadrao.d("Retorno:" + resultString);
//		}
//		if (resultString == null) {
//			return null;
//		}
//		conteudo = resultString.length();
//		if (debug) {
//			LogPadrao.d("ContentLength - Retorno:" + resultString.length());
//		}
		JSONTokener tokener = new FrameworkJSONTokener(instream);
		JSONObject obj = null;
		try {
			obj = new JSONObject(tokener);
		} catch (JSONException je) {
			LogPadrao.e(je);
//			RuntimeException re = new RuntimeException(resultString, je);
//			throw new LeituraRespostaException(
//					"Leitura da resposta do servidor temporariamente indisponível tente novamente.", re);
		}

		String tipoRetorno = (String) obj.get(TIPO_RETORNO);
		if (RETORNO_SUCESSO.equals(tipoRetorno)) {
			Iterator<?> it = obj.keys();
			while (it.hasNext()) {
				String chave = (String) it.next();
				if (chave.equals(MENSAGEM_RETORNO)) {
					return null;
				} else if (!chave.equals(TIPO_RETORNO)) {
					try {
						return obj.getJSONArray(chave);
					} catch (JSONException je) {
						if (je.getMessage().contains("of type org.json.JSONObject cannot be converted to JSONArray")
								|| je.getMessage().contains("is not a JSONArray")) {
							JSONObject j = obj.getJSONObject(chave);
							Collection<JSONObject> col = new ArrayList<JSONObject>();
							col.add(j);
							JSONArray array = new JSONArray(col);
							return array;
						}
						if (je.getMessage().contains("Value null at")) {
							return null;
						}
						if (je.getMessage().contains("of type java.lang.String cannot be converted to JSONArray")) {
							JSONObject jo = new JSONObject();
							jo.put("retorno", obj.getString(chave));
							Collection<JSONObject> col = new ArrayList<JSONObject>();
							col.add(jo);
							JSONArray array = new JSONArray(col);
							return array;
						}
						throw je;
					}
				}
			}
			return null;

		} else {
			String mensagemErro = (String) obj.get(MENSAGEM_RETORNO);
			if(RETORNO_AVISO.equals(tipoRetorno))
				mensagemErro = obj.get(MENSAGEM_RETORNO).toString();
			if (mensagemErro == null) {
				// TODO
				mensagemErro = "Erro desconhecido.";
			}
			// TODO
			if (mensagemErro.equals("Nenhum valor encontrado.")) {
				return null;
			}
			throw new ComunicacaoException(mensagemErro);
		}
	}

	protected ArrayList<T> trataRetorno(JSONArray entries) throws Throwable {
		if (entries == null) {
			return new ArrayList<T>();
		}
		ArrayList<T> retorno = new ArrayList<T>();
		for (int i = 0; i < entries.length(); i++) {
			JSONObject jsonObject = entries.getJSONObject(i);
			retorno.add(jsonToObject(jsonObject));
		}
		return retorno;
	}

	public int getConteudo() {
		return (int) conteudo;
	}

	public float getTaxaTransferencia() {
		return calculaTaxaTransferencia();
	}

}
