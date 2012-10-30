package com.quantium.mobile.framework.test;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class GenericBean
    implements Serializable
{
    public abstract void toMap(Map<String,Object> map);

    public Map<String,Object> toMap(){
        HashMap<String, Object> map = new HashMap<String, Object>();
        toMap(map);
        return map;
    }

    public void triggerObserver(String column){}

    public abstract int hashCodeImpl();
    public abstract boolean equalsImpl(Object obj);
    public abstract GenericBean cloneImpl(Object obj);

    @Override
    public int hashCode() {
        return hashCodeImpl();
    }

    @Override
    public boolean equals(Object obj) {
        return equalsImpl(obj);
    }
}
