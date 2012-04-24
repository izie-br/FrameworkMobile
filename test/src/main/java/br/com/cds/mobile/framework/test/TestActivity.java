package br.com.cds.mobile.framework.test;

import android.app.Activity;
import android.os.Bundle;
import android.widget.DatePicker;
import de.akquinet.android.androlog.Log;

public class TestActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.init();
		// Log a message (only on dev platform)
		Log.i(this, "onCreate");

		setContentView(R.layout.main);
	}

}

