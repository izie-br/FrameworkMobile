
import java.io.Serializable;
import android.database.SQLException;
import org.json.JSONException;
import org.json.JSONObject;

import com.quantium.mobile.framework.Save;

@SuppressWarnings("serial")
public abstract class GenericBean
    implements Serializable, Cloneable
{
    public abstract JSONObject toJson();
    public abstract GenericBean jsonToObjectWithPrototype(JSONObject json)
        throws JSONException;
    public abstract boolean save(int flags) throws SQLException;
    public abstract boolean delete();

    public boolean save() throws SQLException{
        return save(Save.INSERT_IF_NULL_PK);
    }

    protected int onPreSave(int flags){
        return flags;
    }

}
