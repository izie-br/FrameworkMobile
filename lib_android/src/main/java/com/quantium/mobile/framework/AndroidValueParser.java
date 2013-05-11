package com.quantium.mobile.framework;

import com.quantium.mobile.framework.utils.ValueParser;

public class AndroidValueParser extends ValueParser {
	@Override
	public long parseLong(Object value) {
		try {
			return super.parseLong(value);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public double parseDouble(Object value) {
		try {
			return super.parseDouble(value);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}
}
