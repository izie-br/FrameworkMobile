package br.com.cds.mobile.framework.test;

import br.com.cds.mobile.framework.BaseApplication;

public class TestApplication extends BaseApplication {

	@Override
	public int dbVersion() {
		return 0;
	}

	@Override
	public String userName() {
		return "test";
	}

}
