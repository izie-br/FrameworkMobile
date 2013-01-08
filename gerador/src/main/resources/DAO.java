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
##
##
#parse("DAO.java.d/androidCursorToObject.java")

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
#**##parse("DAO.java.d/androidManyToManyHandlers.java")
#end
##
#parse("DAO.java.d/toManyDAO.java")

#parse("DAO.java.d/androidQuery.java")

}

