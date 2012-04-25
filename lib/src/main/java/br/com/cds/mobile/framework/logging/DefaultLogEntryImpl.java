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
import br.com.cds.mobile.framework.BaseApplication;
import br.com.cds.mobile.framework.ErrorCode;
import br.com.cds.mobile.framework.FrameworkException;
import br.com.cds.mobile.framework.utils.DateUtil;
import br.com.cds.mobile.framework.utils.StringUtil;

public class DefaultLogEntryImpl implements LogEntry{

	// campos do json
	public static final String USER_NAME = "user_name";
	public static final String MESSAGE = "message";
	public static final String HASH_CODE = "message_hash";
	public static final String APP_VERSION = "app_version";
	public static final String DB_VERSION = "db_version";
	public static final String LEVEL = "level";
	public static final String TIMESTAMP = "timestamp";

	private final static String LOG_FOLDER_RELATIVE_PATH = "/logs/";
	private static final String LOG_FILE_FORMAT = "%s_log.txt";
	private static final String ENCODING = "UTF-8";

	private static SoftReference<PrintWriter> fileLog;

	private Date timestamp;
	private String level;
	private String message;
	private String appVersion;
	private String userName;
	private int dbVersion;

	public DefaultLogEntryImpl(){
		Context ctx = BaseApplication.getContext();
		try{
			appVersion = ctx.getPackageManager().getPackageInfo(
					ctx.getPackageName(), 0).versionName;
			userName = BaseApplication.getUserName();
			dbVersion = BaseApplication.getDBVersion();
		} catch (NameNotFoundException e) {
			throw new RuntimeException(); // Este erro nao deve ocorrer, senao
		}                                 // o aplicativo nao tem pacote! O_o
	}

	public static String getLogFolder() {
		return BaseApplication.getContext().getApplicationInfo().dataDir +
				LOG_FOLDER_RELATIVE_PATH;
	}

	public static String getLogFileName() {
		return String.format(LOG_FILE_FORMAT, DateUtil.dateToString(new Date()) );
	}

	public static synchronized PrintWriter getLog() throws FrameworkException{
		PrintWriter fileLogPw = (fileLog == null) ? null : fileLog.get();
		if (fileLogPw == null) {
			String logPath = getLogFolder();
			File pasta = new File(logPath);
			if (!pasta.exists()) {
				pasta.mkdirs();
			}
			fileLogPw = openLogFile(logPath+getLogFileName());
			fileLog = new SoftReference<PrintWriter>(fileLogPw);
		}
		return fileLogPw;
	}

	private static PrintWriter openLogFile(String path) throws FrameworkException {
		File logFile = new File(path);
		try {
			if( !logFile.exists())
				logFile.createNewFile();
		} catch (IOException e1) {
			throw new FrameworkException(ErrorCode.LOG_FILE_NOT_CREATED_IO_EXCEPTION);
		}
		PrintWriter pw = null;
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(logFile,true),
							ENCODING
					)
			);
			pw = new PrintWriter(bufferedWriter){
				@Override                                     // Nao estava escrevendo o log   
				protected void finalize() throws Throwable {  // ao finalizar.                 
					try{                                      // Sobreescrevi o metodo finalize
						this.flush();                         // para realizar o flush.
					} finally {
						super.finalize();
					}
				}
			};
		} catch (UnsupportedEncodingException e) {  // Este erro nao deve acontecer em hipotese alguma
			throw new RuntimeException(e);          // a menos que a plataforma nao suporte UTF-8!  O_o
		} catch (FileNotFoundException e) {  // Este nao deve ocorrer, o arquivo de log jah
			throw new RuntimeException(e);   // foi criado e, se nao, um erro jah foi lancado
		}                                    // reescreva isto se o metodo "openLogFile" for refatorado
		return pw;
	}

	public JSONObject toJson() throws JSONException{
		JSONObject json = new JSONObject();
		json.put(TIMESTAMP, DateUtil.dateToString(timestamp));
		json.put(USER_NAME, userName);
		json.put(APP_VERSION, appVersion);
		json.put(DB_VERSION, dbVersion);
		json.put(LEVEL, level);
		json.put(MESSAGE, message);
		json.put(HASH_CODE, StringUtil.SHA1(message));
		return json;
	}

	@Override
	public boolean save() {
		try{
			JSONObject json = toJson();
			LogPadrao.d(json.toString());
			PrintWriter log = getLog();
			log.println(json.toString());
			return true;
		} catch (Exception e) {
			// TODO 
			return false;
		}
	}

	@Override
	public void log(String level,String message) {
		this.timestamp = new Date();
		this.level = level;
		this.message = message;
	}

	@Override
	public DefaultLogEntryImpl clone() {
		try {
			return (DefaultLogEntryImpl)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public static void clearAllLogs(){
		File logs[] = getLogFiles();
		for(File log: logs)
			log.delete();
		fileLog = null;
	}

	public static File[] getLogFiles() {
		File logFolder = new File(getLogFolder());
		File logArray[];
		if(logFolder.exists())
			logArray = logFolder.listFiles();
		else
			logArray = new File[0];
		return logArray;
	}

	public static Iterator<JSONObject> logEntriesIterator(){
		try {
			getLog().flush();
		} catch (FrameworkException e) {
			// TODO conferir se nao ha logs mesmo
		}
		return new JsonLogIterator();
	}

	private static class JsonLogIterator implements Iterator<JSONObject>{

		Iterator<File> logs;
		FrameworkJSONTokener current;
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
				current = new FrameworkJSONTokener(openLogToRead(logs.next()));
			}
			try {
				return new JSONObject((JSONTokener)current);
			} catch (JSONException e) {
				// TODO conferir se o arquivo acabou
				if(logs.hasNext()){
					current = null;
					return getNextfromFiles();
				}
				return null;
			}
		}

		private Reader openLogToRead(File file){
			try{
				return new InputStreamReader(
						new FileInputStream(file),
						ENCODING
				);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);   // Estes erros nao devem ocorrer na implementacao atual
			} catch (FileNotFoundException e) {  // a menos que UTF-8 nao seja suportada  O_o
				throw new RuntimeException(e);   // ou File::listFiles retorna arquivos inexistentes !
			}
		}


	}

}
