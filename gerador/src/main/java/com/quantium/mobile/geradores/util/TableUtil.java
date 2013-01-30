package com.quantium.mobile.geradores.util;

import java.util.ArrayList;
import java.util.List;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.geradores.javabean.ModelSchema;
import com.quantium.mobile.geradores.javabean.Property;

public class TableUtil {

	public static Table tableForModelSchema (ModelSchema modelSchema) {
		Table.Builder builder = Table.create (modelSchema.getName ());
		List<Property> properties =
				new ArrayList<Property>(modelSchema.getProperties ());
		for (int i=0; i< properties.size (); i++) {
			Property prop = properties.get (i);
			builder.addColumn (
					prop.getPropertyClass (), prop.getNome (),
					prop.getConstraints ());
		}
		return builder.get ();
	}

}
