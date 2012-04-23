package br.com.cds.mobile.framework.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import br.com.cds.mobile.framework.BaseApplication;
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

	private final String LOG_FOLDER;
	private static SoftReference<PrintWriter> fileLog;

	private Date timestamp;
	private String level;
	private String message;
	private String appVersion;

	public DefaultLogEntryImpl(){
		Context ctx = BaseApplication.getContext();
		LOG_FOLDER = ctx.getApplicationInfo().dataDir + "/logs";
		try{
			appVersion = ctx.getPackageManager().getPackageInfo(
					ctx.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			appVersion = null;
		}
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

	public PrintWriter getLog() throws FileNotFoundException{
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

	private PrintWriter openLogFile(String path) throws FileNotFoundException {
		File logFile = new File(path);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(
					new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(logFile),
									"UTF-8"
							)
					)
			);
		} catch (UnsupportedEncodingException e) {  // Este erro nao deve acontecer em hipotese alguma
			throw new RuntimeException(e);          // a menos que a plataforma nao suporte UTF-8!  O_o
		}
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
			getLog().println(json.toString());
			return true;
		} catch (JSONException e) {
			return false;
		} catch (FileNotFoundException e) {
			return false;
		}
	}

	@Override
	public LogEntry cloneFor(String message) {
		try {
			DefaultLogEntryImpl obj = (DefaultLogEntryImpl)this.clone();
			obj.message = message;
			obj.timestamp = new Date();
			return obj;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLevel(String level) {
		this.level = level;
	}

}
