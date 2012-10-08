package ${package};

import com.quantium.mobile.framework.Session;
import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.DAOFactory;

public abstract class AbstractSessionFacade implements Session, DAOFactory {

    @Override
    public DAOFactory getDAOFactory(){
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DAO<T> getDaoFor(Class<T> klass){
        if (klass == null)
            return null;
        String name = klass.getName();
#foreach ($Klass in $Klasses)
       #if ($foreach.index !=0) else#end if (name.equals(${Klass}.class.getName()))
            return ((DAO<T>)new ${Klass}DAOSQLite(this, this));
#end
        return null;
    }

}
