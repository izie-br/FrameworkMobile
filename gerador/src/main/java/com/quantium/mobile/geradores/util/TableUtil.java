package com.quantium.mobile.geradores.util;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.geradores.javabean.ModelSchema;
import com.quantium.mobile.geradores.javabean.Property;

public class TableUtil {

    public static Table tableForModelSchema(ModelSchema modelSchema) {
        Table newtable = new Table(modelSchema.getName());
        for (Property prop : modelSchema.getProperties()) {
            newtable.addColumn(
                    prop.getPropertyClass(), prop.getNome(),
                    prop.getConstraints());
        }
        return newtable;
    }

}
