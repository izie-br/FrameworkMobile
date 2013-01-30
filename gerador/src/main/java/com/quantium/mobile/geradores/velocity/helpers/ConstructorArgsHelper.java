package com.quantium.mobile.geradores.velocity.helpers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;

public class ConstructorArgsHelper {

	JavaBeanSchema javaBeanSchema;
	List<Property> orderedFields;
	Map<Property, OneToManyAssociationHelper> associationForPropertyMap;
	Collection<OneToManyAssociationHelper> oneToManyAssociations;
	Collection<ManyToManyAssociationHelper> manyToManyAssociations;

	public ConstructorArgsHelper(JavaBeanSchema javaBeanSchema,
			List<Property> orderedFields,
			Map<Property, OneToManyAssociationHelper> associationForPropertyMap,
			Collection<OneToManyAssociationHelper> oneToManyAssociations,
			Collection<ManyToManyAssociationHelper> manyToManyAssociations) {
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
			Map<Property, OneToManyAssociationHelper> associationForPropertyMap,
			Collection<OneToManyAssociationHelper> oneToManyAssociations,
			Collection<ManyToManyAssociationHelper> manyToManyAssociations,
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
				String klass = associationForPropertyMap.get(field)
						.getKlass();
				String attibuteName = associationForPropertyMap.get(field)
						.getKeyToA();
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
		for (OneToManyAssociationHelper assoc : oneToManyAssociations){
			boolean last = (i == (argCount-1) );

			if (declare){
				sb.append("QuerySet<");
				sb.append(assoc.getKlass());
				sb.append("> ");
				sb.append('_');
				sb.append(assoc.getKeyToAPluralized().toString());
			} else {
				sb.append("querySetFor");
				sb.append(assoc.getKeyToAPluralized().toString());
				sb.append("(_");
				Property property = assoc.getReferenceKey();
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
		for (ManyToManyAssociationHelper assoc : manyToManyAssociations){
			boolean last = (i == (argCount-1) );

			if (declare){
				sb.append("QuerySet<");
				sb.append(assoc.getKlass());
				sb.append("> ");
				sb.append('_');
				sb.append(assoc.getPluralized().toString());
			} else {
				sb.append("querySetFor");
				sb.append(assoc.getPluralized().toString());
				sb.append("(_");
				Property property =
					(assoc.isThisTableA())?
						assoc.getReferenceA() :
					// default
						assoc.getReferenceB();
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
