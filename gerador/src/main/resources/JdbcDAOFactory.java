package ${package};

import java.sql.Connection;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.DAOFactory;
import com.quantium.mobile.framework.db.FirstLevelCache;
import com.quantium.mobile.framework.utils.ValueParser;

public abstract class JdbcDAOFactory
        extends FirstLevelCache implements DAOFactory
{

    private ValueParser parser;

    public JdbcDAOFactory(ValueParser valueParser) {
        this.parser = valueParser;
    }

    public JdbcDAOFactory() {
        this.parser = new ValueParser();
    }

    public ValueParser getValueParser() {
        return this.parser;
    }

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
