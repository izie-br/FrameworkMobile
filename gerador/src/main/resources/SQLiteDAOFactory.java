package ${package};

import android.database.sqlite.SQLiteDatabase;
import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.db.AbstractSQLiteDAOFactory;

${Imports}

public abstract class SQLiteDAOFactory extends AbstractSQLiteDAOFactory {

    public abstract SQLiteDatabase getDb();

    @Override
    @SuppressWarnings("unchecked")
    public <T> DAO<T> getDaoFor(Class<T> klass){
        if (klass == null)
            return null;
        String name = klass.getName();
#foreach ($Klass in $Klasses)
       #if ($foreach.index !=0) else#end if (name.equals(${Klass}.class.getName()))
            return ((DAO<T>)new ${Klass}DAOSQLite(this));
#end
        return null;
    }

}
