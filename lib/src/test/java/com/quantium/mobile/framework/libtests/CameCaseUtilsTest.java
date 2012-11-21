package com.quantium.mobile.framework.libtests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.quantium.mobile.framework.utils.CamelCaseUtils;

public class CameCaseUtilsTest {

	public static final String LOWER_CAMEL_TEST = "inUsuarioAtivoI18n";
	public static final String UPPER_CAMEL_TEST = "InUsuarioAtivoI18n";
	public static final String LOWER_UNDERSCORE_TEST = "in_usuario_ativo_i18n";
	public static final String UPPER_UNDERSCORE_TEST = "IN_USUARIO_ATIVO_I18N";

	@Test
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

	@Test
	public void testToUpperCaseAndUnderscores(){
		assertEquals(UPPER_UNDERSCORE_TEST,
				CamelCaseUtils.camelToUpper(LOWER_CAMEL_TEST));
	}

	@Test
	public void testToUpperCamelCase(){
		assertEquals(UPPER_CAMEL_TEST,
				CamelCaseUtils.toUpperCamelCase(LOWER_CAMEL_TEST));
	}

}
