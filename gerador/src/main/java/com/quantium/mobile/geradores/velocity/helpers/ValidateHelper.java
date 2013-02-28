package com.quantium.mobile.geradores.velocity.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;

public class ValidateHelper {

//	private JavaBeanSchema javaBeanSchema;
	private List<Property> orderedFields;

	private List<Collection<Property>> uniques = null;
	private List<Property> notNull = null;
	private Map<Property,Constraint> max = null;
	private Map<Property,Constraint> min = null;
	private Map<Property,Constraint> length = null;


	public ValidateHelper(
			JavaBeanSchema javaBeanSchema,
			List<Property> orderedFields)
	{
//		this.javaBeanSchema = javaBeanSchema;
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

	public Map<Property,Constraint> getMax() {
		return max;
	}

	public Map<Property,Constraint> getMin() {
		return min;
	}

	public Map<Property,Constraint> getLength() {
		return length;
	}

	private void findConstraints () {
		if (uniques != null && notNull != null)
			return;

		uniques = new ArrayList<Collection<Property>> ();
		notNull = new ArrayList<Property> ();
		max = new HashMap<Property, Constraint>();
		min = new HashMap<Property, Constraint>();
		length = new HashMap<Property, Constraint>();

		for (Property property : orderedFields) {
			for (Constraint constraint : property.getConstraints ()) {
				if (constraint instanceof Constraint.Unique) {
					Collection<Property> column = new ArrayList<Property> (1);
					column.add (property);
					uniques.add (column);
				} else if (constraint instanceof Constraint.NotNull) {
					notNull.add (property);
				} else if (constraint instanceof Constraint.Max) {
					max.put(property, constraint);
				} else if (constraint instanceof Constraint.Min) {
					min.put(property, constraint);
				} else if (constraint instanceof Constraint.Length) {
					length.put(property, constraint);
				}
			}
		}
	}

}
