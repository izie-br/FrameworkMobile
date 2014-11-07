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

	public String getConstructorArgumentsForDAO(){
		return getConstructorArgumentsAndDecl(
				javaBeanSchema, orderedFields, associationForPropertyMap,
				oneToManyAssociations,manyToManyAssociations, false, true);
	}

	public String getConstructorArguments(){
		return getConstructorArgumentsAndDecl(
				javaBeanSchema, orderedFields, associationForPropertyMap,
				oneToManyAssociations,manyToManyAssociations, false, false);
	}

	public String getConstructorArgsDecl(){
		return getConstructorArgumentsAndDecl(
				javaBeanSchema, orderedFields, associationForPropertyMap,
				oneToManyAssociations,manyToManyAssociations, true, false);
	}
	
	public String getConstructorSetters(){
		return getConstructorArgumentsSetters(
				javaBeanSchema, orderedFields, associationForPropertyMap,
				oneToManyAssociations,manyToManyAssociations, true, false);
	}

	private static String getConstructorArgumentsAndDecl(
			JavaBeanSchema javaBeanSchema,
			List<Property> orderedFields,
			Map<Property, OneToManyAssociationHelper> associationForPropertyMap,
			Collection<OneToManyAssociationHelper> oneToManyAssociations,
			Collection<ManyToManyAssociationHelper> manyToManyAssociations,
			boolean declare, boolean forDao)
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
				sb.append(attibuteName);
                sb.append('_');
            } else {
				if (declare) {
					sb.append(field.getType());
					sb.append(' ');
				}
				sb.append(field.getLowerCamel());
                sb.append('_');
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
				sb.append(assoc.getKeyToAPluralized().toString());
                sb.append('_');
            } else if (forDao){
				sb.append("querySetFor");
				sb.append(assoc.getKeyToAPluralized().toString());
				sb.append("(");
				Property property = assoc.getReferenceKey();
				sb.append(property.getLowerCamel());
				sb.append("_)");
			} else {
				sb.append(assoc.getKeyToAPluralized().toString());
                sb.append('_');
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
				sb.append(assoc.getPluralized().toString());
                sb.append('_');
            } else if (forDao) {
				sb.append("querySetFor");
				sb.append(assoc.getPluralized().toString());
				sb.append("(");
				Property property =
					(assoc.isThisTableA())?
						assoc.getReferenceA() :
					// default
						assoc.getReferenceB();
				sb.append(property.getLowerCamel());
				sb.append("_)");
            } else {
                sb.append(assoc.getPluralized().toString());
                sb.append('_');
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
	
	private static String getConstructorArgumentsSetters(
			JavaBeanSchema javaBeanSchema,
			List<Property> orderedFields,
			Map<Property, OneToManyAssociationHelper> associationForPropertyMap,
			Collection<OneToManyAssociationHelper> oneToManyAssociations,
			Collection<ManyToManyAssociationHelper> manyToManyAssociations,
			boolean declare, boolean forDao)
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
				sb.append(attibuteName);
                sb.append('_');
            } else {
				if (declare) {
					sb.append(field.getType());
					sb.append(' ');
				}
				sb.append(field.getLowerCamel());
                sb.append('_');
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
				sb.append(assoc.getKeyToAPluralized().toString());
                sb.append('_');
            } else if (forDao){
				sb.append("querySetFor");
				sb.append(assoc.getKeyToAPluralized().toString());
				sb.append("(");
				Property property = assoc.getReferenceKey();
				sb.append(property.getLowerCamel());
				sb.append("_)");
			} else {
				sb.append(assoc.getKeyToAPluralized().toString());
                sb.append('_');
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
				sb.append(assoc.getPluralized().toString());
                sb.append('_');
            } else if (forDao) {
				sb.append("querySetFor");
				sb.append(assoc.getPluralized().toString());
				sb.append("(");
				Property property =
					(assoc.isThisTableA())?
						assoc.getReferenceA() :
					// default
						assoc.getReferenceB();
				sb.append(property.getLowerCamel());
				sb.append("_)");
			} else {
				sb.append(assoc.getPluralized().toString());
                sb.append('_');
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
