package com.quantium.mobile.framework.test;

import com.quantium.mobile.framework.BaseApplication;
import com.quantium.mobile.framework.test.db.DB;

public class Aplicacao extends BaseApplication {

    public static SessionFacade facade;

    public Aplicacao() {
        facade = new SessionFacade();
    }

    public static SessionFacade getFacade() {
        return facade;
    }

    @Override
    public int dbVersion() {
        return DB.DB_VERSAO;
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

}
