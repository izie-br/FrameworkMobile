package com.quantium.mobile.framework;

import com.quantium.mobile.framework.validation.ValidationError;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

public interface BaseGenericVO {

    public abstract void toMap(Map<String, Object> map);

    public abstract Map<String, Object> toMap();

    public abstract void registerObserver(Observer observer);

    public abstract void unregisterObserver(Observer observer);

    public abstract void triggerObserver(String column);

    public abstract int hashCodeImpl();

    public abstract boolean equalsImpl(Object obj);

    public abstract Collection<ValidationError> validate();

    public String getId();

    public void setId(String id);

    public Date getCreatedAt();

    public void setCreatedAt(Date createdAt);

    public Date getLastModified();

    public void setLastModified(Date lastModified);

    public Date getInactivatedAt();

    public void setInactivatedAt(Date inactivatedAt);
}
