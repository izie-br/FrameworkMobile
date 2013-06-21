package com.quantium.mobile.geradores.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;
import com.quantium.mobile.geradores.velocity.helpers.ManyToManyAssociationHelper;
import com.quantium.mobile.geradores.velocity.helpers.OneToManyAssociationHelper;

public class ColumnsUtils {

	private static final class TypeAndNameComparator implements Comparator<Property> {
		@Override
		public int compare(Property col1, Property col2) {
			if (!col1.getType().equals(col2.getType()))
				return col1.getType().compareTo(col2.getType());
			return col1.getNome().compareTo(col2.getNome());
		}
	}

	private static final class TypeAndNameManyToManyAssociacaoComparator implements
			Comparator<ManyToManyAssociationHelper> {
		
		@Override
		public int compare(ManyToManyAssociationHelper col1, ManyToManyAssociationHelper col2) {
			if (!col1.getKlass().equals(col2.getKlass()))
				return col1.getJoinTable().compareTo(col2.getJoinTable());
			return col1.getKeyToA().getNome().compareTo(col2.getKeyToA().getNome());
		}
	}

	private static final class TypeAndNameOneToManyAssociationHelperComparator implements
			Comparator<OneToManyAssociationHelper> {
		@Override
		public int compare(OneToManyAssociationHelper col1, OneToManyAssociationHelper col2) {
			if (!col1.getKlass().equals(col2.getKlass()))
				return col1.getKeyToAPluralized().compareTo(col2.getKeyToAPluralized());
			return col1.getKeyToA().compareTo(col2.getKeyToA());
		}
	}

	public static List<String> orderedColumnsFromJavaBeanSchema(JavaBeanSchema javaBeanSchema) {
		return orderedColumnsFromTableSchema(javaBeanSchema);
	}

	public static List<String> orderedColumnsFromTableSchema(JavaBeanSchema table) {
		Set<Property> setOrdenado = new TreeSet<Property>(new TypeAndNameComparator());
		for (String coluna : table.getColunas()) {
			setOrdenado.add(table.getPropriedade(coluna));
		}

		List<String> primaryKeys = new ArrayList<String>();
		List<String> notPrimaryKeys = new ArrayList<String>();

		// Separando as chaves primarias do resto
		for (Property column : setOrdenado) {
			Constraint constraints[] = column.getConstraints();
			boolean isPrimaryKey = false;
			for (Constraint constraint : constraints) {
				if (constraint instanceof Constraint.PrimaryKey) {
					isPrimaryKey = true;
					break;
				}
			}
			((isPrimaryKey) ? primaryKeys : notPrimaryKeys).add(column.getNome());
		}

		List<String> colunasEmOrdem = new ArrayList<String>();
		colunasEmOrdem.addAll(primaryKeys);
		colunasEmOrdem.addAll(notPrimaryKeys);
		return colunasEmOrdem;
	}

	public static boolean checkIfIsPK(Table.Column<?> col) {
		boolean isPK = false;
		for (Constraint constraint : col.getConstraintList()) {
			if (constraint instanceof Constraint.PrimaryKey) {
				isPK = true;
			}
		}
		return isPK;
	}

	public static boolean checkIfIsPK(Property prop) {
		boolean isPK = false;
		for (Constraint constraint : prop.getConstraints()) {
			if (constraint instanceof Constraint.PrimaryKey) {
				isPK = true;
			}
		}
		return isPK;
	}

	public static boolean isNullable(Property prop) {
		boolean isPK = true;
		for (Constraint constraint : prop.getConstraints()) {
			if (constraint instanceof Constraint.NotNull) {
				isPK = false;
			}
		}
		return isPK;
	}

	public static ArrayList<OneToManyAssociationHelper> orderedOneToManyAssociations(
			Collection<OneToManyAssociationHelper> associations) {
		Set<OneToManyAssociationHelper> setOrdenado = new TreeSet<OneToManyAssociationHelper>(
				new TypeAndNameOneToManyAssociationHelperComparator());
		for (OneToManyAssociationHelper association : associations) {
			setOrdenado.add(association);
		}
		return new ArrayList<OneToManyAssociationHelper>(setOrdenado);
	}

	public static ArrayList<ManyToManyAssociationHelper> orderedManyToManyAssociations(
			Collection<ManyToManyAssociationHelper> associations) {
		Set<ManyToManyAssociationHelper> setOrdenado = new TreeSet<ManyToManyAssociationHelper>(
				new TypeAndNameManyToManyAssociacaoComparator());
		for (ManyToManyAssociationHelper association : associations) {
			setOrdenado.add(association);
		}
		return new ArrayList<ManyToManyAssociationHelper>(setOrdenado);
	}

}
