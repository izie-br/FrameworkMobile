#set ($compoundPk = $primaryKeys.size() > 1)
#foreach ($association in $oneToManyAssociations)
#if ($association.Nullable)
#set ($hasNullableAssociation = true)
#else
#set ($hasNotNullableAssociation = true)
#end##if
#end##foreach
#foreach ($field in $fields)
#if ( $field.Klass.equals("Date") )
#set ($hasDateField = true)
#break
#end##if_Klass_equals_Date
#end##foreach
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
#set ($queryByPrimaryKey = $queryByPrimaryKey + "${field.LowerAndUnderscores} = ?")
#set ($fieldIsForeignKey = false)
#foreach ($association in $manyToOneAssociations)
#if ($association.ForeignKey.equals($field))
#set ($fieldIsForeignKey = true)
#set ($primaryKeysArgs = $primaryKeysArgs + "((${field.Klass})target._${association.Klass}.get${association.ReferenceKey.UpperCamel}()).toString(),")
#set ($nullPkCondition = $nullPkCondition + "target._${association.Klass} == null ||" +
                         "target._${association.Klass}.get${association.ReferenceKey.UpperCamel}() == ${defaultId}")
#end##if($association.ForeignKey.equals($field))
#end##($association in $manyToOneAssociations)
#if (!$fieldIsForeignKey)
#set ($primaryKeysArgs = $primaryKeysArgs + "((${field.Klass})target.${field.LowerCamel}).toString(),")
#set ($nullPkCondition = $nullPkCondition + "target.get${field.UpperCamel}() == ${defaultId}" )
#end##if (!$fieldIsForeignKey)
#end##foreach ($field in $primaryKeys)
#set ($primaryKeysArgs = ${primaryKeysArgs} + "}")
package $package;

import java.io.IOException;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.SQLException;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.LazyProxy;
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

public class ${Klass} implements DAOSQLite<${Target}> {

    private SQLiteDAOFactory factory;

    public ${Klass}(SQLiteDAOFactory factory){
        this.factory = factory;
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
        target._daofactory = this.factory;
        if (target instanceof LazyProxy)
            ((LazyProxy)target).load();
        ContentValues contentValues = new ContentValues();
#foreach ($field in $fields)
#set ($fieldIsForeignKey = false)
#foreach ($association in $manyToOneAssociations)
#if ($association.ForeignKey.equals($field))
#set ($fieldIsForeignKey = true)
        contentValues.put("${field.LowerAndUnderscores}",
                          (target._${association.Klass} == null) ? 0 : target._${association.Klass}.get${association.ReferenceKey.UpperCamel}());
#end##if($association.ForeignKey.equals($field))
#end##($association in $manyToOneAssociations)
#if (!$fieldIsForeignKey)
#if ($compoundPk || !$primaryKey.equals($field))
#if ($field.Klass.equals("Date") )
        contentValues.put("${field.LowerAndUnderscores}",
                          DateUtil.timestampToString(target.${field.LowerCamel}));
#elseif ($field.Klass.equals("Boolean") )
        contentValues.put("${field.LowerAndUnderscores}", (target.${field.LowerCamel})?1:0 );
#else##if_class_equals
        contentValues.put("${field.LowerAndUnderscores}", target.${field.LowerCamel});
#end##if_class_equals
#end##if ($compoundPk || !$primaryKey.equals($field))
#end##if (!$fieldIsForeignKey)
#end##foreach
        SQLiteDatabase db = this.factory.getDb();
        boolean insert;
        String queryByPrimaryKey = "${queryByPrimaryKey}";
        String primaryKeysArgs [] = ${primaryKeysArgs};
#if (!$compoundPk)
        boolean insertIfNotExists = ( (flags&Save.INSERT_IF_NOT_EXISTS) != 0);
        insert = target.${primaryKey.LowerCamel} == ${defaultId};
#end##not_compoundPk
        #if (!$compoundPk)if (insertIfNotExists)#end{
            Cursor cursor = this.factory.getDb().rawQuery(
                "SELECT COUNT(*) FROM ${table} WHERE "+ queryByPrimaryKey,
                primaryKeysArgs);
            insert = cursor.moveToNext() && cursor.getLong(0) == 0L;
            cursor.close();
        }
        if (insert) {
#if (!$compoundPk)
            if (insertIfNotExists) {
                contentValues.put("${primaryKey.LowerAndUnderscores}", target.${primaryKey.LowerCamel});
            }
#end##not_compoundPk
            long value;
            try{
                value = db.insertOrThrow("${table}", null, contentValues);
            } catch (SQLException e){
                throw new IOException(StringUtil.getStackTrace(e));
            }
#if ($compoundPk)
            return (value > 0);
#else##not_compoundPk
           if (value > 0){
               target.${primaryKey.LowerCamel} = value;
               return true;
           } else {
               return false;
           }
#end##not_compoundPk
        } else {
            int value = db.update(
                "${table}", contentValues, queryByPrimaryKey, primaryKeysArgs);
            return (value > 0);
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


    public boolean delete(${Target} target) throws IOException {
        if (${nullPkCondition}) {
            return false;
        }
        SQLiteDatabase db = this.factory.getDb();
        try {
            db.beginTransaction();
#if ($hasNullableAssociation)
            ContentValues contentValues;
#end##if_hasNullableAssociation
#foreach ($relation in $oneToManyAssociations)
#if ($relation.Nullable)
            contentValues = new ContentValues();
            contentValues.putNull("${relation.ForeignKey.LowerAndUnderscores}");
            db.update(
                "${relation.Table}", contentValues,
                "${relation.ForeignKey.LowerAndUnderscores} = ?",
                new String[] {((${relation.ForeignKey.Klass}) target.${relation.ReferenceKey.LowerCamel}).toString()});
#else##association_nullable
            DAO<${relation.Klass}> daoFor${relation.Klass} = (DAO<${relation.Klass}>)factory.getDaoFor(${relation.Klass}.class);
            for (${relation.Klass} obj: target.get${relation.Pluralized}().all()) {
                daoFor${relation.Klass}.delete(obj);
            }
#end##association_nullable
#end##foreach_oneToMany
#foreach ($relation in $manyToManyRelation)
            db.delete("${relation.ThroughTable}", "${relation.ThroughReferenceKey.LowerAndUnderscores} = ?",
                      new String[] {((${relation.ReferenceKey.Klass}) target.${relation.ReferenceKey.LowerCamel}).toString()});
#end##foreach_manyToMany
            int affected;
            try {
                affected = db.delete(
                    "${table}",
                    "${queryByPrimaryKey}",
                    ${primaryKeysArgs});
            } catch (SQLException e) {
                throw new IOException(StringUtil.getStackTrace(e));
            }
            if (affected == 0) {
                return false;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return true;
    }


    @Override
    public  $Target cursorToObject(Cursor cursor){
#foreach ($field in $fields)
#set ($columnIndex = $foreach.index)
#set ($fieldIsForeignKey = false)
#foreach ($association in $manyToOneAssociations)
#if ($association.ForeignKey.equals($field))
#set ($fieldIsForeignKey = true)
        Long __${field.LowerCamel} = cursor.getLong(${columnIndex});
        ${association.Klass}.Proxy _${association.Klass} = null;
        if (!__${field.LowerCamel}.equals((long)${defaultId})) {
            _${association.Klass} = new ${association.Klass}.Proxy();
            _${association.Klass}.${association.ReferenceKey.LowerCamel} = __${field.LowerCamel};
            _${association.Klass}._daofactory = this.factory;
        }

#end##($association.ForeignKey.equals($field))
#end##foreach($association in $manyToOneAssociations)
#if (!$fieldIsForeignKey)
#if ($field.Klass.equals("Boolean") )
        ${field.Type} ${field.LowerCamel} = (cursor.getShort(${columnIndex}) > 0);
#elseif ($field.Klass.equals("Date") )
        ${field.Type} ${field.LowerCamel} = DateUtil.stringToDate(cursor.getString(${columnIndex}));
#elseif ($field.Klass.equals("Long") )
        ${field.Type} ${field.LowerCamel} = cursor.getLong(${columnIndex});
#elseif ($field.Klass.equals("Double") )
        ${field.Type} ${field.LowerCamel} = cursor.getDouble(${columnIndex});
#elseif ($field.Klass.equals("String") )
        ${field.Type} ${field.LowerCamel} = cursor.getString(${columnIndex});
#end##if_field.Klass.equals(*)
#end##if (!$fieldIsForeignKey)
#end##foreach ($field in $fields)

        ${Target} target = new ${Target}(
#foreach ($field in $fields)
#set ($columnIndex = $foreach.index)
#set ($fieldIsForeignKey = false)
#foreach ($association in $manyToOneAssociations)
#if ($association.ForeignKey.equals($field))
#set ($fieldIsForeignKey = true)
            _${association.Klass}#if ($foreach.count < $fields.size()),#else);#end

#end##($association.ForeignKey.equals($field))
#end##foreach($association in $manyToOneAssociations)
#if (!$fieldIsForeignKey)
            ${field.LowerCamel}#if ($foreach.count < $fields.size()),#else);#end

#end##if (!$fieldIsForeignKey)
#end##foreach ($field in $fields)
        target._daofactory = this.factory;
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
#set ($fieldIsForeignKey = false)
#foreach ($association in $manyToOneAssociations)
#if ($association.ForeignKey.equals($field))
#set ($fieldIsForeignKey = true)
#set ($submap = "submapFor${association.Klass}")
        Object ${submap} = mapAnyCamelCase.get("${association.Klass}");
        ${association.Klass} _${association.Klass};
        if (${submap} != null && ${submap} instanceof Map){
            _${association.Klass} = factory.getDaoFor(${association.Klass}.class)
                .mapToObject((Map<String,Object>)${submap});
        } else {
            temp = mapAnyCamelCase.get("${alias}");
            long ${field.LowerCamel} = ((temp!= null)?((Number) temp).longValue(): ${defaultId});
            DAO<${association.Klass}> dao = this.factory.getDaoFor(${association.Klass}.class);
            _${association.Klass} = (${field.LowerCamel} != ${defaultId})?
                dao.query(${association.Klass}.${association.ReferenceKey.UpperAndUnderscores}.eq((Long)${field.LowerCamel})).first():
                null;
        }
#end##if($association.ForeignKey.equals($field))
#end##($association in $manyToOneAssociations)
#if (!$fieldIsForeignKey)
        temp = mapAnyCamelCase.get("${alias}");
#if (${field.Klass} == "Long" || ${field.Klass} == "Double")
        ${field.Type} _${field.LowerCamel} = ((temp!= null)?((Number) temp).${field.Type}Value(): ${fallback});
#elseif (${field.Klass} == "Boolean")
        ${field.Type} _${field.LowerCamel} = ((temp!= null)?((Boolean) temp): ${fallback});
#else##if_Klass_eq_***
        ${field.Type} _${field.LowerCamel} = ((temp!= null)? ((${field.Klass})temp): ${fallback});
#end##if_Klass_eq_***
#end##if (!$fieldIsForeignKey)
#end##foreach

        ${Target} target = new ${Target}(
#foreach ($field in $fields)
#set ($columnIndex = $foreach.index)
#set ($fieldIsForeignKey = false)
#foreach ($association in $manyToOneAssociations)
#if ($association.ForeignKey.equals($field))
#set ($fieldIsForeignKey = true)
            _${association.Klass}#if ($foreach.count < $fields.size()),#else);#end

#end##($association.ForeignKey.equals($field))
#end##foreach($association in $manyToOneAssociations)
#if (!$fieldIsForeignKey)
            _${field.LowerCamel}#if ($foreach.count < $fields.size()),#else);#end

#end##if (!$fieldIsForeignKey)
#end##foreach ($field in $fields)
        target._daofactory = this.factory;
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
        if (obj instanceof LazyProxy)
            ((LazyProxy)obj).load();
        if (target instanceof LazyProxy)
            ((LazyProxy)target).load();
        ContentValues contentValues = new ContentValues();
#if (${association.IsThisTableA})
        if (target.${association.ReferenceA.LowerCamel} == ${defaultId}) {
            return false;
        }
        contentValues.put("${association.KeyToA.LowerAndUnderscores}", target.${association.ReferenceA.LowerCamel});
        contentValues.put("${association.KeyToB.LowerAndUnderscores}", obj.${association.ReferenceB.LowerCamel});
#else##(${association.IsThisTableA)
        if (target.${association.ReferenceB.LowerCamel} == ${defaultId}) {
            return false;
        }
        contentValues.put("${association.KeyToB.LowerAndUnderscores}", target.${association.ReferenceB.LowerCamel});
        contentValues.put("${association.KeyToA.LowerAndUnderscores}", obj.${association.ReferenceA.LowerCamel});
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
        if (obj instanceof LazyProxy)
            ((LazyProxy)obj).load();
        if (target instanceof LazyProxy)
            ((LazyProxy)target).load();
#if (${association.IsThisTableA})
        if (target.${association.ReferenceA.LowerCamel} == ${defaultId}) {
            return false;
        }
        String whereSql = "${association.KeyToA.LowerAndUnderscores} = ? AND ${association.KeyToB.LowerAndUnderscores} = ?";
        String [] args = new String[]{
            ((${association.KeyToA.Klass})target.${association.ReferenceA.LowerCamel}).toString(),
            ((${association.KeyToB.Klass})obj.${association.ReferenceB.LowerCamel}).toString()
       };
#else##(${association.IsThisTableA})
        if (target.${association.ReferenceB.LowerCamel} == ${defaultId}) {
            return false;
        }
        String whereSql = "${association.KeyToB.LowerAndUnderscores} = ? AND ${association.KeyToA.LowerAndUnderscores} = ?";
        String [] args = new String[]{
            ((${association.KeyToA.Klass})obj.${association.ReferenceA.LowerCamel}).toString(),
            ((${association.KeyToB.Klass})target.${association.ReferenceB.LowerCamel}).toString()
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

    final class QuerySetImpl
        extends SQLiteQuerySet<${Target}>
    {

        private SQLiteDAOFactory factory;
        private ${Klass} dao;

        protected QuerySetImpl(SQLiteDAOFactory factory) {
            this.factory = factory;
            this.dao = (${Klass})factory.getDaoFor(${Target}.class);
        }

        @Override
        protected SQLiteDatabase getDb() {
            return factory.getDb();
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

        protected ${Target} cursorToObject(Cursor cursor) {
            return dao.cursorToObject(cursor);
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

