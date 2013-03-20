package com.quantium.mobile.geradores.filters;

import java.util.Collection;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.util.Constants;

/**
 * <p>
 *   Altera a instancia de {@link Table}, modificando o nome adicionando o
 *   nome do modulo antes ao nome original.
 * </p>
 * <p>
 *   Onde o metodo {@link Table#getName()} retornaria "NOME_DA_TABELA",
 *   passara a retornar "MODULO_NOME_DA_TABELA"
 * </p>
 * 
 * @author Igor Soares
 *
 */
public class ModuleNameOnTablePrefixFilter extends TabelaSchemaFilter {


	@Override
	public Table getTable() {
		Table table = super.getTable ();
		String module = super.getModule ();
		if (module.equals (Constants.DEFAULT_MODULE_NAME))
			return table;

		String tableName = module+ '_' + table.getName ();
		Table newtable = new Table (tableName);
		for (Table.Column<?> col : table.getColumns ()) {
			Collection<Constraint> constraintList = col.getConstraintList ();
			Constraint array [] = new Constraint[constraintList.size ()];
			constraintList.toArray (array);
			newtable.addColumn (col.getKlass (), col.getName (), array);
		}
		for (Constraint constraint : table.getConstraints ()) {
			newtable.addConstraint (constraint);
		}
		return newtable;
	}

/*
 * Se precisar de modulos com input de SQL:
 *  - retrabalhar o metodo abaixo, pois do jeito que esta,
 *    se o nome do modulo for igual ao (ou contido no inicio)
 *    nome de uma classe, ela fica "" apos passar por este filtro;
 *
 *	private static final String SEPARATOR = "_";
 *	@Override
 *	public String getName() {
 *		String nome = super.getName();
 *		String module = super.getModule ();
 *		if (module.equals (Constants.DEFAULT_GENERATOR_CONFIG))
 *			return nome;
 *		if(nome.startsWith(module))
 *			nome =  nome.substring(module.length());
 *		if (nome.startsWith (SEPARATOR))
 *			nome = nome.substring (SEPARATOR.length ());
 *		return nome;
 *	}
 */

	/**
	 * @see ModuleNameOnTablePrefixFilter
	 * 
	 * @author Igor Soares
	 *
	 */
	public static class Factory implements TabelaSchemaFilterFactory {

		@Override
		public TabelaSchemaFilter getFilterInstance() {
			return new ModuleNameOnTablePrefixFilter ();
		}

	}

}
