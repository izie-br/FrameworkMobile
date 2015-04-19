package com.quantium.mobile.framework.test;


import com.quantium.mobile.framework.Observer;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class GenericBean implements Serializable, GenericVO {

    /* (non-Javadoc)
     * @see com.quantium.mobile.framework.test.GenericVO#toMap(java.util.Map)
	 */
    @Override
    public abstract void toMap(Map<String, Object> map);

    /* (non-Javadoc)
	 * @see com.quantium.mobile.framework.test.GenericVO#toMap()
	 */
    @Override
    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        toMap(map);
        return map;
    }

    /* (non-Javadoc)
	 * @see com.quantium.mobile.framework.test.GenericVO#triggerObserver(java.lang.String)
	 */
    @Override
    public void triggerObserver(String column) {
    }

    /* (non-Javadoc)
	 * @see com.quantium.mobile.framework.test.GenericVO#hashCodeImpl()
	 */
    @Override
    public abstract int hashCodeImpl();

    /* (non-Javadoc)
	 * @see com.quantium.mobile.framework.test.GenericVO#equalsImpl(java.lang.Object)
	 */
    @Override
    public abstract boolean equalsImpl(Object obj);

    @Override
    public int hashCode() {
        return hashCodeImpl();
    }

    @Override
    public boolean equals(Object obj) {
        return equalsImpl(obj);
    }

    @Override
    public void registerObserver(Observer observer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void unregisterObserver(Observer observer) {
        // TODO Auto-generated method stub

    }

    @Override
    public Date getCreatedAt() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCreatedAt(Date createdAt) {
        // TODO Auto-generated method stub

    }

    @Override
    public Date getInactivatedAt() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setInactivatedAt(Date inactivatedAt) {
        // TODO Auto-generated method stub

    }

    @Override
    public Date getLastModified() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLastModified(Date lastModified) {
        // TODO Auto-generated method stub

    }
}
