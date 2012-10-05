package com.quantium.mobile.framework.test;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class GenericBean
    implements Serializable, Cloneable
{
    public abstract Map<String,Object> toMap(Map<String,Object> map);

    public Map<String,Object> toMap(){
        return toMap(new HashMap<String, Object>());
    }

    public void triggerObserver(String column){}

    public abstract GenericBean mapToObject(Map<String,Object> json);

}
