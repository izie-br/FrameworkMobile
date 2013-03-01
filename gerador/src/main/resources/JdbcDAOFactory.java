package ${package};

import java.sql.Connection;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.DAOFactory;
import com.quantium.mobile.framework.db.FirstLevelCache;

public abstract class JdbcDAOFactory
        extends FirstLevelCache implements DAOFactory
{

    public abstract Connection getConnection();

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
