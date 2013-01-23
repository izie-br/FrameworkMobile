package com.quantium.mobile.geradores.velocity.helpers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;

public class ConstructorArgsHelper {

	JavaBeanSchema javaBeanSchema;
	List<Property> orderedFields;
	Map<Property, Object> associationForPropertyMap;
	Collection<Object> oneToManyAssociations;
	Collection<Object> manyToManyAssociations;

	public ConstructorArgsHelper(JavaBeanSchema javaBeanSchema,
			List<Property> orderedFields,
			Map<Property, Object> associationForPropertyMap,
			Collection<Object> oneToManyAssociations,
			Collection<Object> manyToManyAssociations) {
		this.javaBeanSchema = javaBeanSchema;
		this.orderedFields = orderedFields;
		this.associationForPropertyMap = associationForPropertyMap;
		this.oneToManyAssociations = oneToManyAssociations;
		this.manyToManyAssociations = manyToManyAssociations;
	}

	public String getConstructorArguments(){
		return getConstructorArgumentsAndDecl(
				javaBeanSchema, orderedFields, associationForPropertyMap,
				oneToManyAssociations,manyToManyAssociations, false);
	}

	public String getConstructorArgsDecl(){
		return getConstructorArgumentsAndDecl(
				javaBeanSchema, orderedFields, associationForPropertyMap,
				oneToManyAssociations,manyToManyAssociations, true);
	}

	private static String getConstructorArgumentsAndDecl(
			JavaBeanSchema javaBeanSchema,
			List<Property> orderedFields,
			Map<Property, Object> associationForPropertyMap,
			Collection<Object> oneToManyAssociations,
			Collection<Object> manyToManyAssociations,
			boolean declare)
	{
		int argCount = orderedFields.size() + oneToManyAssociations.size() + manyToManyAssociations.size();
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
				String attibuteName =
						(String)(
							(Map<?,?>)associationForPropertyMap.get(field)
						).get("KeyToA");
				if (declare) {
					sb.append(klass);
					sb.append(' ');
				}
				sb.append('_');
				sb.append(attibuteName);
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
		for (Object obj : oneToManyAssociations){
			@SuppressWarnings("unchecked")
			Map<String, Object> assoc = (Map<String, Object>) obj;
			boolean last = (i == (argCount-1) );

			if (declare){
				sb.append("QuerySet<");
				sb.append(assoc.get("Klass"));
				sb.append("> ");
				sb.append('_');
				sb.append(assoc.get("KeyToAPluralized").toString());
			} else {
				sb.append("querySetFor");
				sb.append(assoc.get("KeyToAPluralized").toString());
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
		for (Object obj : manyToManyAssociations){
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
