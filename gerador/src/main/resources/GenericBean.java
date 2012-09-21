
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import android.database.SQLException;

import com.quantium.mobile.framework.Save;
import com.quantium.mobile.framework.Session;

@SuppressWarnings("serial")
public abstract class GenericBean
    implements Serializable, Cloneable
{
    public abstract Map<String,Object> toMap(Map<String,Object> map);

    public Map<String,Object> toMap(){
        return toMap(new HashMap<String, Object>());
    }

    public abstract GenericBean mapToObject(Map<String,Object> json);
    public abstract boolean save(Session session, int flags) throws SQLException;
    public abstract boolean delete();

    public boolean save(Session session) throws SQLException{
        return save(session, Save.INSERT_IF_NULL_PK);
    }

    protected int onPreSave(int flags){
        return flags;
    }

}
