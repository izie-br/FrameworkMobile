
import java.io.Serializable;
import android.database.SQLException;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public abstract class GenericBean
    implements Serializable, Cloneable
{
    public abstract JSONObject toJson();
    public abstract GenericBean jsonToObjectWithPrototype(JSONObject json)
        throws JSONException;
    public abstract boolean save() throws SQLException;
    public abstract boolean delete();

    protected int onPreSave(int flags){
        return flags;
    }

}
