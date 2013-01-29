package com.quantium.mobile.geradores.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;

public class ColumnsUtils {


	private static final class TypeAndNameComparator implements
			Comparator<Property> {
		@Override
		public int compare(Property col1, Property col2) {
			if(!col1.getType().equals(col2.getType()))
				return col1.getType().compareTo(col2.getType());
			return col1.getNome().compareTo(col2.getNome());
		}
	}

	public static List<String> orderedColumnsFromJavaBeanSchema(
			JavaBeanSchema javaBeanSchema)
	{
		return orderedColumnsFromTableSchema (javaBeanSchema);
	}

	public static List<String> orderedColumnsFromTableSchema(
			JavaBeanSchema table)
	{
		Set<Property> setOrdenado = 
			new TreeSet<Property>(
				new TypeAndNameComparator()
			);
		for (String coluna : table.getColunas ()) {
			setOrdenado.add(table.getPropriedade (coluna));
		}

		List<String> primaryKeys = new ArrayList<String>();
		List<String> notPrimaryKeys = new ArrayList<String>();

		// Separando as chaves primarias do resto
		for(Property column : setOrdenado){
			Constraint constraints [] = column.getConstraints ();
			boolean isPrimaryKey = false;
			for (Constraint constraint : constraints) {
				if (constraint.getType () == Constraint.Type.PRIMARY_KEY){
					isPrimaryKey = true;
					break;
				}
			}
			((isPrimaryKey) ? primaryKeys : notPrimaryKeys)
				.add(column.getNome());
		}

		List<String> colunasEmOrdem = new ArrayList<String>();
		colunasEmOrdem.addAll(primaryKeys);
		colunasEmOrdem.addAll(notPrimaryKeys);
		return colunasEmOrdem;
	}

	public static boolean checkIfIsPK(Table.Column<?> col) {
		boolean isPK = false;
		for (Constraint constraint : col.getConstraintList ()){
			if (constraint.isTypeOf (Constraint.Type.PRIMARY_KEY)) {
				isPK = true;
			}
		}
		return isPK;
	}

	public static boolean checkIfIsPK(Property prop) {
		boolean isPK = false;
		for (Constraint constraint : prop.getConstraints ()){
			if (constraint.isTypeOf (Constraint.Type.PRIMARY_KEY)) {
				isPK = true;
			}
		}
		return isPK;
	}

	public static boolean isNullable(Property prop) {
		boolean isPK = true;
		for (Constraint constraint : prop.getConstraints ()){
			if (constraint.isTypeOf (Constraint.Type.NOT_NULL)) {
				isPK = false;
			}
		}
		return isPK;
	}

}
