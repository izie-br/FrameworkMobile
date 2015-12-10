package com.quantium.mobile.framework.utils;

import com.quantium.mobile.framework.DAO;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ValueParser {

    public String dateToDatabase(Date date) {
        if (date == null)
            return null;
        return DateUtil.timestampToString(date);
    }

    public <T> T extractAssociation(Map<?, ?> allFieldsMap,
                                    DAO<T> associationDao, String submapKey, String foreignKey) throws IOException {
        Object temp = allFieldsMap.get(submapKey);
        if (temp != null && temp instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> submap = (Map<String, Object>) temp;
            return associationDao.mapToObject(submap);
        } else {
            temp = allFieldsMap.get(foreignKey);
            String associationId = StringFromMap(temp);
            if (associationId != null) {
                T t = associationDao.get(associationId);
                if (t == null) {
                    Map<String, Object> associationMap = new HashMap<String, Object>();
                    associationMap.put("id", associationId);
                    return associationDao.mapToObject(associationMap);
                }
                return t;
            }
            return null;
        }
    }

    public int booleanToDatabase(boolean active) {
        if (active) {
            return 1;
        }
        return 0;
    }

    public Date dateFromDatabase(Object value) {
        if (value == null)
            return null;
        if (value instanceof Date)
            return (Date) value;
        if (value instanceof String)
            return DateUtil.stringToDate((String) value);
        throw new IllegalArgumentException(value.toString());
    }

    public boolean booleanFromDatabase(Object value) {
        if (value == null)
            return false;
        if (value instanceof Boolean)
            return (Boolean) value;
        if (value instanceof String) {
            if (value.equals("0"))
                return false;
            else if (value.equals("1"))
                return true;
            else
                return Boolean.parseBoolean((String) value);
        }
        if (value instanceof Number) {
            long val = ((Number) value).longValue();
            if (val == 0)
                return false;
            if (val == 1)
                return true;
        }
        throw new IllegalArgumentException(value.toString());
    }

    public String StringFromMap(Object value) {
        return (value == null) ? null : value.toString();
    }

    public Date DateFromMap(Object value) {
        if (value == null)
            return null;
        if (value instanceof Date)
            return (Date) value;
        if (value instanceof String)
            return DateUtil.stringToDate((String) value);
        throw new IllegalArgumentException(value.toString());
    }

    public boolean BooleanFromMap(Object value) {
        if (value == null)
            return false;
        if (value instanceof Boolean)
            return (Boolean) value;
        if (value instanceof String) {
            if (value.equals("0"))
                return false;
            else if (value.equals("1"))
                return true;
            else
                return Boolean.parseBoolean((String) value);
        }
        if (value instanceof Number) {
            long val = ((Number) value).longValue();
            if (val == 0)
                return false;
            if (val == 1)
                return true;
        }
        throw new IllegalArgumentException(value.toString());
    }

    public Object numberToDatabase(Number value) {
        return (value == null) ? null : value.toString();
    }

    public Object stringToDatabase(CharSequence value) {
        return (value == null) ? null : value.toString();
    }

    public long LongFromMap(Object value) {
        if (value == null)
            return 0l;
        if (value instanceof Number)
            return ((Number) value).longValue();
        if (value instanceof String) {
            String strValue = (String) value;
            return Long.parseLong(strValue);
        }
        throw new IllegalArgumentException();
    }

    public double DoubleFromMap(Object value) {
        if (value == null)
            return 0.0;
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        if (value instanceof String) {
            String strValue = (String) value;
            return Double.parseDouble(strValue);
        }
        throw new IllegalArgumentException(value.toString());
    }

}
