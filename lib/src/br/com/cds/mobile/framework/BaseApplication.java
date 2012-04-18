package br.com.cds.mobile.framework;

import android.app.Application;
import android.content.Context;

public class BaseApplication extends Application{

	public static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		BaseApplication.context = getApplicationContext();
	}

	public static Context getContext(){
		return BaseApplication.context;
	}

}
