
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import android.database.SQLException;

import com.quantium.mobile.framework.Save;

@SuppressWarnings("serial")
public abstract class GenericBean
    implements Serializable, Cloneable
{
    public abstract Map<String,Object> toMap(Map<String,Object> map);

    public Map<String,Object> toMap(){
        return toMap(new HashMap<String, Object>());
    }

    public abstract GenericBean mapToObject(Map<String,Object> json);
    public abstract boolean save(int flags) throws SQLException;
    public abstract boolean delete();

    public boolean save() throws SQLException{
        return save(Save.INSERT_IF_NULL_PK);
    }

    protected int onPreSave(int flags){
        return flags;
    }

}
