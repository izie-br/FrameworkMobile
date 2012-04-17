package br.com.cds.mobile.framework.config;

import android.app.Application;
import android.content.Context;

public class Aplicacao extends Application{

	public static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		Aplicacao.context = getApplicationContext();
	}

	public static Context getContext(){
		return Aplicacao.context;
	}

}
