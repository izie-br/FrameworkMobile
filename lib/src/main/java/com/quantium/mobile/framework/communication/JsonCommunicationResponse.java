package com.quantium.mobile.framework.communication;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;

import com.quantium.mobile.framework.FrameworkJSONTokener;
import com.quantium.mobile.framework.JsonSerializable;
import com.quantium.mobile.framework.JsonToObjectIterator;
import com.quantium.mobile.framework.logging.LogPadrao;

public class JsonCommunicationResponse implements SerializedCommunicationResponse{

	private static final String JSON_TOKEN_EXCEPTION_FMT =
			"caractere(s) '%s' esperado, '%s' encontrado."+
			" Continuacao da resposta:\n%s";

	public JsonCommunicationResponse(Reader reader, String...keys){
		this.reader = reader;
		this.keysToObjectList = keys;
	}

	private String keysToObjectList[];
	private Reader reader;
	private JsonSerializable<?> prototype;

	private Map<String,Object> map;
	private Iterator<?> iterator;

	public void setPrototype(JsonSerializable<?> prototype) {
		this.prototype = prototype;
	}

	public void setIterator(Iterator<?> iterator) {
		this.iterator = iterator;
	}

	@Override
	public Reader getReader() {
		return reader;
	}

	private void checkOutput() {
		if (map == null && iterator == null) {
			map = new HashMap<String, Object>();
			iterator = parseResponse(map);
		}
	}

	@Override
	public Map<String, Object> getResponseMap() {
		checkOutput();
		return map;
	}

	protected void setKeysToObjectList(String... keysToObject) {
		this.keysToObjectList = keysToObject;
	}

	protected Iterator<?> getIterator() {
		checkOutput();
		return iterator;
	}

	private Iterator<?> parseResponse(
			Map<String, Object> responseOutput
	){
		try {
			fillResponseOutput(keysToObjectList, responseOutput, reader);
			if (prototype != null) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				JsonToObjectIterator iter = new JsonToObjectIterator(
						reader,
						prototype
				);
				return iter;
			}
			return null;
		} catch (JSONException e) {
			LogPadrao.e(e);
			throw new RuntimeException(e);
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
			Reader reader
	) throws JSONException
	{
		FrameworkJSONTokener tokener = new FrameworkJSONTokener(reader);
		int keysToArrayIndex = 0;
		char c= tokener.nextClean();
		String key;
		if (
				prototype != null &&
				(keysToArray == null || keysToArray.length == 0)
		) {
			return (c=='[');
		}

		// chave de abertura
		if (c != '{') {
			throw new JSONException(String.format(
				JSON_TOKEN_EXCEPTION_FMT,
				"{",
				""+c,
				nextChars(512, reader)
			));
		}

		for(;;) {
			// key
			c = tokener.nextClean();
			switch (c) {
			case 0:
				throw new JSONException(String.format(
					JSON_TOKEN_EXCEPTION_FMT,
					"<jsonkey>",
					"<0>",
					nextChars(512, reader)
				));
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
				throw new JSONException(String.format(
					JSON_TOKEN_EXCEPTION_FMT,
					"=, => ou : ",
					""+c,
					nextChars(512, reader)
				));
			}

			// conferir se eh uma das chaves que levam aos objetos
			if ( keysToArray != null && key.equals(keysToArray[keysToArrayIndex]) ) {
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
					throw new JSONException(String.format(
						JSON_TOKEN_EXCEPTION_FMT,
						"{",
						""+c,
						nextChars(512, reader)
					));
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

	private String nextChars(int quantity, Reader reader) {
		char buffer[] = new char[quantity];
		int readCount = 0;
		int lastCount = 0;
		while (lastCount >= 0) {
			try {
				lastCount = reader.read(
					buffer,
					readCount,
					buffer.length - readCount
				);
			} catch (IOException e) {
				return new String(buffer, 0, readCount) +
					"<IOException>";
			}
			if (lastCount < 0 || readCount >= quantity)
				break;
			readCount += lastCount;
		}
		return new String(buffer, 0, readCount);
	}

}
