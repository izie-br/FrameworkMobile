package com.quantium.mobile.geradores.util;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.geradores.javabean.ModelSchema;
import com.quantium.mobile.geradores.javabean.Property;

public class TableUtil {

	public static Table tableForModelSchema (ModelSchema modelSchema) {
		Table.Builder builder = Table.create (modelSchema.getName ());
		for (Property prop : modelSchema.getProperties ()) {
			builder.addColumn (
					prop.getPropertyClass (), prop.getNome (),
					prop.getConstraints ());
		}
		return builder.get ();
	}

}
