package $package;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.quantium.mobile.framework.Save;
import com.quantium.mobile.framework.Session;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.utils.DateUtil;

public class $Class extends $BaseClass {

    private Session session;
    private DAOFactory factory;

    public ${Class}(Session session){
        this.session = session;
    }

    public boolean save($Target ${target}{
        return save(${target}, Save.INSERT_IF_NULL_PK);
    }

    public boolean save($Target ${target}, int flags)
        throws SQLException
    {
        ${target}._session = this.session;
        ContentValues contentValues = new ContentValues();
#foreach ($field in $fields)
        contentValues.put("${field.LowerAndUnderscores}", ${target}.${field.LowerCamel});
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
            Cursor cursor = getDb().rawQuery(
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
                new String[] {#foreach ($key in $primaryKeys)#if ($foreach.index != 0),#end ((${key.Klass})${key.LowerCamel}).toString()#end});
            return (value > 0);
        }
    }

    public QuerySet<${Target}> query() {
        return query(null);
    }

    public QuerySet<${Target}> query(Q q) {
        QuerySet<${Target}> queryset = new ${Target}.QuerySetImpl<${Target}>(this, new ${Target}());
        if (q == null) {
            return queryset;
        }
        return queryset.filter(q);
    }


    public boolean delete(${Target} ${target}){
        if (#foreach ($key in $primaryKeys)#if ($foreach.index != 0) || #end(${target}.${key.LowerCamel} == ${defaultId}#end) {
            return false;
        }
        SQLiteDatabase db = session.getDb();
        try {
            db.beginTransaction();
#foreach ($relation in $nullableRelation)
           #if ($foreach.index ==0)ContentValues#end contentValues = new ContentValues();
            contentValues.putNull(${relation.ForeignKey});
            db.update(
                "${relation.Table}", contentValues,
                "${relation.ForeignKey.LowerAndUnderscores} = ?",
                new String[] {((${relation.ForeignKey.Klass}) ${target}.${relation.ReferenceKey.LowerCamel}).toString()});
#end
#foreach ($relation in $nonNullRelations)
            ${relation.Klass}DAO daoFor${relation.Klass} = factory.getDaoFor(${relation.Klass}.class);
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
    }

#foreach ($relation in $manyToManyRelation)
    public boolean add${relation.Klass}To${Target}(${relation.Klass} obj, $Target ${target}) {
        if (${target}.${primaryKey.LowerCamel} == ${defaultId}) {
            return false;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("${relation.ThroughReferenceKey.LowerAndUnderscores}", ${target}.${primaryKey.LowerCamel});
        contentValues.put("${relation.ThroughForeignKey.LowerAndUnderscores}", obj.${relation.ForeignKey.LowerCamel});
        SQLiteDatabase db = session.getDb();
        long value = db.insertOrThrow("${relation.ThroughTable}, null, contentValues);
        return (value > 0);
    }

    public booelan remove${relation.Klass}From${Target}(${relation.Klass} obj, $Target ${target}) {

        if (${target}.${primaryKey.LowerCamel} == ${defaultId}) {
            return false;
        }
        SQLiteDatabase db = session.getDb();
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
    }

#end
}

