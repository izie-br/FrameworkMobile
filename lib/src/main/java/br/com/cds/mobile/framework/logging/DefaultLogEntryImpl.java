package br.com.cds.mobile.framework.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import br.com.cds.mobile.framework.BaseApplication;
import br.com.cds.mobile.framework.ErrorCode;
import br.com.cds.mobile.framework.FrameworkException;
import br.com.cds.mobile.framework.utils.DateUtil;

public class DefaultLogEntryImpl implements LogEntry{

	private static String LOG_FILE_FORMAT = "%s_log.txt";
	/*
CREATE  TABLE tb_erro (
  _id integer primary key autoincrement,
  dt_erro text NULL,
  erro_sha1 TEXT NOT NULL,
  erro TEXT NOT NULL,
  log TEXT NOT NULL,
  versao_app VARCHAR(30) NULL,
  versao_db VARCHAR(30) NULL,
  ativo TINYINT NULL DEFAULT 1 
);

	 */

	private final static String LOG_FOLDER =  
			BaseApplication.getContext().getApplicationInfo().dataDir + "/logs";
	private static SoftReference<PrintWriter> fileLog;

	private Date timestamp;
	private String level;
	private String message;
	private String appVersion;

	public DefaultLogEntryImpl(){
		Context ctx = BaseApplication.getContext();
		try{
			appVersion = ctx.getPackageManager().getPackageInfo(
					ctx.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			throw new RuntimeException(); // Este erro nao deve ocorrer, senao
		}                                 // o aplicativo nao tem pacote! O_o
	}

	public String getLogPath() {
		return String.format(
				LOG_FOLDER,
				Environment.getDataDirectory(),
				BaseApplication.getContext().getPackageName()
		);
	}

	public String getDefaultLogFile() {
		return String.format(LOG_FILE_FORMAT, DateUtil.dateToString(new Date()) );
	}

	public synchronized PrintWriter getLog() throws FrameworkException{
		PrintWriter fileLogPw = (fileLog == null) ? null : fileLog.get();
		if (fileLogPw == null) {
			String logPath = getLogPath();
			File pasta = new File(logPath);
			if (!pasta.exists()) {
				pasta.mkdirs();
			}
			fileLogPw = openLogFile(logPath+'/'+getDefaultLogFile());
			fileLog = new SoftReference<PrintWriter>(fileLogPw);
		}
		return fileLogPw;
	}

	private PrintWriter openLogFile(String path) throws FrameworkException {
		File logFile = new File(path);
		try {
			if( !logFile.exists())
				logFile.createNewFile();
		} catch (IOException e1) {
			throw new FrameworkException(ErrorCode.LOG_FILE_NOT_CREATED_IO_EXCEPTION);
		}
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(
					new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(logFile,true),
									"UTF-8"
							)
					)
			){
				@Override
				protected void finalize() throws Throwable {
					this.flush();
					super.finalize();
				}
			};
		} catch (UnsupportedEncodingException e) {  // Este erro nao deve acontecer em hipotese alguma
			throw new RuntimeException(e);          // a menos que a plataforma nao suporte UTF-8!  O_o
		} catch (FileNotFoundException e) {  // Este nao deve ocorrer, o arquivo de log jah
			throw new RuntimeException(e);   // foi criado e se nao funcionou, um erro jah foi lancado
		}                                    // reescreva se o metodo "openLogFile" foi refatorado
		return pw;
	}

	public JSONObject toJson() throws JSONException{
		JSONObject json = new JSONObject();
		json.put("timestamp", DateUtil.dateToString(timestamp));
		json.put("level", level);
		json.put("application_version", appVersion);
		json.put("message", message);
		return json;
	}

	@Override
	public boolean save() {
		try{
			JSONObject json = toJson();
			LogPadrao.d(json.toString());
			PrintWriter log = getLog();
			log.println(json.toString());
			//log.flush();
			return true;
		} catch (Exception e) {
			// TODO 
			return false;
		}
	}

	@Override
	public void setMessage(String message) {
		this.message = message;
		this.timestamp = new Date();
	}

	@Override
	public DefaultLogEntryImpl clone() {
		try {
			return (DefaultLogEntryImpl)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public void setLevel(String level) {
		this.level = level;
	}

	public static void clearAllLogs(){
		File logs[] = getLogFiles();
		for(File log: logs)
			log.delete();
	}

	public static File[] getLogFiles() {
		File logFolder = new File(LOG_FOLDER);
		File logArray[];
		if(logFolder.exists())
			logArray = logFolder.listFiles();
		else
			logArray = new File[0];
		return logArray;
	}

	public static Iterator<JSONObject> logEntriesIterator(){
		return new JsonLogIterator();
	}

	private static class JsonLogIterator implements Iterator<JSONObject>{

		Iterator<File> logs;
		Reader current;
		private JSONObject nextObj;

		private JsonLogIterator(){
			File[] logArray = getLogFiles();
			logs = Arrays.asList(logArray).iterator();
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
				current = openLogToRead(logs.next());
			}
			FrameworkJSONTokener tokener = new FrameworkJSONTokener(current);
			try {
				return new JSONObject((JSONTokener)tokener);
			} catch (JSONException e) {
				// TODO conferir se o arquivo acabou
				return null;
			}
		}

		private Reader openLogToRead(File file){
			try{
				return new InputStreamReader(
						new FileInputStream(file),
						"UTF-8"
				);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);   // Estes erros nao devem ocorrer na implementacao atual
			} catch (FileNotFoundException e) {  // a menos que UTF-8 nao seja suportada  O_o
				throw new RuntimeException(e);   // ou File::listFiles retorna arquivos inexistentes !
			}
		}


	}

}
