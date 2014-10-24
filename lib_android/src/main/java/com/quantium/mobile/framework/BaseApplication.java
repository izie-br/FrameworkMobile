package com.quantium.mobile.framework;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import com.quantium.mobile.framework.logging.LogPadrao;

public abstract class BaseApplication extends MultiDexApplication {

    protected static BaseApplication instance;
    private boolean installed = false;

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

    public boolean isInstalled() {
        return installed;
    }

    @Override
    protected final void attachBaseContext(Context base) {
        try {
            super.attachBaseContext(base);
            installed = true;
        } catch (Throwable t) {
            LogPadrao.e(t);
        }
    }

}
