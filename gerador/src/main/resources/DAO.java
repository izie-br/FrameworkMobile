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

public class $Class extends DAO<$Target> {

    private Session session;

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
        boolean insertIfNotExists = ((flags&Save.INSERT_IF_NOT_EXISTS)> 0);
        boolean insert = ${target}.${primaryKey.LowerCamel} == ${defaultId};
        if (insertIfNotExists){
            Cursor cursor = getDb().rawQuery(
                "SELECT COUNT(*) FROM ${table} WHERE ${primaryKey.LowerAndUnderscores} = ?",
                new String[]{ ((${primaryKey.Klass})${target}.${primaryKey.LowerCamel}).toString()});
            insert = cursor.getLong(0) == 0L;
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
                "#foreach ($key in $primaryKeys)#if ($foreach.index != 0) AND #end${key.LowerAndUnderscores} = ?#end",
                new String[] {#foreach ($key in $primaryKeys)#if ($foreach.index != 0),#end ((${key.Klass})${key.LowerCamel}).toString()#end});
            return (value > 0);
        }
    }
}

