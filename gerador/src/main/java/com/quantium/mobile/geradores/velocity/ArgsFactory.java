package com.quantium.mobile.geradores.velocity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;

public class ArgsFactory {

//	public String get(Object obj) {
//		if (obj instanceof Property)
//			return "_" + ((Property)obj).getLowerCamel();
//		@SuppressWarnings("unchecked")
//		Map<String, Object> assoc = (Map<String, Object>) obj;
//		if (assoc.get("Pluralized") == null) {
//			return "_" + assoc.get("Klass");
//		}
//	}

	public static String getConstructorArguments(
			JavaBeanSchema javaBeanSchema,
			List<Property> orderedFields,
			Map<Property, Object> associationForPropertyMap,
			Collection<Object> toManyAssociations){
		return getConstructorArgumentsAndDecl(
				javaBeanSchema, orderedFields, associationForPropertyMap,
				toManyAssociations, false);
	}

	public static String getConstructorArgsDecl(
			JavaBeanSchema javaBeanSchema,
			List<Property> orderedFields,
			Map<Property, Object> associationForPropertyMap,
			Collection<Object> toManyAssociations){
		return getConstructorArgumentsAndDecl(
				javaBeanSchema, orderedFields, associationForPropertyMap,
				toManyAssociations, true);
	}

	public static String getConstructorArgumentsAndDecl(
			JavaBeanSchema javaBeanSchema,
			List<Property> orderedFields,
			Map<Property, Object> associationForPropertyMap,
			Collection<Object> toManyAssociations,
			boolean declare)
	{
		int argCount = orderedFields.size() + toManyAssociations.size();
		StringBuilder sb = new StringBuilder();
		String indent = "            ";
		sb.append('\n');
		sb.append(indent);

		int i = 0;
		for (; i < orderedFields.size(); i++) {
			Property field = orderedFields.get(i);
			boolean last = (i == (argCount-1) );


			if (associationForPropertyMap.get(field) != null){
				String klass =
					(String)(
						(Map<?,?>)associationForPropertyMap.get(field)
					).get("Klass");
				if (declare) {
					sb.append(klass);
					sb.append(' ');
				}
				sb.append('_');
				sb.append(klass);
			} else {
				if (declare) {
					sb.append(field.getType());
					sb.append(' ');
				}
				sb.append('_');
				sb.append(field.getLowerCamel());
			}
			if (!last){
				sb.append(',');
				if ( i%3 == 2){
					sb.append('\n');
					sb.append(indent);
				} else {
					sb.append(' ');
				}
			}
		}
		for (Object obj : toManyAssociations){
			@SuppressWarnings("unchecked")
			Map<String, Object> assoc = (Map<String, Object>) obj;
			boolean last = (i == (argCount-1) );

			if (declare){
				sb.append("QuerySet<");
				sb.append(assoc.get("Klass"));
				sb.append("> ");
				sb.append('_');
				sb.append(assoc.get("Pluralized").toString());
			} else {
				sb.append("querySetFor");
				sb.append(assoc.get("Pluralized").toString());
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
			}

			if (!last){
				sb.append(',');
				if ( i%3 == 2){
					sb.append('\n');
					sb.append(indent);
				} else {
					sb.append(' ');
				}
			}
			i++;
		}
		return sb.toString();
	}

}
