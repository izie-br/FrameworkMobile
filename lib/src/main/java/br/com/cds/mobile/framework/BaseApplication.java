package br.com.cds.mobile.framework;

import br.com.cds.mobile.framework.sync.Synchronizer;
import android.app.Application;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;

public abstract class BaseApplication extends Application{

	private static BaseApplication instance;
	private static Synchronizer sincronizador;
	private static ServiceConnection serviceConnection;

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

	public static SharedPreferences getPreferences(){
		return getContext().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
	}

	public static SharedPreferences getPreferences(String nome){
		return getContext().getSharedPreferences(getContext().getPackageName()+nome, Context.MODE_PRIVATE);
	}

	public static int getDBVersion(){
		return instance.dbVersion();
	}

	public static String getUserName(){
		return instance.userName();
	}

	public static Synchronizer getSincronizador() {
		return sincronizador;
	}

	public static void setSincronizador(Synchronizer sincronizador) {
		BaseApplication.sincronizador = sincronizador;
	}

	public static ServiceConnection getServiceConnection() {
		return serviceConnection;
	}

	public static void setServiceConnection(ServiceConnection serviceConnection) {
		BaseApplication.serviceConnection = serviceConnection;
	}

}
