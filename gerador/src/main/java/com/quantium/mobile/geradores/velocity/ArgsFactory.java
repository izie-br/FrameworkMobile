package com.quantium.mobile.geradores.velocity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;

public class ArgsFactory {

	public static String getConstructorArguments(
			JavaBeanSchema javaBeanSchema,
			List<Property> orderedFields,
			Map<Property, Object> associationForPropertyMap,
			Collection<Object> toManyAssociations)
	{
		int argCount = orderedFields.size() + toManyAssociations.size();
		StringBuilder sb = new StringBuilder();
		String indent = "\n";
		sb.append(indent);

		int i = 0;
		for (; i < orderedFields.size(); i++) {
			sb.append('_');

			Property field = orderedFields.get(i);
			boolean last = (i == (argCount-1) );
			if (associationForPropertyMap.get(field) != null){
				String klass =
					(String)(
						(Map<?,?>)associationForPropertyMap.get(field)
					).get("Klass");
				sb.append(klass);
			} else {
				sb.append(field.getLowerCamel());
			}
			if (!last) sb.append(',');
		}
		for (Object obj : toManyAssociations){
			@SuppressWarnings("unchecked")
			Map<String, Object> assoc = (Map<String, Object>) obj;
			boolean last = (i == (argCount-1) );
			i++;
			sb.append("querysetFor");
			sb.append(assoc.get("Klass").toString());
			sb.append("(_");
			Property property =
				(assoc.get("ReferenceKey") != null)?
					(Property)assoc.get("ReferenceKey") :
				((Boolean)assoc.get("IsThisTableA"))?
					(Property)assoc.get("ReferenceA") :
				// default
					(Property)assoc.get("ReferenceB");
			sb.append(property.getLowerCamel());
			sb.append(')');
			if (!last) sb.append(',');
		}
		return sb.toString();
	}

}
