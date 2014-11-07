package com.quantium.mobile.framework;

import android.app.Application;
import android.content.Context;
import com.quantium.mobile.framework.logging.LogPadrao;

public abstract class BaseApplication extends Application {

    protected static BaseApplication instance;

    public abstract int dbVersion();

    public abstract String userName();

    @Override
    public void onCreate() {
        super.onCreate();
        BaseApplication.instance = this;
    }

    public static Context getContext() {
        return BaseApplication.instance.getApplicationContext();
    }

//	public static SharedPreferences getPreferences(){
//		return getContext().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
//	}
//
//	public static SharedPreferences getPreferences(String nome){
//		return getContext().getSharedPreferences(getContext().getPackageName()+nome, Context.MODE_PRIVATE);
//	}

    public static int getDBVersion() {
        return instance.dbVersion();
    }

    public static String getUserName() {
        return instance.userName();
    }

}
