package br.com.cds.mobile.framework.test.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import android.test.ActivityInstrumentationTestCase2;
import br.com.cds.mobile.framework.logging.DefaultLogEntryImpl;
import br.com.cds.mobile.framework.logging.LogPadrao;
import br.com.cds.mobile.framework.test.TestActivity;


public class LogTests extends 
		ActivityInstrumentationTestCase2<TestActivity> {

	public LogTests(){
		super("br.com.cds.mobile.framework.test", TestActivity.class);
	}

	public void testSimpleLogging(){
		LogPadrao.d("debug");
		LogPadrao.i("info");
		LogPadrao.e("error");
		LogPadrao.e(new RuntimeException());
	}

	public void testLogEntriesIO(){
		DefaultLogEntryImpl logEntry = new DefaultLogEntryImpl();
		LogPadrao.d("Log: "+logEntry.getLogPath()+'/'+logEntry.getDefaultLogFile());
		try {
			logEntry.setMessage("message");
			logEntry.save();
			logEntry.getLog().flush();
			JSONObject obj = DefaultLogEntryImpl.logEntriesIterator().next();
			LogPadrao.d("json:\n"+logEntry.toJson().toString()+'\n'+obj.toString());
			JSONArray arr = obj.names();
			JSONObject expected = logEntry.toJson();
			for(int i=0;i<arr.length();i++){
				String key = arr.get(i).toString();
				assertEquals(expected.get(key), obj.get(key));
			}
		} catch (Exception e) {
			fail();
		} finally{
			DefaultLogEntryImpl.clearAllLogs();
		}
	}

}
