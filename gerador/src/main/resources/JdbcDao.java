package $package;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.WeakHashMap;

import java.sql.*;

import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.jdbc.JdbcQuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.jdbc.JdbcDao;
import com.quantium.mobile.framework.utils.StringUtil;
import com.quantium.mobile.framework.ToManyDAO;
import com.quantium.mobile.framework.Save;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.utils.CamelCaseUtils;

#if ($hasNullableAssociation)
#**#import java.util.Collection;
#**#import java.lang.ref.Reference;
#end
##
#if ($manyToOneAssociations.size() > 0)
#**#import java.lang.reflect.Proxy;
#**#import com.quantium.mobile.framework.DAO;
#**#import com.quantium.mobile.framework.LazyInvocationHandler;
#elseif ($hasNotNullableAssociation)
#**#import com.quantium.mobile.framework.DAO;
#end
##
#if ( $hasDateField)
#**#import java.util.Date;
#**#import com.quantium.mobile.framework.utils.DateUtil;
#end

public class ${Klass} implements JdbcDao<${Target}> {

    private static final String COUNT_BY_PRIMARY_KEYS =
        "SELECT COUNT(*) FROM ${table} WHERE ${primaryKey.LowerAndUnderscores}=?";

    private JdbcDAOFactory factory;

    public ${Klass}(JdbcDAOFactory factory){
        this.factory = factory;
    }

    private static class StatementCacheStruct {
        Connection connection;
        PreparedStatement statement;
        boolean lock;
    }

    private Map<String, StatementCacheStruct> stmCache =
        new WeakHashMap<String, StatementCacheStruct>();

    private PreparedStatement getStatement(String statement){
        StatementCacheStruct entry =
            stmCache.get(statement);
        Connection connection = factory.getConnection();
        try{
            if (entry == null || entry.connection.isClosed() || entry.lock ){
                PreparedStatement stm =
                    connection.prepareStatement(statement);
                /* if ( !(entry.lock) )*/ {
                    entry = new StatementCacheStruct();
                    entry.connection = connection;
                    entry.statement = stm;
                    entry.lock = true;
                    stmCache.put(statement, entry);
                }
                return stm;
            }
            entry.lock = true;
            entry.statement.clearParameters();
            return entry.statement;
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(StringUtil.getStackTrace(e));
        }
    }

    @Override
    public String[] getColumns() {
        return new String[] {
#foreach ($field in $fields)
            "${field.UpperAndUnderscores}",
#end
        };
    }

#parse("DAO.java.d/jdbcSave.java")

#parse("DAO.java.d/jdbcDelete.java")

#parse("DAO.java.d/jdbcQuery.java")

#parse("DAO.java.d/querySetForAssociations.java")

#parse("DAO.java.d/jdbcCursorToObject.java")

#parse("DAO.java.d/mapToObject.java")

#parse("DAO.java.d/jdbcManyToManyHandlers.java")

#parse("DAO.java.d/toManyDAO.java")

}

