package com.quantium.mobile.framework.test.utils;

import android.test.ActivityInstrumentationTestCase2;

import com.quantium.mobile.framework.test.TestActivity;
import com.quantium.mobile.framework.utils.CamelCaseUtils;

public class CameCaseUtilsTests extends
		ActivityInstrumentationTestCase2<TestActivity> {

	public static final String LOWER_CAMEL_TEST = "inUsuarioAtivo";
	public static final String UPPER_CAMEL_TEST = "InUsuarioAtivo";
	public static final String LOWER_UNDERSCORE_TEST = "in_usuario_ativo";
	public static final String UPPER_UNDERSCORE_TEST = "IN_USUARIO_ATIVO";

	public CameCaseUtilsTests() {
		super("com.quantium.mobile.framework.test", TestActivity.class);
	}

	public void testToLowerCamelCase(){
		assertEquals(LOWER_CAMEL_TEST,
				CamelCaseUtils.toLowerCamelCase(LOWER_CAMEL_TEST));
		assertEquals(LOWER_CAMEL_TEST,
				CamelCaseUtils.toLowerCamelCase(UPPER_CAMEL_TEST));
		assertEquals(LOWER_CAMEL_TEST,
				CamelCaseUtils.toLowerCamelCase(LOWER_UNDERSCORE_TEST));
		assertEquals(LOWER_CAMEL_TEST,
				CamelCaseUtils.toLowerCamelCase(UPPER_UNDERSCORE_TEST));
	}

	public void testToUpperCaseAndUnderscores(){
		assertEquals(UPPER_UNDERSCORE_TEST,
				CamelCaseUtils.camelToUpper(LOWER_CAMEL_TEST));
	}

	public void testToUpperCamelCase(){
		assertEquals(UPPER_CAMEL_TEST,
				CamelCaseUtils.toUpperCamelCase(LOWER_CAMEL_TEST));
	}

}
