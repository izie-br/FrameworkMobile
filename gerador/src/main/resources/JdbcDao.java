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
        "SELECT COUNT(*) FROM ${table} WHERE ${queryByPrimaryKey}";

    private JdbcDAOFactory factory;

    public ${Klass}(JdbcDAOFactory factory){
        this.factory = factory;
    }

    private class StatementCacheStruct {
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

#parse("DAO.java.d/jdbcSave.java")

#parse("DAO.java.d/jdbcDelete.java")

#parse("DAO.java.d/jdbcQuery.java")

#parse("DAO.java.d/querySetForAssociations.java")

#parse("DAO.java.d/jdbcCursorToObject.java")

#parse("DAO.java.d/mapToObject.java")
    @Override
    public String[] getColumns() {
        return new String[] {
#foreach ($field in $fields)
            "${field.UpperAndUnderscores}",
#end
        };
    }

#foreach ($association in $manyToManyAssociations)
    public boolean add${association.Klass}To${Target}(${association.Klass} obj, $Target target) throws IOException {
        long value;
#if (${association.IsThisTableA})
        if (target.${getter[$association.ReferenceA]}() == ${defaultId}) {
            return false;
        }
        PreparedStatement stm = getStatement(
            "INSERT INTO ${association.JoinTable} (" +
                "${association.KeyToA.LowerAndUnderscores}," +
                "${association.KeyToB.LowerAndUnderscores}" +
            ") VALUES (?,?)");
        try {
            stm.set${association.KeyToA.Klass}(1, target.${getter[$association.ReferenceA]}());
            stm.set${association.KeyToB.Klass}(2, obj.${getter[$association.ReferenceB]}());
#else##(${association.IsThisTableA)
        if (target.${getter[$association.ReferenceB]}() == ${defaultId}) {
            return false;
        }
        PreparedStatement stm = getStatement(
            "INSERT INTO ${association.JoinTable} (" +
                "${association.KeyToA.LowerAndUnderscores}," +
                "${association.KeyToB.LowerAndUnderscores}" +
            ") VALUES (?,?)");
        try {
            stm.set${association.KeyToA.Klass}(1, obj.${getter[$association.ReferenceA]}());
            stm.set${association.KeyToB.Klass}(2, target.${getter[$association.ReferenceB]}());
#end##(${association.IsThisTableA})
            value = stm.executeUpdate();
        } catch (java.sql.SQLException e){
            throw new IOException(StringUtil.getStackTrace(e));
        }
        return (value > 0);
    }


    public boolean remove${association.Klass}From${Target}(${association.Klass} obj, $Target target) throws IOException {
#if (${association.IsThisTableA})
        if (target.${getter[$association.ReferenceA]}() == ${defaultId}) {
            return false;
        }
        PreparedStatement stm = getStatement(
            "DELETE FROM ${association.JoinTable} WHERE " +
            "${association.KeyToA.LowerAndUnderscores} = ? AND " +
            "${association.KeyToB.LowerAndUnderscores} = ?" +
            "LIMIT 1");
        try {
            stm.set${association.KeyToA.Klass}(1, target.${getter[$association.ReferenceA]}());
            stm.set${association.KeyToB.Klass}(2, obj.${getter[$association.ReferenceB]}());
#else##(${association.IsThisTableA})
        if (target.${getter[$association.ReferenceB]}() == ${defaultId}) {
            return false;
        }
        PreparedStatement stm = getStatement(
            "DELETE FROM ${association.JoinTable} WHERE " +
            "${association.KeyToA.LowerAndUnderscores} = ? AND " +
            "${association.KeyToB.LowerAndUnderscores} = ?" +
            "LIMIT 1");
        try {
            stm.set${association.KeyToA.Klass}(1, obj.${getter[$association.ReferenceA]}());
            stm.set${association.KeyToB.Klass}(2, target.${getter[$association.ReferenceB]}());
#end##(${association.IsThisTableA})
            long affected = stm.executeUpdate();
            return (affected == 1);
        } catch (java.sql.SQLException e) {
            throw new IOException(StringUtil.getStackTrace(e));
        }
    }
#end##foreach_manyToManyAssociation

#parse("DAO.java.d/toManyDAO.java")

}

