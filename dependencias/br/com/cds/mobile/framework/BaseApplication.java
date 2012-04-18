package br.com.cds.mobile.framework;

import br.com.cds.mobile.framework.config.Aplicacao;
import android.app.Application;
import android.content.Context;

public abstract class BaseApplication extends Application{

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
