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

    public QuerySet<${Target}> query() {
        return query(null);
    }

    public QuerySet<${Target}> query(Q q) {
        QuerySet<${Target}> queryset =
            new QuerySetImpl(this.factory);
        if (q == null) {
            return queryset;
        }
        return queryset.filter(q);
    }

#foreach ($association in $oneToManyAssociations)
#if ($association.Nullable)
    private static class ${association.Klass}NullFkThread implements Runnable {

        ${Target} target;
        JdbcDAOFactory factory;

        private ${association.Klass}NullFkThread(JdbcDAOFactory factory, ${Target} target) {
            this.factory = factory;
            this.target = target;
        }

        @Override
        public void run() {
            Collection<Reference<${association.Klass}>> references = factory.lookupForClass(${association.Klass}.class);
            for (Reference<${association.Klass}> reference : references) {
                ${association.Klass} obj = (${association.Klass})reference.get();
                if (obj == null)
                    continue;
                if (target.equals(obj.get${Target}()) )
                    obj.set${Target}(null);
            }
        }
    }

#end##if ($association.Nullable)
#end##foreach ($association in $oneToManyAssociations)
    public boolean delete(${Target} target) throws IOException {
        if (${nullPkCondition}) {
            return false;
        }
        try {
            PreparedStatement stm;
#foreach ($association in $oneToManyAssociations)
#if ($association.Nullable)
            stm = getStatement(
                    "UPDATE ${association.Table} SET " +
                    "${association.ForeignKey.LowerAndUnderscores}=NULL " +
                    "WHERE ${association.ForeignKey.LowerAndUnderscores}=?");
            try {
                stm.set${association.ReferenceKey.Klass}(1, target.${getter[$association.ReferenceKey]}());
                stm.executeUpdate();
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(StringUtil.getStackTrace(e));
            }
            Runnable _${association.Klass}NullFkThread = null;
            _${association.Klass}NullFkThread = new ${association.Klass}NullFkThread(factory, target);
            //_${association.Klass}NullFkThread.start();

#else##association_nullable
            DAO<${association.Klass}> daoFor${association.Klass} = (DAO<${association.Klass}>)factory.getDaoFor(${association.Klass}.class);
            for (${association.Klass} obj: target.get${association.Pluralized}().all()) {
                daoFor${association.Klass}.delete(obj);
            }
#end##association_nullable
#end##foreach_oneToMany
#foreach ($association in $manyToManyRelation)
            stm = getStatement("DELETE FROM ${association.ThroughTable} WHERE ${association.ThroughReferenceKey.LowerAndUnderscores} = ?");
            try {
                stm.set${association.ReferenceKey.Klass}(1, target.${getter[$association.ReferenceKey.UpperCamel]}());
                stm.executeUpdate();
            } catch (java.sql.SQLException e) {
                throw new IOException(StringUtil.getStackTrace(e));
            }
#end##foreach_manyToMany
            stm = getStatement("DELETE FROM ${table} WHERE ${queryByPrimaryKey}");
            int affected;
            try {
#foreach ($field in $primaryKeys)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
                stm.set${field.Klass}(
                        ${foreach.count},
                        (target.get${association.Klass}() == null) ?
                            0 :
                            target.get${association.Klass}().get${association.ReferenceKey.UpperCamel}());
#else##if (!$associationForField[$field])
#if ($field.Klass.equals("Date") )
                stm.setTimestamp(
                        ${foreach.count},
                        (target.${getter[$field]}() == null) ?
                            null :
                            new java.sql.Timestamp(target.${getter[$field]}().getTime()));
#else##if_class_equals
                 stm.set${field.Klass}(${foreach.count}, target.${getter[$field]}());
#end##if_class_equals
#end##if ($associationForField[$field])
#end##foreach
                affected = stm.executeUpdate();
            } catch (java.sql.SQLException e) {
                throw new IOException(StringUtil.getStackTrace(e));
            }
            if (affected == 0) {
                return false;
            }
#foreach ($relation in $oneToManyAssociations)
#if ($relation.Nullable)
            if (_${relation.Klass}NullFkThread != null) {
                try {
                    // _${relation.Klass}NullFkThread.join();
                    _${relation.Klass}NullFkThread.run();
                } catch (Exception e) {
                    LogPadrao.e(e);
                }
            }
#end##if ($relation.Nullable)
#end##foreach ($relation in $oneToManyAssociations)
        } finally {
            //db.endTransaction();
        }
        Serializable pks [] = new Serializable[]{
#foreach ($field in $primaryKeys)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
            target.get${association.Klass}().get${association.ReferenceKey.UpperCamel}(),
#else##if (!$associationForField[$field])
            target.${getter[$field]}(),
#end##if ($associationForField[$field])
#end##foreach ($key in $primaryKeys)
        };
        factory.removeFromCache(${Target}.class, pks);
        return true;
    }

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

    final class QuerySetImpl extends JdbcQuerySet<${Target}> {

        private JdbcDAOFactory factory;
        private ${Klass} dao;

        protected QuerySetImpl(JdbcDAOFactory factory) {
            this.factory = factory;
            this.dao = (${Klass})factory.getDaoFor(${Target}.class);
        }

        @Override
        protected Connection getConnection() {
            return factory.getConnection();
        }

        @Override
        public Table getTable() {
            return ${Target}._TABLE;
        }

        @Override
        protected Table.Column<?> [] getColunas() {
            return new Table.Column[] {
#foreach ($field in $fields)
                ${Target}.${field.UpperAndUnderscores},
#end
            };
        }

        protected ${Target} cursorToObject(ResultSet cursor) {
            return dao.cursorToObject(cursor, true);
        }

    }

}

