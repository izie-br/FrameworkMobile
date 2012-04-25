package br.com.cds.mobile.framework.logging;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import br.com.cds.mobile.framework.BaseApplication;
import br.com.cds.mobile.framework.utils.DateUtil;
import br.com.cds.mobile.framework.utils.FileUtil;
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

	private static synchronized PrintWriter getLog() throws IOException{
		PrintWriter fileLogPw = (fileLog == null) ? null : fileLog.get();
		if (fileLogPw == null) {
			fileLogPw = FileUtil.openFileToAppend(getLogFolder()+getLogFileName());
			fileLog = new SoftReference<PrintWriter>(fileLogPw);
		}
		return fileLogPw;
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

	public static Iterator<JSONObject> logEntriesIterator(){
		try {
			getLog().flush();
		} catch (IOException e) {
			// TODO conferir se nao ha logs mesmo
		}
		return new JsonLogIterator(getLogFiles());
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

}
