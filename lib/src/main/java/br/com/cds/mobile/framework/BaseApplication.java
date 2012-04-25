package br.com.cds.mobile.framework;

import android.app.Application;
import android.content.Context;

public abstract class BaseApplication extends Application{

	public static BaseApplication instance;

	public abstract int dbVersion();

	public abstract String userName();

	@Override
	public void onCreate() {
		super.onCreate();
		BaseApplication.instance = this;
	}

	public static Context getContext(){
		return BaseApplication.instance.getApplicationContext();
	}

	public static int getDBVersion(){
		return instance.dbVersion();
	}

	public static String getUserName(){
		return instance.userName();
	}

}
