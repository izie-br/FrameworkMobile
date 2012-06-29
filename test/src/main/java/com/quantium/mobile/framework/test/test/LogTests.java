package com.quantium.mobile.framework.test.test;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.test.ActivityInstrumentationTestCase2;
import com.quantium.mobile.framework.logging.DefaultLogEntryImpl;
import com.quantium.mobile.framework.logging.LogEntry;
import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.test.TestActivity;


public class LogTests extends 
		ActivityInstrumentationTestCase2<TestActivity> {

	public LogTests(){
		super("com.quantium.mobile.framework.test", TestActivity.class);
	}

	/**
	 * testes com log sem IO 
	 */
	public void testSimpleLogging(){
		LogPadrao.setPrototype(null);
		LogPadrao.d("debug");
		LogPadrao.i("info");
		LogPadrao.e("Este eh um log erro explicitamente criado para testes");
		LogPadrao.e(new RuntimeException("Este eh um erro explicitamente criado para testes"));
	}

	/**
	 * teste para inserir uma quantidade de Jsons de log, e em seguida
	 * conferir se foram corretamente inseidos
	 */
	public void testLogEntriesIO(){
		DefaultLogEntryImpl logEntry = new DefaultLogEntryImpl();
		try {
			// quantidade de entradas de log para o teste
			int count = 12;
			// lista para armazenar a lista de entries de Log
			ArrayList<DefaultLogEntryImpl> list =
					new ArrayList<DefaultLogEntryImpl>();
			// criando  salvando as entradas de log
			for(int i =0; i<count;i++){
				logEntry = logEntry.clone();
				logEntry.log(LogEntry.LEVEL_INFO,getRandomString(40));
				logEntry.save();
				list.add(logEntry);
			}
			// Iterador para buscar todas as entradas de log no(s) arquivo(s)
			Iterator<JSONObject> logEntriesIterator = DefaultLogEntryImpl.logEntriesIterator();
			for(int i =0; i<count;i++){
				// se nao ha a mesma quantidade de logs, marcar erro
				if(!logEntriesIterator.hasNext())
					fail();
				// jsonobject da lista como valor esperado
				JSONObject expected = list.get(i).toJson();
				// jsonObject lido do arquivo
				JSONObject obj = logEntriesIterator.next();
				jsonAssertEquals(expected, obj);
			}
		} catch (Exception e) {
			fail(e.getMessage());
		} finally{
			DefaultLogEntryImpl.clearAllLogs();
		}
	}

	public void testLoggingWithIO(){
		try {
			LogPadrao.setPrototype(new DefaultLogEntryImpl());
			// este NAO deve ser inserido
			LogPadrao.d("debug");

			// as tres entradas abaixo devem ser inseridas
			// serao escritos em lista para conferir

			// logando uma mensagem (info)
			JSONObject jsonInfo = new JSONObject();
			jsonInfo.put(DefaultLogEntryImpl.LEVEL, LogEntry.LEVEL_INFO);
			jsonInfo.put(DefaultLogEntryImpl.MESSAGE, LogEntry.LEVEL_INFO);
			LogPadrao.i(jsonInfo.getString(DefaultLogEntryImpl.MESSAGE));

			// logando uma mensagem de erro
			JSONObject jsonMessError = new JSONObject();
			jsonMessError.put(DefaultLogEntryImpl.LEVEL, LogEntry.LEVEL_ERROR);
			jsonMessError.put(DefaultLogEntryImpl.MESSAGE,
					"Este eh um log erro explicitamente criado para testes");
			LogPadrao.e(jsonMessError.getString(DefaultLogEntryImpl.MESSAGE));

			// logando um stack trace de erro
			String stackTraceMessage = "Este eh um log erro explicitamente criado para testes";
			LogPadrao.e(new RuntimeException(stackTraceMessage));

			// conferirndo se tudo esta escrito no arquivo
			Iterator<JSONObject> logIterator = DefaultLogEntryImpl.logEntriesIterator();

			//conferindo o INFO
			if(!logIterator.hasNext())
				fail();
			JSONObject jsonobj = logIterator.next();
			assertEquals(jsonInfo.get(
					DefaultLogEntryImpl.LEVEL),
					jsonobj.get(DefaultLogEntryImpl.LEVEL)
			);
			assertEquals(jsonInfo.get(
					DefaultLogEntryImpl.MESSAGE),
					jsonobj.get(DefaultLogEntryImpl.MESSAGE)
			);

			//conferindo a mensagem de erro
			if(!logIterator.hasNext())
				fail();
			jsonobj = logIterator.next();
			assertEquals(jsonMessError.get(
					DefaultLogEntryImpl.LEVEL),
					jsonobj.get(DefaultLogEntryImpl.LEVEL)
			);
			assertEquals(jsonMessError.get(
					DefaultLogEntryImpl.MESSAGE),
					jsonobj.get(DefaultLogEntryImpl.MESSAGE)
			);

			//conferindo o log de stacktrace
			if(!logIterator.hasNext())
				fail();
			jsonobj = logIterator.next();
			assertTrue(
				jsonobj.optString(DefaultLogEntryImpl.MESSAGE, "")
				.contains(stackTraceMessage)
			);
			assertEquals(jsonMessError.get(
					DefaultLogEntryImpl.LEVEL),
					jsonobj.get(DefaultLogEntryImpl.LEVEL)
			);

		} catch (JSONException e) {
			fail(e.getMessage());
		}
		finally {
			DefaultLogEntryImpl.clearAllLogs();
		}
	}

	private void jsonAssertEquals(JSONObject expected, JSONObject obj)
			throws JSONException {
		// keyset dos campos do json
		JSONArray arr = obj.names();
		for(int j=0;j<arr.length();j++){
			String key = arr.optString(j,null);
			// se a key nao for uma string
			if(key==null)
				fail();
			assertEquals(expected.get(key), obj.get(key));
		}
	}

	private String getRandomString(int count){
		return org.apache.commons.lang.RandomStringUtils.random(count);
	}

}
