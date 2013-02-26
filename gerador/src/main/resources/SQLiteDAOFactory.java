package ${package};

import android.database.sqlite.SQLiteDatabase;
import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.db.AbstractSQLiteDAOFactory;

public abstract class SQLiteDAOFactory extends AbstractSQLiteDAOFactory {

    public abstract SQLiteDatabase getDb();

    @Override
    @SuppressWarnings("unchecked")
    public <T> DAO<T> getDaoFor(Class<T> klass){
        if (klass == null)
            return null;
#foreach ($Klass in $Klasses.keySet())
       #if ($foreach.index !=0) else#end if (${Klass}.class.isAssignableFrom(klass))
            return ((DAO<T>)new ${Klasses[$Klass]}(this));
#end
        return null;
    }

}
