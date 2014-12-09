package com.quantium.mobile.framework.test;

import com.quantium.mobile.framework.test.db.DB;
import com.quantium.mobile.framework.BaseApplication;

public class Aplicacao extends BaseApplication {

	@Override
	public int dbVersion() {
		return DB.DB_VERSAO;
	}

	public static SessionFacade facade;

    public Aplicacao(){
        facade = new SessionFacade();
    }

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public String userName() {
		// TODO Auto-generated method stub
		return "";
	}

	public static SessionFacade getFacade() {
		return facade;
	}

}
