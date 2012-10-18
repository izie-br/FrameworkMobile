package com.quantium.mobile.framework.test;

import com.quantium.mobile.framework.test.query.QueryTests;

import android.app.Activity;
import android.os.Bundle;

public class TestActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		new QueryTests()
		.testQString();
	}

}

