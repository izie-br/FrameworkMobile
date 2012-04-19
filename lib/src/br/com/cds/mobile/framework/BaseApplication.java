package br.com.cds.mobile.framework;

import android.app.Application;
import android.content.Context;

public class BaseApplication extends Application{

	public static BaseApplication instance;

	@Override
	public void onCreate() {
		super.onCreate();
		BaseApplication.instance = this;
	}

	public static Context getContext(){
		return BaseApplication.instance.getApplicationContext();
	}

}
