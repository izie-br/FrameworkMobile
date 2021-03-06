package $package;
/*
 * Copyright (c) 2014 Izie.
 *
 */
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.SQLException;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.PrimaryKeyUpdater;
import com.quantium.mobile.framework.LazyInvocationHandler;
import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.query.SQLiteQuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.db.DAOSQLite;
import com.quantium.mobile.framework.utils.StringUtil;
import com.quantium.mobile.framework.ToManyDAO;
import com.quantium.mobile.framework.Save;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.framework.utils.SQLiteUtils;
import com.quantium.mobile.framework.utils.ValueParser;
import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.framework.validation.ValidationError;

${Imports}

@SuppressWarnings("unused")
public class ${Klass}
       implements DAOSQLite<${Target}>,
                  PrimaryKeyUpdater<${Target}> {

    @SuppressWarnings("unchecked")
    public static final List<Table.Column<?>> COLUMNS =
            Collections.unmodifiableList(Arrays.asList(
#foreach ($field in $fields)
                    (Table.Column<?>)${Target}.${field.UpperAndUnderscores}#if($foreach.count != $fields.size()),#end

#end
            ));

    private ${DaoFactory} factory;

    public ${Klass}(${DaoFactory} factory){
        this.factory = factory;
    }

#parse("DAO.java.d/getById.java")

#parse("DAO.java.d/updateCache.java")

#parse("DAO.java.d/androidSave.java")

#parse("DAO.java.d/updatePrimaryKey.java")

#parse("DAO.java.d/androidDelete.java")

#parse("DAO.java.d/querySetForAssociations.java")
##
##
#parse("DAO.java.d/androidCursorToObject.java")

#parse("DAO.java.d/mapToObject.java")

#parse("DAO.java.d/updateWithMap.java")

#parse("DAO.java.d/androidValidate.java")

#foreach ($association in $manyToManyAssociations)
#**##parse("DAO.java.d/androidManyToManyHandlers.java")
#end
##
#parse("DAO.java.d/toManyDAO.java")

#parse("DAO.java.d/androidQuery.java")

#parse("DAO.java.d/getTable.java")

    protected ${Target} new${Target}(){
        return new ${KlassImpl}();
    }

    protected ${Target} new${Target}(${constructorArgsDecl}){
    	${Target} obj = new${Target}();
#foreach ($field in $fields)
#**###
#**### O metodo setter nao deve ser gerado se houver uma associacao one-to-many
#**### em que ele eh a FK
#**### Se for marcado como chave primaria, ou "Set" como false, deve existir na
#**### implementacao e na interface editavel.
#**###
#**##if ( !$associationForField[$field] && (($field.Set && !$field.PrimaryKey)))
	    obj.set${field.UpperCamel}(${field.LowerCamel}AG);
#**##end
#**##if ($field.PrimaryKey)
	    ((${Target}Editable) obj).setId(idAG);
#**##end
#end
##
#foreach ($association in $manyToOneAssociations)
    	((${Target}Editable) obj)
            .set${association.KeyToA}(
                ${association.KeyToA}AG);
#end
##
#foreach ($association in $oneToManyAssociations)
    	((${Target}Editable) obj)
            .set${association.KeyToAPluralized}(
                ${association.KeyToAPluralized}AG);
#end
#foreach ($association in $manyToManyAssociations)
    	((${Target}Editable) obj)
            .set${association.Pluralized}(
                ${association.Pluralized}AG);
#end
        return obj;
    }
    
    protected final ${DaoFactory} getFactory(){
    	return factory;
    }
}

