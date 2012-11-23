package ${package};

import java.sql.Connection;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.db.AbstractSQLiteDAOFactory;

public abstract class JdbcDAOFactory extends AbstractSQLiteDAOFactory {

    public abstract Connection getConnection();

    @Override
    @SuppressWarnings("unchecked")
    public <T> DAO<T> getDaoFor(Class<T> klass){
        if (klass == null)
            return null;
        String name = klass.getName();
#foreach ($Klass in $Klasses)
       #if ($foreach.index !=0) else#end if (name.equals(${Klass}.class.getName()))
            return ((DAO<T>)new ${Klass}JdbcDAO(this));
#end
        return null;
    }

}
