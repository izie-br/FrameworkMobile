#set ($compoundPk = $primaryKeys.size() > 1)
package $package;

import java.io.IOException;
#if ($implementation)
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.SQLException;

#foreach ($association in $oneToManyAssociations)
#if ($association.Nullable)
#set ($hasNullableAssociation = true)
#else
#set ($hasNotNullableAssociation = true)
#end##if
#end##foreach
#if ($hasNotNullableAssociation)
import com.quantium.mobile.framework.DAO;
#end##if_hasNullableAssociation
import com.quantium.mobile.framework.query.SQLiteQuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.db.DAOSQLite;
import com.quantium.mobile.framework.utils.StringUtil;
import com.quantium.mobile.framework.ToManyDAO;
#foreach ($field in $fields)
#if ( $field.Klass.equals("Date") )
import com.quantium.mobile.framework.utils.DateUtil;
#break
#end##if_Klass_equals_Date
#end##foreach
#else##if_not_implementation
import com.quantium.mobile.framework.DAO;
#end
import com.quantium.mobile.framework.Save;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;

public#if (!$implementation) abstract#end class ${Klass}
#if ($implementation)
    implements DAOSQLite<${Target}>
#else
    implements DAO<${Target}>
#end
{

#if ($implementation)
    private SQLiteDAOFactory factory;

    public ${Klass}(SQLiteDAOFactory factory){
        this.factory = factory;
    }

#end
    @Override
    public boolean save($Target ${target}) throws IOException {
        return save(${target}, Save.INSERT_IF_NULL_PK);
    }

    @Override
    public boolean save($Target ${target}, int flags) throws IOException {
        ${target}._daofactory = this.factory;
        ContentValues contentValues = new ContentValues();
#foreach ($field in $fields)
#if ($compoundPk || !$primaryKey.equals($field))
#if ($field.Klass == "Date")
        contentValues.put("${field.LowerAndUnderscores}",
                          DateUtil.timestampToString(${target}.${field.LowerCamel}));
#elseif ($field.Klass == "Boolean")
        contentValues.put("${field.LowerAndUnderscores}", (${target}.${field.LowerCamel})?1:0 );
#else
        contentValues.put("${field.LowerAndUnderscores}", ${target}.${field.LowerCamel});
#end##if_class_==*
#end##if_primaryKey
#end##foreach
        SQLiteDatabase db = this.factory.getDb();
        boolean insert;
#if (!$compoundPk)
        boolean insertIfNotExists = ( (flags&Save.INSERT_IF_NOT_EXISTS) != 0);
        insert = ${target}.${primaryKey.LowerCamel} == ${defaultId};
#end
        #if (!$compoundPk)if (insertIfNotExists)#end{
            Cursor cursor = this.factory.getDb().rawQuery(
                "SELECT COUNT(*) FROM ${table} WHERE "+
                "#foreach ($key in $primaryKeys)#if ($foreach.index !=0) AND #end${key.LowerAndUnderscores} = ?#end",
                new String[]{ #foreach ($key in $primaryKeys)((${key.Klass})${target}.${key.LowerCamel}).toString(), #end});
            insert = cursor.moveToNext() && cursor.getLong(0) == 0L;
            cursor.close();
        }
        if (insert) {
#if (!$compoundPk)
            if (insertIfNotExists) {
                contentValues.put("${primaryKey.LowerAndUnderscores}", ${target}.${primaryKey.LowerCamel});
            }
#end
            long value;
            try{
                value = db.insertOrThrow("${table}", null, contentValues);
            } catch (SQLException e){
                throw new IOException(StringUtil.getStackTrace(e));
            }
#if ($compoundPk)
            return (value > 0);
#else
           if (value > 0){
               ${target}.${primaryKey.LowerCamel} = value;
               return true;
           } else {
               return false;
           }
#end
        } else {
            int value = db.update(
                "${table}", contentValues,
##
##Se for o primeiro escreve: campo = ?
##Ou entao escreve:      AND campo = ?
##
                "#foreach ($key in $primaryKeys)#if ($foreach.index != 0) AND #end${key.LowerAndUnderscores} = ?#end",
                new String[] { #foreach ($key in $primaryKeys)((${key.Klass})${target}.${key.LowerCamel}).toString(), #end});
            return (value > 0);
        }
    }


    public QuerySet<${Target}> query() {
        return query(null);
    }

    public#if (!$implementation) abstract#end QuerySet<${Target}> query(Q q)#if ($implementation) {
        QuerySet<${Target}> queryset =
            new QuerySetImpl<${Target}>(this.factory, new ${Target}());
        if (q == null) {
            return queryset;
        }
        return queryset.filter(q);
    }#else;#end


    public#if (!$implementation) abstract#end boolean delete(${Target} ${target}) throws IOException#if ($implementation) {
        if (#foreach ($key in $primaryKeys)#if ($foreach.index != 0) || #end${target}.${key.LowerCamel} == ${defaultId}#end) {
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
                new String[] {((${relation.ForeignKey.Klass}) ${target}.${relation.ReferenceKey.LowerCamel}).toString()});
#else
            DAO<${relation.Klass}> daoFor${relation.Klass} = (DAO<${relation.Klass}>)factory.getDaoFor(${relation.Klass}.class);
            for (${relation.Klass} obj: ${target}.get${relation.Pluralized}().all()) {
                daoFor${relation.Klass}.delete(obj);
            }
#end
#end
#foreach ($relation in $manyToManyRelation)
            db.delete("${relation.ThroughTable}", "${relation.ThroughReferenceKey.LowerAndUnderscores} = ?",
                      new String[] {((${relation.ReferenceKey.Klass}) ${target}.${relation.ReferenceKey.LowerCamel}).toString()});
#end
            int affected;
            try {
                affected = db.delete(
                    "${table}", "#foreach ($key in $primaryKeys)#if ($foreach.index != 0) AND #end${key.LowerAndUnderscores} = ?#end",
                    new String[] {#foreach ($key in $primaryKeys)#if ($foreach.index != 0),#end (($key.Klass)${target}.${key.LowerCamel}).toString()#end });
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
    }#else;#end


#if ($implementation)
    @Override
    public  $Target cursorToObject(Cursor cursor, ${Target} _prototype){
        ${Target} ${target} = _prototype.clone();
        ${target}._daofactory = this.factory;
#foreach ($field in $fields)
#if ($field.Klass.equals("Boolean") )
        ${target}.${field.LowerCamel} = (cursor.getShort(${foreach.index}) > 0);
#elseif ($field.Klass.equals("Date") )
        ${target}.${field.LowerCamel} = DateUtil.stringToDate(cursor.getString(${foreach.index}));
#elseif ($field.Klass.equals("Long") )
        ${target}.${field.LowerCamel} = cursor.getLong(${foreach.index});
#elseif ($field.Klass.equals("Double") )
        ${target}.${field.LowerCamel} = cursor.getDouble(${foreach.index});
#elseif ($field.Klass.equals("String") )
        ${target}.${field.LowerCamel} = cursor.getString(${foreach.index});
#end##if
#end##foreach
        return ${target};
    }

    @Override
    public String[] getColumns() {
        return new String[] {
            #foreach ($field in $fields)"${field.UpperAndUnderscores}",#end
        };
    }

#end##if_implementation
#foreach ($association in $manyToManyAssociations)
    public#if (!$implementation) abstract#end boolean add${association.Klass}To${Target}(${association.Klass} obj, $Target ${target}) throws IOException#if ($implementation) {
        ContentValues contentValues = new ContentValues();
#if (${association.IsThisTableA})
        if (${target}.${association.ReferenceA.LowerCamel} == ${defaultId}) {
            return false;
        }
        contentValues.put("${association.KeyToA.LowerAndUnderscores}", ${target}.${association.ReferenceA.LowerCamel});
        contentValues.put("${association.KeyToB.LowerAndUnderscores}", obj.${association.ReferenceB.LowerCamel});
#else
        if (${target}.${association.ReferenceB.LowerCamel} == ${defaultId}) {
            return false;
        }
        contentValues.put("${association.KeyToB.LowerAndUnderscores}", ${target}.${association.ReferenceB.LowerCamel});
        contentValues.put("${association.KeyToA.LowerAndUnderscores}", obj.${association.ReferenceA.LowerCamel});
#end
        SQLiteDatabase db = this.factory.getDb();
        long value;
        try{
            value = db.insertOrThrow("${association.JoinTable}", null, contentValues);
        } catch (SQLException e){
            throw new IOException(StringUtil.getStackTrace(e));
        }
        return (value > 0);
    }#else;#end


    public#if (!$implementation) abstract#end boolean remove${association.Klass}From${Target}(${association.Klass} obj, $Target ${target}) throws IOException#if ($implementation) {
#if (${association.IsThisTableA})
        if (${target}.${association.ReferenceA.LowerCamel} == ${defaultId}) {
            return false;
        }
        String whereSql = "${association.KeyToA.LowerAndUnderscores} = ? AND ${association.KeyToB.LowerAndUnderscores} = ?";
        String [] args = new String[]{
            ((${association.KeyToA.Klass})${target}.${association.ReferenceA.LowerCamel}).toString(),
            ((${association.KeyToB.Klass})obj.${association.ReferenceB.LowerCamel}).toString()
       };
#else
        if (${target}.${association.ReferenceB.LowerCamel} == ${defaultId}) {
            return false;
        }
        String whereSql = "${association.KeyToB.LowerAndUnderscores} = ? AND ${association.KeyToA.LowerAndUnderscores} = ?";
        String [] args = new String[]{
            ((${association.KeyToA.Klass})obj.${association.ReferenceA.LowerCamel}).toString(),
            ((${association.KeyToB.Klass})${target}.${association.ReferenceB.LowerCamel}).toString()
       };
#end
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
    }#else;#end

#end
#if ($implementation)
    public ToManyDAO with(${Target} obj){
#if ($manyToManyAssociations.size() == 0 && $oneToManyAssociations.size() == 0)
         throw new UnsupportedOperationException();
#else
         return new ${Target}ToManyDAO(obj);
#end
    }

    final class QuerySetImpl<T extends ${Target} >
        extends SQLiteQuerySet<T>
    {

        private SQLiteDAOFactory factory;
        private ${Klass} dao;
        private T mPrototype;

        protected QuerySetImpl(SQLiteDAOFactory factory, T _prototype) {
            this.factory = factory;
            this.dao = (${Klass})factory.getDaoFor(
                _prototype.getClass());
            this.mPrototype = _prototype;
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
                #foreach ($field in $fields)#if ($foreach.index != 0),#end ${Target}.${field.UpperAndUnderscores}#end
            };
        }

        @SuppressWarnings("unchecked")
        protected T cursorToObject(Cursor cursor) {
            return (T)dao.cursorToObject(cursor, mPrototype);
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
#set ($assocIndex = 0)
#foreach ($association in $oneToManyAssociations)
           #if ($assocIndex != 0)} else#end if (obj instanceof ${association.Klass}){
                ${association.Klass} objCast = ((${association.Klass})obj);
                objCast.set${Target}(this.target);
                return factory.getDaoFor(${association.Klass}.class).save(objCast);
#set ($assocIndex = $assocIndex+1)
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
#end
}

