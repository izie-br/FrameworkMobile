package com.quantium.mobile.geradores.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.tabelaschema.TabelaSchema;
import com.quantium.mobile.geradores.tabelaschema.TabelaSchema.Coluna;


public class ColumnsUtils {


	private static final class TypeAndNameComparator implements
			Comparator<TabelaSchema.Coluna> {
		@Override
		public int compare(Coluna col1, Coluna col2) {
			if(!col1.getType().equals(col2.getType()))
				return col1.getType().getName().compareTo(col2.getType().getName());
			return col1.getNome().compareTo(col2.getNome());
		}
	}

	public static List<String> orderedColumnsFromJavaBeanSchema(
			JavaBeanSchema javaBeanSchema
	) {

		TabelaSchema table = javaBeanSchema.getTabela();
		List<String> colunasEmOrdem = orderedColumnsFromTableSchema (table);

		return colunasEmOrdem;

	}

	public static List<String> orderedColumnsFromTableSchema(
			TabelaSchema table)
	{
		Set<TabelaSchema.Coluna> setOrdenado = 
			new TreeSet<TabelaSchema.Coluna>(
				new TypeAndNameComparator()
			);
		setOrdenado.addAll(table.getColunas());

		List<String> primaryKeys = new ArrayList<String>();
		List<String> notPrimaryKeys = new ArrayList<String>();

		// Separando as chaves primarias do resto
		for(TabelaSchema.Coluna column : setOrdenado){
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
}
