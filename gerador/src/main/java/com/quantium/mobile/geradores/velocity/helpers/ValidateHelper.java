package com.quantium.mobile.geradores.velocity.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.quantium.mobile.geradores.javabean.Constraint;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;

public class ValidateHelper {

	private JavaBeanSchema javaBeanSchema;
	private List<Property> orderedFields;

	private List<Collection<Property>> uniques = null;
	private List<Property> notNull = null;


	public ValidateHelper(
			JavaBeanSchema javaBeanSchema,
			List<Property> orderedFields)
	{
		this.javaBeanSchema = javaBeanSchema;
		this.orderedFields = orderedFields;
	}

	/**
	 * Para o caso de uma constraint "UNIQUE(col1, col2)"
	 * o par col1 e col2 que devem ser unico,
	 * mas nao col1 e col2 individualmente
	 * Deve-se prever este caso, logo, uniques devem ser
	 * uma "lista de propriedades" (note propriedades no plural)
	 */
	public List<Collection<Property>> getUniques () {
		findConstraints ();
		return uniques;
	}

	public List<Property> getNotNull () {
		findConstraints ();
		return notNull;
	}


	private void findConstraints () {
		if (uniques != null && notNull != null)
			return;

		uniques = new ArrayList<Collection<Property>> ();
		notNull = new ArrayList<Property> ();

		for (Property property : orderedFields) {
			for (Constraint constraint : property.getConstraints ()) {
				switch (constraint.getType ()) {
				case UNIQUE:
					Collection<Property> column = new ArrayList<Property> (1);
					column.add (property);
					uniques.add (column);
					break;
				case NOT_NULL:
					notNull.add (property);
					break;
				default:
					/* no-op */
				}
			}
		}

	}

}
