package com.quantium.mobile.framework.test;

import com.quantium.mobile.framework.BaseApplication;

public class TestApplication extends BaseApplication {

	@Override
	public int dbVersion() {
		return 0;
	}

	@Override
	public String userName() {
		return "test";
	}

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
