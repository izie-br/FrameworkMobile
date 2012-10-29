package com.quantium.mobile.framework.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Iterator;

import org.json.JSONObject;

import com.quantium.mobile.framework.StreamJsonIterator;
import com.quantium.mobile.framework.utils.FileUtil;


public class JsonLogIterator implements Iterator<JSONObject>{

	private static final String LOG_FILE_REMOVED_BEFORE_COMPLETE_READ =
			"LOG_FILE_REMOVED_BEFORE_COMPLETE_READ";

	Iterator<File> logs;
	StreamJsonIterator current;
	private JSONObject nextObj;

	public JsonLogIterator(File[] files){ 
		this.logs = Arrays.asList(files).iterator();
		
	}

	@Override
	public boolean hasNext() {
		if(nextObj==null)
			nextObj = getNextfromFiles();
		return (nextObj!=null);
	}

	@Override
	public JSONObject next() {
		if(nextObj==null)
			return getNextfromFiles();
		JSONObject obj = nextObj;
		nextObj = null;
		return obj;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	private JSONObject getNextfromFiles(){
		if(current==null){
			if(!logs.hasNext())
				return null;
			try {
				current = new StreamJsonIterator(
						FileUtil.openFileToRead(logs.next().getPath())
				);
			} catch (FileNotFoundException e) {
				// Ocorre se o arquivo de log foi removido antes de ser lido
				// conferir se o metodo clearAllLogs foi chamado incorretamente
				throw new RuntimeException(
							LOG_FILE_REMOVED_BEFORE_COMPLETE_READ
						);
			}
		}
		try {
			return current.next();
		} catch (RuntimeException e) {
			// TODO conferir se o arquivo acabou
			if(logs.hasNext()){
				current = null;
				return getNextfromFiles();
			}
			return null;
		}
	}


}