package ${package};

import android.database.sqlite.SQLiteDatabase;

import com.quantium.mobile.framework.BaseModelFacade;
import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.DAOFactory;
import com.quantium.mobile.framework.db.FirstLevelCache;
import com.quantium.mobile.framework.utils.ValueParser;

public abstract class SQLiteDAOFactory extends FirstLevelCache
        implements DAOFactory
{
// Exemplo de TrimTimer
//
//    private Timer trimTimer = new Timer(/*isDaemon=*/true);
//    {
//        trimTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                trim();
//            }
//        }, 15*1000, 15*1000);
//    }
//
//    @Override
//    protected void finalize() throws Throwable {
//        this.trimTimer.cancel();
//        super.finalize();
//    }

    private ValueParser parser;
    private BaseModelFacade modelFacade;

    public SQLiteDAOFactory(ValueParser valueParser) {
        this.parser = valueParser;
    }

    public SQLiteDAOFactory() {
        this.parser = new ValueParser();
    }
    
    
    @Override
	public BaseModelFacade getModelFacade() {
		return modelFacade;
	}
    
    @Override
	public void setModelFacade(BaseModelFacade modelFacade) {
    	this.modelFacade = modelFacade;	
	}

    public ValueParser getValueParser() {
        return this.parser;
    }

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
