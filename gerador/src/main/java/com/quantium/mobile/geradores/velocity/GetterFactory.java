package com.quantium.mobile.geradores.velocity;

import com.quantium.mobile.geradores.javabean.Property;

public class GetterFactory {

	public String get(Property prop){
		if (prop.getKlass().equals(Boolean.class.getSimpleName()))
			return "is"  + prop.getUpperCamel();
		else
			return "get" + prop.getUpperCamel();
	}

}
