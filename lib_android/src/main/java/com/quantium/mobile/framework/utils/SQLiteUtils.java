package com.quantium.mobile.framework.utils;

import java.util.Calendar;
import java.util.Date;


public class SQLiteUtils {

    private static final String ERRO_CLASSE_SEM_STRING_FORMAT =
            "Classe \"%s\" sem um metodo parse de sring correspodente.";

    public static final String dateTimeForColumnForSelect(String columnName) {
        return "datetime(" + columnName + ") as " + columnName.substring(columnName.indexOf(".") + 1);
    }

    public static final String dateTimeForColumnForWhere(String columnName) {
        return "datetime(" + columnName + ")";
    }

    public static int booleanToInteger(boolean b) {
        return b ? 1 : 0;
    }

    public static boolean integerToBoolean(int i) {
        return i != 0;
    }

    public static String parse(Object object) {
        if (CharSequence.class.isInstance(object))
            return object.toString();
        if (Number.class.isInstance(object))
            return object.toString();
        if (object instanceof Date)
            return DateUtil.timestampToString((Date) object);
        if (Calendar.class.isInstance(object))
            return DateUtil.timestampToString(((Calendar) object).getTime());
        if (object instanceof Boolean)
            return "" + booleanToInteger((Boolean) object);
        throw new RuntimeException(String.format(
                ERRO_CLASSE_SEM_STRING_FORMAT,
                object.getClass().getSimpleName()
        ));
    }


}
