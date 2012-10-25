package com.quantium.mobile.framework.test;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.quantium.mobile.framework.DAOFactory;

@SuppressWarnings("serial")
public abstract class GenericBean
    implements Serializable, Cloneable
{
    public abstract void toMap(Map<String,Object> map);

    public Map<String,Object> toMap(){
        HashMap<String, Object> map = new HashMap<String, Object>();
        toMap(map);
        return map;
    }

    public void triggerObserver(String column){}

    public abstract GenericBean mapToObject(Map<String,Object> json);
    public abstract GenericBean mapToObject(Map<String, Object> map, DAOFactory daoFactory);

}
