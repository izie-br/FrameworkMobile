package ${package};

import com.quantium.mobile.framework.Session;
import com.quantium.mobile.framework.DAOFactory;

public abstract class AbstractSessionFacade implements Session, DAOFactory {

    @Override
    public DAOFactory getDAOFactory(){
        return this;
    }

    @Override
    public Object getDaoFor(Class<?> klass){
        if (klass == null)
            return null;
        String name = klass.getName();
#foreach ($Klass in $Klasses)
       #if ($foreach.index !=0) else#end if (name.equals(${Klass}.class.getName()))
            return new ${Klass}DAOSQLite(this, this);
#end
        return null;
    }

}
