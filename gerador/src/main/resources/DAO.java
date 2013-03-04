package $package;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.SQLException;

import com.quantium.mobile.framework.DAO;
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
import com.quantium.mobile.framework.utils.DateUtil;
import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.framework.validation.ValidationError;

${Imports}

@SuppressWarnings("unused")
public class ${Klass} implements DAOSQLite<${Target}> {

    private ${DaoFactory} factory;

    public ${Klass}(${DaoFactory} factory){
        this.factory = factory;
    }

    @Override
    public String[] getColumns() {
        return new String[] {
#foreach ($field in $fields)
            "${field.UpperAndUnderscores}",
#end
        };
    }

#parse("DAO.java.d/getById.java")

#parse("DAO.java.d/androidSave.java")

#parse("DAO.java.d/androidDelete.java")

#parse("DAO.java.d/querySetForAssociations.java")
##
##
#parse("DAO.java.d/androidCursorToObject.java")

#parse("DAO.java.d/mapToObject.java")

#parse("DAO.java.d/androidValidate.java")

#foreach ($association in $manyToManyAssociations)
#**##parse("DAO.java.d/androidManyToManyHandlers.java")
#end
##
#parse("DAO.java.d/toManyDAO.java")

#parse("DAO.java.d/androidQuery.java")

    protected ${Target} new${Target}(${constructorArgsDecl}){
        return new ${KlassImpl}(${constructorArgs});
    }

}

