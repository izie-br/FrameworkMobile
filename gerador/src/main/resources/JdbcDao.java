##
## String de busca e arrays de argumentos
##
#set ($primaryKeysArgs = "new String[]{")
#set ($queryByPrimaryKey = "")
#set ($nullPkCondition = "")
#foreach ($field in $primaryKeys)
#if ($foreach.index !=0)
#set ($queryByPrimaryKey = $queryByPrimaryKey + " AND ")
#set ($nullPkCondition = $nullPkCondition + " || ")
#end##if ($foreach.index !=0)
#set ($queryByPrimaryKey = $queryByPrimaryKey +
                           "${field.LowerAndUnderscores} = ?")
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
#set ($primaryKeysArgs = $primaryKeysArgs +
                         "((${field.Klass})target.get${association.Klass}()" +
                         ".get${association.ReferenceKey.UpperCamel}())" +
                         ".toString(),")
#set ($nullPkCondition = $nullPkCondition +
                         "target.get${association.Klass}() == null ||" +
                         "target.get${association.Klass}()" +
                         ".get${association.ReferenceKey.UpperCamel}() " +
                         "== ${defaultId}")
#else##if (!$associationForField[$field])
#set ($primaryKeysArgs = $primaryKeysArgs +
                         "((${field.Klass})target.${getter[$field]}())" +
                         ".toString(),")
#set ($nullPkCondition = $nullPkCondition +
                         "target.${getter[$field]}() == ${defaultId}" )
#end##if ($associationForField[$field])
#end##foreach ($field in $primaryKeys)
#set ($primaryKeysArgs = $primaryKeysArgs + "}")
package $package;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.WeakHashMap;

import java.sql.*;

import com.quantium.mobile.framework.logging.LogPadrao;
#if ($manyToOneAssociations.size() > 0)
import java.lang.reflect.Proxy;
import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.LazyInvocationHandler;
#elseif ($hasNotNullableAssociation)
import com.quantium.mobile.framework.DAO;
#end##if ($manyToOneAssociations.size() > 0)
import com.quantium.mobile.framework.jdbc.JdbcQuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.jdbc.JdbcDao;
import com.quantium.mobile.framework.utils.StringUtil;
import com.quantium.mobile.framework.ToManyDAO;
#if ( $hasDateField)
import java.util.Date;
import com.quantium.mobile.framework.utils.DateUtil;
#end##if_hasDateField
import com.quantium.mobile.framework.Save;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.utils.CamelCaseUtils;
#if ($hasNullableAssociation)
import java.util.Collection;
import java.lang.ref.Reference;
#end##if ($hasNullableAssociation)

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

    @Override
    public boolean save($Target target) throws IOException {
        return save(target, Save.INSERT_IF_NULL_PK);
    }

    @Override
    public boolean save($Target target, int flags) throws IOException {
#if ($compoundPk)
        if (${nullPkCondition}) {
            return false;
        }
#end##if ($compoundPk)
        boolean insert;
#if (!$compoundPk)
        boolean insertIfNotExists = ( (flags&Save.INSERT_IF_NOT_EXISTS) != 0);
        insert = target.${getter[$primaryKey]}() == ${defaultId};
        if (insertIfNotExists)
#end
        {
            try {
                PreparedStatement stm = getStatement(COUNT_BY_PRIMARY_KEYS);
#foreach ($field in $primaryKeys)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
                stm.setLong(
                    ${foreach.count},
                    (target.get${association.Klass}() == null) ? 0 : target.get${association.Klass}().get${association.ReferenceKey.UpperCamel}());
#elseif (!$field.Klass.equals("Date"))
                stm.set${field.Klass}(${foreach.count}, target.${getter[$field]}());
#else
                #primary key ${field.Klass}
#end##if($field.Klass.equals(****))
#end##foreach
                ResultSet rs = stm.executeQuery();
                insert = rs.next() && rs.getLong(1) == 0L;
                rs.close();
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(StringUtil.getStackTrace(e));
            }
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
        if (insert) {
            long value;
            try{
                PreparedStatement stm;
                #if (!$compoundPk)if (insertIfNotExists) #end{
                    stm = getStatement(
                            "INSERT INTO ${table} (" +
#foreach ($field in $fields)
#if (!$primaryKeys.contains($field))
                                "${field.LowerAndUnderscores}" + "," +
#end##if (!$primaryKeys.contains($field))
#end##foreach ($field in $fields)
#foreach ($field in $primaryKeys)
                                "${field.LowerAndUnderscores}" +#if ($foreach.count < $primaryKeys.size()) "," +#end

#end##foreach ($field in $primaryKey)
                            ") VALUES (#foreach ($field in $fields)?#if ($foreach.count < $fields.size()),#else)#end#end");
                }
#if (!$compoundPk)
                else {
                    stm = getStatement(
                            "INSERT INTO ${table} (" +
#set ($argCount = $fields.size() - $primaryKeys.size())
#set ($argIndex = 0)
#foreach ($field in $fields)
#if (!$primaryKeys.contains($field))
#set ($argIndex = $argIndex + 1)
                                "${field.LowerAndUnderscores}" +#if ($argIndex < $argCount ) "," +#end

#end##if (!$primaryKeys.contains($field))
#end##foreach ($field in $fields)
                            ") VALUES (#foreach ($field in $fields)?#if ($foreach.count < $argCount),#else)#break#end#end");
                }
#end##if (!$compoundPk)
#set ($argIndex = 0)
#foreach ($field in $fields)
#if (!$primaryKeys.contains($field))
#set ($argIndex = $argIndex + 1)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
                stm.set${field.Klass}(
                        ${argIndex},
                        (target.get${association.Klass}() == null) ?
                            0 :
                            target.get${association.Klass}().get${association.ReferenceKey.UpperCamel}());
#elseif ($compoundPk || !$primaryKey.equals($field))##if (!$associationForField[$field])
#if ($field.Klass.equals("Date") )
                stm.setTimestamp(
                        ${argIndex},
                        (target.${getter[$field]}() == null) ?
                            null :
                            new java.sql.Timestamp(target.${getter[$field]}().getTime()));
#else##if_class_equals
                stm.set${field.Klass}(${argIndex}, target.${getter[$field]}());
#end##if_class_equals
#end##if ($associationForField[$field])
#end##if (!$primaryKeys.contains($field))
#end##foreach
                #if (!$compoundPk)if (insertIfNotExists) #end{
#foreach ($field in $primaryKeys)
#set ($argIndex = $argIndex + 1)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
                    stm.set${field.Klass}(
                            ${argIndex},
                            (target.get${association.Klass}() == null) ?
                                0 :
                                target.get${association.Klass}().get${association.ReferenceKey.UpperCamel}());
#else##if (!$associationForField[$field])
#if ($field.Klass.equals("Date") )
                    stm.setTimestamp(
                            ${argIndex},
                            (target.${getter[$field]}() == null) ?
                                null :
                                new java.sql.Timestamp(target.${getter[$field]}().getTime()));
#else##if_class_equals
                    stm.set${field.Klass}(${argIndex}, target.${getter[$field]}());
#end##if_class_equals
#end##if ($associationForField[$field])
#end##foreach
                }
                int qty = stm.executeUpdate();
                if (qty != 1) {
                    LogPadrao.e("Insert retornou %d", qty);
                    return false;
                }
                value = qty;
#if (!$compoundPk)
                if (!insertIfNotExists) {
                    ResultSet rs = stm.getGeneratedKeys();
                    value = (rs.next() ) ? rs.getLong(1) : -1;
                    if (value <= 0){
                        LogPadrao.e("id '%d' gerado", value);
                        return false;
                    }
                }
#end##if(!$compoundPk)
            } catch (java.sql.SQLException e){
                throw new IOException(StringUtil.getStackTrace(e));
            }
#if ($compoundPk)
            if (value > 0) {
                if (target instanceof ${EditableInterface}) {
#if ($toManyAssociations.size() > 0)
                    $EditableInterface editable = (${EditableInterface})target;
#end##if ($toManyAssociations.size() > 0)
#foreach ($association in $toManyAssociations)
#if ($association.ReferenceKey)
#set ($referenceKey = $association.ReferenceKey)
#elseif ($association.IsThisTableA)
#set ($referenceKey = $association.ReferenceA)
#else##if ($association.ReferenceKey)
#set ($referenceKey = $association.ReferenceB)
#end##if ($association.ReferenceKey)
                    if (editable.get${association.Pluralized}() == null) {
                        editable.set${association.Pluralized}(querySetFor${association.Pluralized}(editable.${getter[$referenceKey]}()));
                    }
#end##foreach ($association in $toManyAssociations)
                    factory.pushToCache(${Target}.class, pks, target);
                } else {
                    factory.removeFromCache(${Target}.class, pks);
                    LogPadrao.e(String.format("%s nao editavel salvo", target.getClass().getName()));
                }
                return true;
            } else {
                return false;
            }
#else##not_compoundPk
            if (value > 0){
                if (target instanceof ${EditableInterface}) {
                    $EditableInterface editable = (${EditableInterface})target;
                    editable.set${primaryKey.UpperCamel}(value);
#foreach ($association in $toManyAssociations)
#if ($association.ReferenceKey)
#set ($referenceKey = $association.ReferenceKey)
#elseif ($association.IsThisTableA)
#set ($referenceKey = $association.ReferenceA)
#else##if ($association.ReferenceKey)
#set ($referenceKey = $association.ReferenceB)
#end##if ($association.ReferenceKey)
                    if (editable.get${association.Pluralized}() == null) {
                        editable.set${association.Pluralized}(querySetFor${association.Pluralized}(editable.${getter[$referenceKey]}()));
                    }
#end##foreach ($association in $toManyAssociations)
                    pks = new Serializable[]{ value };
                    factory.pushToCache(${Target}.class, pks, target);
                } else {
                    factory.removeFromCache(${Target}.class, pks);
                    LogPadrao.e(String.format("%s nao editavel salvo", target.getClass().getName()));
                }
                return true;
            } else {
                return false;
            }
#end##if ($compoundPk)
        } else {
            try {
                PreparedStatement stm = getStatement(
                        "UPDATE ${table} SET " +
#set ($argCount = $fields.size() - $primaryKeys.size())
#set ($argIndex = 0)
#foreach ($field in $fields)
#if (!$primaryKeys.contains($field))
#set ($argIndex = $argIndex + 1)
                            "${field.LowerAndUnderscores}=?" +#if ($argIndex < $argCount) ", " +#end

#end##if (!$primaryKeys.contains($field))
#end##foreach ($field in $fields)
                        " WHERE ${queryByPrimaryKey}");
#set ($argIndex = 0)
#foreach ($field in $fields)
#if (!$primaryKeys.contains($field))
#set ($argIndex = $argIndex + 1)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
                stm.set${field.Klass}(
                        ${argIndex},
                        (target.get${association.Klass}() == null) ?
                            0 :
                            target.get${association.Klass}().get${association.ReferenceKey.UpperCamel}());
#elseif ($compoundPk || !$primaryKey.equals($field))##if (!$associationForField[$field])
#if ($field.Klass.equals("Date") )
                stm.setTimestamp(
                        ${argIndex},
                        (target.${getter[$field]}() == null) ?
                            null :
                            new java.sql.Timestamp(target.${getter[$field]}().getTime()));
#else##if_class_equals
                stm.set${field.Klass}(${argIndex}, target.${getter[$field]}());
#end##if_class_equals
#end##if ($associationForField[$field])
#end##if (!$primaryKeys.contains($field))
#end##foreach
#foreach ($field in $primaryKeys)
#set ($argIndex = $argIndex + 1)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
                stm.set${field.Klass}(
                        ${argIndex},
                        (target.get${association.Klass}() == null) ?
                            0 :
                            target.get${association.Klass}().get${association.ReferenceKey.UpperCamel}());
#else##if (!$associationForField[$field])
#if ($field.Klass.equals("Date") )
                stm.setTimestamp(
                        ${argIndex},
                        (target.${getter[$field]}() == null) ?
                            null :
                            new java.sql.Timestamp(target.${getter[$field]}().getTime()));
#else##if_class_equals
                 stm.set${field.Klass}(${argIndex}, target.${getter[$field]}());
#end##if_class_equals
#end##if ($associationForField[$field])
#end##foreach
                int value = stm.executeUpdate();
                if (value == 1) {
                    factory.pushToCache(${Target}.class, pks, target);
                    return true;
                } else {
                    return false;
                }
            } catch (java.sql.SQLException e) {
                LogPadrao.e(e);
                return false;
            }
        }
    }


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

#foreach ($association in $oneToManyAssociations)
    public QuerySet<${association.Klass}> querySetFor${association.Pluralized}(
        ${association.ReferenceKey.Type} ${association.ReferenceKey.LowerCamel}
    ) {
        return factory.getDaoFor(${association.Klass}.class).query(
            ${association.Klass}.${association.ForeignKey.UpperAndUnderscores}.eq(${association.ReferenceKey.LowerCamel}));
    }

#end##foreach ($association in $oneToManyAssociations)
#foreach ($association in $manyToManyAssociations)
    public QuerySet<${association.Klass}> querySetFor${association.Pluralized}(
#if ($association.IsThisTableA)
        ${association.ReferenceA.Type} ${association.ReferenceA.LowerCamel}
#else
        ${association.ReferenceB.Type} ${association.ReferenceB.LowerCamel}
#end
    ) {
        return factory.getDaoFor(${association.Klass}.class)
#if ($association.IsThisTableA)
            .query(
                (${association.Klass}.${association.ReferenceB.UpperAndUnderscores}.eq(${Target}._${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores}))
                .and( ${Target}._${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores}.eq(${association.ReferenceA.LowerCamel}) ));
#else
            .query(
                (${association.Klass}.${association.ReferenceA.UpperAndUnderscores}.eq(${Target}._${association.JoinTableUpper}_${association.KeyToA.UpperAndUnderscores}))
                .and( ${Target}._${association.JoinTableUpper}_${association.KeyToB.UpperAndUnderscores}.eq(${association.ReferenceB.LowerCamel}) ));
#end
    }

#end##foreach ($association in $oneToManyAssociations)

    @Override
    public  $Target cursorToObject(ResultSet cursor, boolean useCache){
#set ($primaryKeyIndex = 0)
#foreach ($field in $fields)
#set ($columnIndex = $foreach.count)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
        Long _${field.LowerCamel};
        try{
            _${field.LowerCamel} = cursor.getLong(${columnIndex});
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
        ${association.Klass} _${association.Klass} = null;
        if (!_${field.LowerCamel}.equals((long)${defaultId})) {
            Object cacheItem = factory.cacheLookup(
                ${association.Klass}.class,
                new Serializable[]{_${field.LowerCamel}});
            if (cacheItem == null) {
                LazyInvocationHandler<${association.Klass}> handler =
                    new LazyInvocationHandler<${association.Klass}>(
                        factory.getDaoFor(${association.Klass}.class).query(
                            ${association.Klass}.${association.ReferenceKey.UpperAndUnderscores}.eq(_${field.LowerCamel})),
                        _${field.LowerCamel},
                        "${getter[$field]}");
                _${association.Klass} = (${association.Klass})Proxy.newProxyInstance(
                    this.getClass().getClassLoader(),
                    new Class[]{ ${association.Klass}Editable.class },
                    handler);
            } else if (cacheItem instanceof ${association.Klass}) {
                _${association.Klass} = (${association.Klass})cacheItem;
            }
        }

#else##if ($!associationForField[$field])
        ${field.type} _${field.LowerCamel};
        try{
#if ($field.Klass.equals("Date") )
            Timestamp temp = cursor.getTimestamp(${columnIndex});
            _${field.LowerCamel} = (temp == null)?
                null :
                new Date(temp.getTime());
#else##if ($associationForField[$field])
            _${field.LowerCamel} = cursor.get${field.Klass}(${columnIndex});
#end##if_field.Klass.equals(*)
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
#end##if ($associationForField[$field])
#if ($field.PrimaryKey)
#set ($primaryKeyIndex = $primaryKeyIndex + 1)
#end##if ($field.PrimaryKey)
#if ($primaryKeyIndex.equals($primaryKeys.size()))
        Serializable pks [] = null;
        if (useCache) {
            pks = new Serializable[]{
#foreach ($key in $primaryKeys)
                 _${key.LowerCamel},
#end##foreach ($key in $primaryKeys)
            };
            Object cacheItem = factory.cacheLookup(${Target}.class, pks);
            if (cacheItem != null &&
                (cacheItem instanceof ${Target}))
            {
                return (${Target})cacheItem;
            }
        }
#set ($primaryKeyIndex = 0)
#end##if ($primaryKeyIndex.equals($primaryKeys.size()))
#end##foreach ($field in $fields)

#set ($argCount = $fields.size() + $toManyAssociations.size())
        ${KlassImpl} target = new ${KlassImpl}(
#foreach ($field in $fields)
#set ($columnIndex = $foreach.index)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
            _${association.Klass}#if ($foreach.count < $argCount),#end

#else##if (!$associationForField[$field])
            _${field.LowerCamel}#if ($foreach.count < $argCount),#end

#end##if ($associationForField[$field])
#end##foreach ($field in $fields)
#foreach ($association in $oneToManyAssociations)
#set ($argIndex = $fields.size() + $foreach.count)
            querySetFor${association.Pluralized}(_${association.ReferenceKey.LowerCamel})#if ($argIndex < $argCount),#end

#end##foreach ($association in $toManyAssociations)
#foreach ($association in $manyToManyAssociations)
#set ($argIndex = $fields.size() + $oneToManyAssociations.size() + $foreach.count)
#if ($association.IsThisTableA)
            querySetFor${association.Pluralized}(_${association.ReferenceA.LowerCamel})#if ($argIndex < $argCount),#end
#else
            querySetFor${association.Pluralized}(_${association.ReferenceB.LowerCamel})#if ($argIndex < $argCount),#end
#end

#end##foreach ($association in $manyToManyAssociations)
        );
        //target._daofactory = this.factory;

        if (useCache)
            factory.pushToCache(${Target}.class, pks, target);

        return target;
    }

#if ($manyToOneAssociations.size() >0 )
    @SuppressWarnings("unchecked")
#end##($manyToOneAssociations.size() >0 )
    @Override
    public $Target mapToObject(Map<String, Object> map)
        throws ClassCastException
    {
        CamelCaseUtils.AnyCamelMap<Object> mapAnyCamelCase =
            new CamelCaseUtils.AnyCamelMap<Object>();
        mapAnyCamelCase.putAll(map);
        Object temp;
#foreach ($field in $fields)
#if ($field.SerializationAlias)
#set ($alias = $field.SerializationAlias)
#else##if_not_alias
#set ($alias = $field.LowerCamel)
#end##end_if_alias
#if ($primaryKeys.contains($field))
#set ($fallback = $defaultId)
#elseif (${field.Klass} == "Long" || ${field.Klass} == "Double")
#set ($fallback = "0")
#elseif (${field.Klass} == "Boolean")
#set ($fallback = "false")
#else
#set ($fallback = "null")
#end##if_primary_key
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
#set ($submap = "submapFor${association.Klass}")
        Object ${submap} = mapAnyCamelCase.get("${association.Klass}");
        ${association.Klass} _${association.Klass};
        if (${submap} != null && ${submap} instanceof Map){
            _${association.Klass} = factory.getDaoFor(${association.Klass}.class)
                .mapToObject((Map<String,Object>)${submap});
        } else {
            temp = mapAnyCamelCase.get("${alias}");
            if (temp != null && temp instanceof String)
                temp = Long.parseLong((String)temp);
            long ${field.LowerCamel} = ((temp!= null)?((Number) temp).longValue(): ${defaultId});
            DAO<${association.Klass}> dao = this.factory.getDaoFor(${association.Klass}.class);
            _${association.Klass} = (${field.LowerCamel} != ${defaultId})?
                dao.query(${association.Klass}.${association.ReferenceKey.UpperAndUnderscores}.eq((Long)${field.LowerCamel})).first():
                null;
        }
#else##if (!$associationForField[$field])
        temp = mapAnyCamelCase.get("${alias}");
#if (${field.Klass} == "Long" || ${field.Klass} == "Double")
        if (temp != null && temp instanceof String)
            temp = ${field.Klass}.parse${field.Klass}((String)temp);
        ${field.Type} _${field.LowerCamel} = ((temp!= null)?((Number) temp).${field.Type}Value(): ${fallback});
#elseif (${field.Klass} == "Boolean")
        if (temp != null && temp instanceof String){
            if (temp.equals("0"))
                temp = false;
            else if (temp.equals("1"))
                temp = true;
            else
                temp = Boolean.parseBoolean((String)temp);
        }
        ${field.Type} _${field.LowerCamel} = ((temp!= null)?((Boolean) temp): ${fallback});
#elseif (${field.Klass} == "Date")
        if (temp != null && temp instanceof String)
            temp = DateUtil.stringToDate((String)temp);
        ${field.Type} _${field.LowerCamel} = ((temp!= null)? ((${field.Klass})temp): ${fallback});
#else##if_Klass_eq_***
        ${field.Type} _${field.LowerCamel} = ((temp!= null)? ((${field.Klass})temp): ${fallback});
#end##if_Klass_eq_***
#end##if (!$associationForField[$field])
#end##foreach

#set ($argCount = $fields.size() + $toManyAssociations.size())
        ${KlassImpl} target = new ${KlassImpl}(
#foreach ($field in $fields)
#set ($columnIndex = $foreach.index)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
            _${association.Klass}#if ($foreach.count < $argCount),#end

#else##if (!$associationForField[$field])
            _${field.LowerCamel}#if ($foreach.count < $argCount),#end

#end##if ($associationForField[$field])
#end##foreach ($field in $fields)
#foreach ($association in $oneToManyAssociations)
#set ($argIndex = $fields.size() + $foreach.count)
            querySetFor${association.Pluralized}(_${association.ReferenceKey.LowerCamel})#if ($argIndex < $argCount),#end

#end##foreach ($association in $toManyAssociations)
#foreach ($association in $manyToManyAssociations)
#set ($argIndex = $fields.size() + $oneToManyAssociations.size() + $foreach.count)
#if ($association.IsThisTableA)
            querySetFor${association.Pluralized}(_${association.ReferenceA.LowerCamel})#if ($argIndex < $argCount),#end
#else
            querySetFor${association.Pluralized}(_${association.ReferenceB.LowerCamel})#if ($argIndex < $argCount),#end
#end

#end##foreach ($association in $manyToManyAssociations)
        );
        //target._daofactory = this.factory;
        return target;
    }

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

    @Override
    public ToManyDAO with(${Target} obj){
#set ($hasMutableAssociations = $manyToManyAssociations.size() > 0)
#foreach ($association in $oneToManyAssociations)
#if (!$association.ForeignKey.PrimaryKey)
#set ($hasMutableAssociations = true)
#break
#end##if (!$association.KeyToA.PrimaryKey)
#end##foreach ($association in oneToManyAssociations)
#if (!$hasMutableAssociations)
         throw new UnsupportedOperationException();
#else##has_toManyAssociations
         return new ${Target}ToManyDAO(obj);
#end##has_toManyAssociations
    }

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

#if ( !($manyToManyAssociations.size() == 0) || !($oneToManyAssociations.size() == 0) )
    private class ${Target}ToManyDAO implements ToManyDAO {

        private $Target target;

        private ${Target}ToManyDAO(${Target} target){
            this.target = target;
        }

        @Override
        public boolean add(Object obj) throws IOException{
#if (!$hasMutableAssociations)
            throw new UnsupportedOperationException();
#else##if (!$hasMutableAssociations)
#set ($assocIndex = 0)
#foreach ($association in $oneToManyAssociations)
#if (!$association.ForeignKey.PrimaryKey)
           #if ($assocIndex != 0)} else#end if (obj instanceof ${association.Klass}){
                ${association.Klass} objCast = ((${association.Klass})obj);
                objCast.set${Target}(this.target);
                return factory.getDaoFor(${association.Klass}.class).save(objCast);
#set ($assocIndex = $assocIndex+1)
#end##if (!$association.ForeignKey.PrimaryKey)
#end##foreach_oneToMany
#foreach ($association in $manyToManyAssociations)
           #if ($assocIndex != 0)} else#end if (obj instanceof ${association.Klass}){
                ${association.Klass} objCast = ((${association.Klass})obj);
                return add${association.Klass}To${Target}(objCast, target);
#set ($assocIndex = $assocIndex+1)
#end##foreach_manyToMany
            } else {
                throw new IllegalArgumentException(obj.getClass().getName());
            }
#end##if (!$hasMutableAssociations)
        }

        @Override
        public boolean remove(Object obj) throws IOException {
#set ($assocIndex = 0)
#foreach ($association in $oneToManyAssociations)
           #if ($assocIndex != 0)} else#end if (obj instanceof ${association.Klass}){
                ${association.Klass} objCast = ((${association.Klass})obj);
#if ($association.Nullable)
                objCast.set${Target}(null);
                return factory.getDaoFor(${association.Klass}.class).save(objCast);
#else##if_association_nullabble
                return factory.getDaoFor(${association.Klass}.class).delete(objCast);
#end##if_association_nullabble
#set ($assocIndex = $assocIndex+1)
#end##foreach_oneToMany
#foreach ($association in $manyToManyAssociations)
           #if ($assocIndex != 0)} else#end if (obj instanceof ${association.Klass}){
                ${association.Klass} objCast = ((${association.Klass})obj);
                return remove${association.Klass}From${Target}(objCast, target);
#set ($assocIndex = $assocIndex+1)
#end##foreach_manyToMany
            } else {
                throw new IllegalArgumentException(obj.getClass().getName());
            }
        }
    }
#end##if_oneToMany_or_manyToMany
}

