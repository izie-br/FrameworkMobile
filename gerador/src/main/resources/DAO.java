package $package;

#set ($compoundPk = $primaryKeys.size() > 1)
#if ($implementation)
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.quantium.mobile.framework.Session;
import com.quantium.mobile.framework.DAOFactory;
import com.quantium.mobile.framework.query.Table;
#foreach ($field in $fields)
#if ( $field.Klass.equals("Date") )
import com.quantium.mobile.framework.utils.DateUtil;
#break
#end##if_Klass_==_Date
#end##foreach
#else##not_implementation
import com.quantium.mobile.framework.DAO;
#end
import android.database.SQLException;
#if (!$compoundPk || !$implementation)
import com.quantium.mobile.framework.Save;
#end
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;

#if ($implementation)
public class ${Klass} extends $BaseClass
#else
public abstract class ${Klass} implements DAO<${Target}>
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
#if (!$primaryKey.equals($field))
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
        SQLiteDatabase db = this.session.getDb();
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
#foreach ($relation in $oneToManyAssociations)
#if ($relation.Nullable)
           #if ($foreach.index ==0)ContentValues#end contentValues = new ContentValues();
            contentValues.putNull("${relation.ForeignKey.LowerAndUnderscores}");
            db.update(
                "${relation.Table}", contentValues,
                "${relation.ForeignKey.LowerAndUnderscores} = ?",
                new String[] {((${relation.ForeignKey.Klass}) ${target}.${relation.ReferenceKey.LowerCamel}).toString()});
#else
            ${relation.Klass}DAO daoFor${relation.Klass} = (${relation.Klass}DAO)factory.getDaoFor(${relation.Klass}.class);
            for (${relation.Klass} obj: ${target}.get${relation.Pluralized}().all()) {
                daoFor${relation.Klass}.delete(obj);
            }
#end
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
    @SuppressWarnings("unchecked")
    public <T extends $Target> T cursorToObject(Cursor cursor, T _prototype){
        T ${target} = (T)_prototype.clone();
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
#foreach ($association in $manyToManyAssociations)
    public#if (!$implementation) abstract#end boolean add${association.Klass}To${Target}(${association.Klass} obj, $Target ${target})#if ($implementation) {
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
        SQLiteDatabase db = this.session.getDb();
        long value = db.insertOrThrow("${association.JoinTable}", null, contentValues);
        return (value > 0);
    }#else;#end


    public#if (!$implementation) abstract#end boolean remove${association.Klass}From${Target}(${association.Klass} obj, $Target ${target})#if ($implementation) {
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
        SQLiteDatabase db = this.session.getDb();
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
    public final class QuerySetImpl<T extends ${Target} >
        extends QuerySet<T>
    {

        private Session session;
        private ${Klass} dao;
        private T mPrototype;

        protected QuerySetImpl(Session session, T _prototype) {
            this.session = session;
            this.dao = (${Klass})session.getDAOFactory().getDaoFor(
                _prototype.getClass());
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

        protected T cursorToObject(Cursor cursor) {
            return dao.cursorToObject(cursor, mPrototype);
        }

    }

#end
}

