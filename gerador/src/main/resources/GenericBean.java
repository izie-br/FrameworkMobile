
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

    @Override
    protected Object clone() {
        Object obj;
        try {
            obj = cloneImpl(super.clone());
        } catch (CloneNotSupportedException e) {
            return null;
        }
        return obj;
    }
}
