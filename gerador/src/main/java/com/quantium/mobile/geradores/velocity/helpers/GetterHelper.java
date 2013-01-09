package com.quantium.mobile.geradores.velocity.helpers;

import com.quantium.mobile.geradores.javabean.Property;

public class GetterHelper {

	public String get(Property prop){
		if (prop.getKlass().equals(Boolean.class.getSimpleName()))
			return "is"  + prop.getUpperCamel();
		else
			return "get" + prop.getUpperCamel();
	}

}
