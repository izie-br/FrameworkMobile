package $package;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.quantium.mobile.framework.Save;
import com.quantium.mobile.framework.Session;
import com.quantium.mobile.framework.DAOFactory;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.utils.DateUtil;


#if ($implementation)
public class ${Klass} extends $BaseClass
#else
public abstract class ${Klass}
#end
{

#if ($implementation)
    private Session session;
    private DAOFactory factory;

    public ${Klass}(Session session, DAOFactory factory){
        this.session = session;
        this.factory = factory;
    }

#end
#if (!$implementation)
    public boolean save($Target ${target}) throws SQLException {
        return save(${target}, Save.INSERT_IF_NULL_PK);
    }

#end
    public#if (!$implementation) abstract#end boolean save($Target ${target}, int flags) throws SQLException#if ($implementation) {
        ${target}._daofactory = this.factory;
        ContentValues contentValues = new ContentValues();
#foreach ($field in $fields)
#if ($field.Klass == "Date")
        contentValues.put("${field.LowerAndUnderscores}",
                          DateUtil.timestampToString(${target}.${field.LowerCamel}));
#elseif ($field.Klass == "Boolean")
        contentValues.put("${field.LowerAndUnderscores}", (${target}.${field.LowerCamel})?1:0 );
#else
        contentValues.put("${field.LowerAndUnderscores}", ${target}.${field.LowerCamel});
#end
#end
        SQLiteDatabase db = this.session.getDb();
#set ($compoundPk = $primaryKeys.size() > 1)
#if ($compoundPk)
        $Target existingObj = this.query(
#foreach ($key in $primaryKeys)
#set ($firstkey = $foreach.index ==0 )
##
##Se for o primeiro escreve: Classe.CAMPO.eq(bean.campo)
##Ou entao escreve:    .and( Classe.CAMPO.eq(bean.campo) )
##
#if (!$firstkey).and( #end${Target}.${key.UpperAndUnderscores}.eq(${target}.${key.LowerCamel})#if (!$firstkey) )#end
#end
        ).first();
        if (existingObj == null) {
#else
        boolean insertIfNotExists = ( (flags&Save.INSERT_IF_NOT_EXISTS) != 0);
        boolean insert = ${target}.${primaryKey.LowerCamel} == ${defaultId};
        if (insertIfNotExists){
            Cursor cursor = this.session.getDb().rawQuery(
                "SELECT COUNT(*) FROM ${table} WHERE ${primaryKey.LowerAndUnderscores} = ?",
                new String[]{ ((${primaryKey.Klass})${target}.${primaryKey.LowerCamel}).toString()});
                insert = cursor.moveToNext() && cursor.getLong(0) == 0L;
        }
        if (insert) {
            if (insertIfNotExists) {
                contentValues.put("${primaryKey.LowerAndUnderscores}", ${target}.${primaryKey.LowerCamel});
            }
#end
            long value = db.insertOrThrow("${table}", null, contentValues);
            return (value > 0);
        } else {
            int value = db.update(
                "${table}", contentValues,
##
##Se for o primeiro escreve: campo = ?
##Ou entao escreve:      AND campo = ?
##
                "#foreach ($key in $primaryKeys)#if ($foreach.index != 0) AND #end${key.LowerAndUnderscores} = ?#end",
                new String[] {#foreach ($key in $primaryKeys)#if ($foreach.index != 0),#end ((${key.Klass})${target}.${key.LowerCamel}).toString()#end});
            return (value > 0);
        }
    }#else;#end


#if (!$implementation)
    public QuerySet<${Target}> query() {
        return query(null);
    }

#end
    public#if (!$implementation) abstract#end QuerySet<${Target}> query(Q q)#if ($implementation) {
        QuerySet<${Target}> queryset =
            new QuerySetImpl<${Target}>(this.session, new ${Target}());
        if (q == null) {
            return queryset;
        }
        return queryset.filter(q);
    }#else;#end


    public#if (!$implementation) abstract#end boolean delete(${Target} ${target})#if ($implementation){
        if (#foreach ($key in $primaryKeys)#if ($foreach.index != 0) || #end${target}.${key.LowerCamel} == ${defaultId}#end) {
            return false;
        }
        SQLiteDatabase db = this.session.getDb();
        try {
            db.beginTransaction();
#foreach ($relation in $nullableAssociations)
           #if ($foreach.index ==0)ContentValues#end contentValues = new ContentValues();
            contentValues.putNull("${relation.ForeignKey.LowerAndUnderscores}");
            db.update(
                "${relation.Table}", contentValues,
                "${relation.ForeignKey.LowerAndUnderscores} = ?",
                new String[] {((${relation.ForeignKey.Klass}) ${target}.${relation.ReferenceKey.LowerCamel}).toString()});
#end
#foreach ($relation in $nonNullableAssociations)
            ${relation.Klass}DAO daoFor${relation.Klass} = (${relation.Klass}DAO)factory.getDaoFor(${relation.Klass}.class);
            for (${relation.Klass} obj: ${target}.get${relation.Pluralized}().all()) {
                daoFor${relation.Klass}.delete(obj);
            }
#end
#foreach ($relation in $manyToManyRelation)
            db.delete("${relation.ThroughTable}", "${relation.ThroughReferenceKey.LowerAndUnderscores} = ?",
                      new String[] {((${relation.ReferenceKey.Klass}) ${target}.${relation.ReferenceKey.LowerCamel}).toString()});
#end
            int affected = db.delete(
                "${table}", "#foreach ($key in $primaryKeys)#if ($foreach.index != 0) AND #end${key.LowerAndUnderscores} = ?#end",
                new String[] {#foreach ($key in $primaryKeys)#if ($foreach.index != 0),#end (($key.Klass)${target}.${key.LowerCamel}).toString()#end });
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
    public $Target cursorToObject(Cursor cursor, $Target _prototype){
        $Target ${target} = _prototype.clone();
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

#end##if_implementation
#foreach ($relation in $manyToManyRelation)
    public#if (!$implementation) abstract#end boolean add${relation.Klass}To${Target}(${relation.Klass} obj, $Target ${target})#if ($implementation) {
        if (${target}.${primaryKey.LowerCamel} == ${defaultId}) {
            return false;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("${relation.ThroughReferenceKey.LowerAndUnderscores}", ${target}.${primaryKey.LowerCamel});
        contentValues.put("${relation.ThroughForeignKey.LowerAndUnderscores}", obj.${relation.ForeignKey.LowerCamel});
        SQLiteDatabase db = this.session.getDb();
        long value = db.insertOrThrow("${relation.ThroughTable}, null, contentValues);
        return (value > 0);
    }#else;#end


    public#if (!$implementation) abstract#end booelan remove${relation.Klass}From${Target}(${relation.Klass} obj, $Target ${target})#if ($implementation) {

        if (${target}.${primaryKey.LowerCamel} == ${defaultId}) {
            return false;
        }
        SQLiteDatabase db = this.session.getDb();
        Cursor cursor = db.query(
            "${relation.ThroughTable}", (new String[]{"rowid"}),
            "${relation.ThroughReferenceKey.LowerAndUnderscores} = ? AND ${relation.ThroughForeignKey.LowerAndUnderscores} = ?",
            new String[]{ ((${primaryKey.Klass})${target}.${primaryKey.LowerCamel}).toString(), ((${relation.ForeignKey.Klass})${relation.ForeignKey.LowerCamel}).toString()},
            null, null, null, "1");
        if (!cursor.moveToNext()) {
            return false;
        }
        long rowid = cursor.getLong(0);
        cursor.close();
        if (rowid<= 0) {
            return false;
        }
        long affected = db.delete("${relation.ThroughTable}", "rowid=?", new String[] {((Long) rowid).toString()});
        return (affected == 1);
    }#else;#end


#end
#if ($implementation)
    public final class QuerySetImpl<T extends ${Target} >
        extends QuerySet<T>
    {

        private Session session;
        private Object dao;
        private T mPrototype;

        protected QuerySetImpl(Session session, T _prototype) {
            this.session = session;
            this.dao = session.getDAOFactory().getDaoFor(_prototype.getClass());
            this.mPrototype = _prototype;
        }

        @Override
        protected SQLiteDatabase getDb() {
            return session.getDb();
        }

        @Override
        protected Table getTabela() {
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
            return (T)((${Klass})dao).cursorToObject(cursor, mPrototype);
        }

    }

#end
}

