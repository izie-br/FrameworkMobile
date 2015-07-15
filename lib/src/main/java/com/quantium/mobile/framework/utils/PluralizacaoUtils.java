package com.quantium.mobile.framework.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluralizacaoUtils {

    @SuppressWarnings("serial")
    private static List<PluralizacaoUtils.Regra> regras = new ArrayList<PluralizacaoUtils.Regra>() {
        {
            //add(new Regra("(.*)ao$", "$1oes"));
            //add(new Regra("(.*[r])$", "$1es"));
            add(new Regra("(.*[aeiou][y])$", "$1s", "(.*[aeiou][y])$"));
            add(new Regra("y(.*)", "$1ies", "(.*[y])$"));
//            add(new Regra("(.*[y])$", "$1ies"));
//            add(new Regra("(.*[sz])$", "$1es"));
            add(new Regra("s(.*)", "$1s", "(.*[s])$"));
            add(new Regra("(.*)$", "$1s", "(.*)$"));
        }
    };

    public static String pluralizar(String singular) {
        for (Regra regra : regras) {
            if (singular.matches(regra.matches)) {
                Pattern pattern = Pattern.compile(regra.replaceGroup);
                Matcher mobj = pattern.matcher(singular);
                StringBuffer sb = new StringBuffer();
                mobj.find();
                mobj.appendReplacement(sb, regra.substitution);
                return sb.toString();
            }
        }
        return singular;
    }

    public static class Regra {

        private String replaceGroup;
        private String substitution;
        private String matches;

        public Regra(String replaceGroup, String substitution, String matches) {
            super();
            this.replaceGroup = replaceGroup;
            this.substitution = substitution;
            this.matches = matches;
        }

    }

}
