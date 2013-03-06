package com.quantium.mobile.framework.utils;

import java.util.Date;

public class ValueParser {

    public long parseLong(Object value) {
        if (value == null)
            return 0;
        if (value instanceof Number)
            return ((Number)value).longValue();
        if (value instanceof String) {
            String strValue = (String) value;
            return Long.parseLong(strValue);
        }
        throw new IllegalArgumentException();
    }

    public double parseDouble(Object value) {
        if (value == null)
            return 0.0;
        if (value instanceof Number)
            return ((Number)value).doubleValue();
        if (value instanceof String) {
            String strValue = (String) value;
            return Double.parseDouble(strValue);
        }
        throw new IllegalArgumentException(value.toString());
    }

    public boolean parseBoolean(Object value) {
        if (value == null)
            return false;
        if (value instanceof Boolean)
            return (Boolean)value;
        if (value instanceof String) {
            if (value.equals("0"))
                return false;
            else if (value.equals("1"))
                return true;
            else
                return Boolean.parseBoolean((String)value);
        }
        if (value instanceof Number) {
            long val = ((Number)value).longValue();
            if (val == 0)
               return false;
            if (val == 1)
               return true;
        }
        throw new IllegalArgumentException(value.toString());
    }

    public Date parseDate(Object value) {
        if (value == null)
            return null;
        if (value instanceof Date)
            return (Date)value;
        if (value instanceof String)
            return DateUtil.stringToDate((String)value);
        throw new IllegalArgumentException(value.toString());
    }

    public String parseString(Object value) {
        return (value == null)? null : value.toString();
    }

}
