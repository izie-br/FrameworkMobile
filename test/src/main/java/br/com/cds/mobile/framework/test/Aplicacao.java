package br.com.cds.mobile.framework.test;


import br.com.cds.mobile.framework.BaseApplication;
import br.com.cds.mobile.framework.test.db.DB;

public class Aplicacao extends BaseApplication{

	@Override
	public int dbVersion() {
		return DB.DB_VERSAO;
	}

	@Override
	public String userName() {
		// TODO Auto-generated method stub
		return "";
	}

}
