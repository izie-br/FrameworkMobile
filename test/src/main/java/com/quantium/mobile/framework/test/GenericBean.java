package com.quantium.mobile.framework.test;


import java.io.Serializable;
import android.database.SQLException;
import com.quantium.mobile.framework.JsonSerializable;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class GenericBean
    implements Serializable, Cloneable
{
    public abstract JSONObject toJson();
    public abstract GenericBean jsonToObjectWithPrototype(JSONObject json)
        throws JSONException;
    public abstract boolean save() throws SQLException;
    public abstract boolean delete();
}
