package com.quantium.mobile.geradores.util;

public class JavaBeanUtils {

    public static String getter(String property) {
        return "get" +
                Character.toUpperCase(property.charAt(0)) +
                property.substring(1);
    }

    public static String setter(String property) {
        return "set" +
                Character.toUpperCase(property.charAt(0)) +
                property.substring(1);
    }

}
