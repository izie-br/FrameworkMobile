package com.quantium.mobile.framework.utils;

import org.atteo.evo.inflector.English;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluralizacaoUtils {

    public static String pluralizar(String singular) {
        return English.plural(singular);
    }

}
