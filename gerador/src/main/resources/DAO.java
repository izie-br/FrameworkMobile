##
## String de busca e arrays de argumentos
##
#set ($primaryKeysArgs = "new String[]{")
#foreach ($field in $primaryKeys)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
#set ($primaryKeysArgs = $primaryKeysArgs +
                         "((${field.Klass})target.get${association.Klass}()" +
                         ".get${association.ReferenceKey.UpperCamel}())" +
                         ".toString(),")
#else##if (!$associationForField[$field])
#set ($primaryKeysArgs = $primaryKeysArgs +
                         "((${field.Klass})target.${getter[$field]}())" +
                         ".toString(),")
#end##if ($associationForField[$field])
#end##foreach ($field in $primaryKeys)
#set ($primaryKeysArgs = $primaryKeysArgs + "}")
package $package;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.SQLException;
import com.quantium.mobile.framework.logging.LogPadrao;
#if ($manyToOneAssociations.size() > 0)
import java.lang.reflect.Proxy;
import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.LazyInvocationHandler;
#elseif ($hasNotNullableAssociation)
import com.quantium.mobile.framework.DAO;
#end##if ($manyToOneAssociations.size() > 0)
import com.quantium.mobile.framework.query.SQLiteQuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.db.DAOSQLite;
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

public class ${Klass} implements DAOSQLite<${Target}> {

    private SQLiteDAOFactory factory;

    public ${Klass}(SQLiteDAOFactory factory){
        this.factory = factory;
    }

#parse("DAO.java.d/androidSave.java")

    public QuerySet<${Target}> query() {
        return query(null);
    }

    public QuerySet<${Target}> query(Q q) {
        QuerySet<${Target}> queryset =
            new QuerySetImpl();
        if (q == null) {
            return queryset;
        }
        return queryset.filter(q);
    }

#foreach ($association in $oneToManyAssociations)
#if ($association.Nullable)
    private static class ${association.Klass}NullFkThread implements Runnable {

        ${Target} target;
        SQLiteDAOFactory factory;

        private ${association.Klass}NullFkThread(SQLiteDAOFactory factory, ${Target} target) {
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

#parse("DAO.java.d/androidDelete.java")

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
    public  $Target cursorToObject(Cursor cursor, boolean useCache){
#set ($primaryKeyIndex = 0)
#foreach ($field in $fields)
#set ($columnIndex = $foreach.index)
#if ($associationForField[$field])
#set ($association = $associationForField[$field])
        Long _${field.LowerCamel} = cursor.getLong(${columnIndex});
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
#if ($field.Klass.equals("Boolean") )
        ${field.Type} _${field.LowerCamel} = (cursor.getShort(${columnIndex}) > 0);
#elseif ($field.Klass.equals("Date") )
        ${field.Type} _${field.LowerCamel} = DateUtil.stringToDate(cursor.getString(${columnIndex}));
#elseif ($field.Klass.equals("Long") )
        ${field.Type} _${field.LowerCamel} = cursor.getLong(${columnIndex});
#elseif ($field.Klass.equals("Double") )
        ${field.Type} _${field.LowerCamel} = cursor.getDouble(${columnIndex});
#elseif ($field.Klass.equals("String") )
        ${field.Type} _${field.LowerCamel} = cursor.getString(${columnIndex});
#end##if_field.Klass.equals(*)
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

        ${KlassImpl} target = new ${KlassImpl}(${constructorArgs});

        if (useCache)
            factory.pushToCache(${Target}.class, pks, target);

        return target;
    }

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
        ContentValues contentValues = new ContentValues();
#if (${association.IsThisTableA})
        if (target.${getter[$association.ReferenceA]}() == ${defaultId}) {
            return false;
        }
        contentValues.put("${association.KeyToA.LowerAndUnderscores}", target.${getter[$association.ReferenceA]}());
        contentValues.put("${association.KeyToB.LowerAndUnderscores}", obj.${getter[$association.ReferenceB]}());
#else##(${association.IsThisTableA)
        if (target.${getter[$association.ReferenceB]}() == ${defaultId}) {
            return false;
        }
        contentValues.put("${association.KeyToB.LowerAndUnderscores}", target.${getter[$association.ReferenceB]}());
        contentValues.put("${association.KeyToA.LowerAndUnderscores}", obj.${getter[$association.ReferenceA]}());
#end##(${association.IsThisTableA})
        SQLiteDatabase db = this.factory.getDb();
        long value;
        try{
            value = db.insertOrThrow("${association.JoinTable}", null, contentValues);
        } catch (SQLException e){
            throw new IOException(StringUtil.getStackTrace(e));
        }
        return (value > 0);
    }


    public boolean remove${association.Klass}From${Target}(${association.Klass} obj, $Target target) throws IOException {
#if (${association.IsThisTableA})
        if (target.${getter[$association.ReferenceA]}() == ${defaultId}) {
            return false;
        }
        String whereSql = "${association.KeyToA.LowerAndUnderscores} = ? AND ${association.KeyToB.LowerAndUnderscores} = ?";
        String [] args = new String[]{
            ((${association.KeyToA.Klass})target.${getter[$association.ReferenceA]}()).toString(),
            ((${association.KeyToB.Klass})obj.${getter[$association.ReferenceB]}()).toString()
       };
#else##(${association.IsThisTableA})
        if (target.${getter[$association.ReferenceB]}() == ${defaultId}) {
            return false;
        }
        String whereSql = "${association.KeyToB.LowerAndUnderscores} = ? AND ${association.KeyToA.LowerAndUnderscores} = ?";
        String [] args = new String[]{
            ((${association.KeyToA.Klass})obj.${getter[$association.ReferenceA]}()).toString(),
            ((${association.KeyToB.Klass})target.${getter[$association.ReferenceB]}()).toString()
       };
#end##(${association.IsThisTableA})
        SQLiteDatabase db = this.factory.getDb();
        Cursor cursor = db.query(
            "${association.JoinTable}", (new String[]{"rowid"}),
            whereSql, args, null, null, null, "1");
        if (!cursor.moveToNext()) {
            return false;
        }
        long rowid = cursor.getLong(0);
        cursor.close();
        if (rowid<= 0) {
            return false;
        }
        long affected = db.delete("${association.JoinTable}", "rowid=?", new String[] {((Long) rowid).toString()});
        return (affected == 1);
    }
#end##foreach_manyToManyAssociation

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

    final class QuerySetImpl extends SQLiteQuerySet<${Target}> {

        @Override
        protected SQLiteDatabase getDb() {
            return ${Klass}.this.factory.getDb();
        }

        @Override
        public Table getTable() {
            return ${Target}._TABLE;
        }

        @Override
        protected Table.Column<?> [] getColunas() {
            final Table.Column<?>[] columns = {
#foreach ($field in $fields)
                ${Target}.${field.UpperAndUnderscores},
#end
            };
            return columns;
        }

        protected ${Target} cursorToObject(Cursor cursor) {
            return ${Klass}.this.cursorToObject(cursor, true);
        }

    }

#if ( !($manyToManyAssociations.size() == 0) || !($oneToManyAssociations.size() == 0) )
#parse("DAO.java.d/toManyDAO.java")
#end
}

