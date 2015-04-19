package com.quantium.mobile.framework.libandroidtest;

import java.util.Map;

public interface GenericVO {

    public abstract void toMap(Map<String, Object> map);

    public abstract Map<String, Object> toMap();

    public abstract void triggerObserver(String column);

    public abstract int hashCodeImpl();

    public abstract boolean equalsImpl(Object obj);

}