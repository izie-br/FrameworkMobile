package com.quantium.mobile.geradores.filters;

import java.util.Collection;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.util.Constants;

public class ModuleNameOnTablePrefixFilter extends TabelaSchemaFilter {


	@Override
	public Table getTable() {
		Table table = super.getTable ();
		String module = super.getModule ();
		if (module.equals (Constants.DEFAULT_MODULE_NAME))
			return table;

		Table.Builder builder = Table.create (module+ '_' + table.getName ());
		for (Table.Column<?> col : table.getColumns ()) {
			Collection<Constraint> constraintList = col.getConstraintList ();
			Constraint array [] = new Constraint[constraintList.size ()];
			constraintList.toArray (array);
			builder.addColumn (col.getKlass (), col.getName (), array);
		}
		for (Constraint constraint : table.getConstraints ()) {
			builder.addConstraint (constraint);
		}
		return builder.get ();
	}

/*
 * Se precisar de modulos com input de SQL:
 *  - retrabalhar o metodo abaixo, pois do jeito que esta,
 *    ele buga se o nome do modulo for igual ao, ou apenas contido no inicio,
 *    nome de uma classe, que fica "" apos passar este filtro;
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
	public static class Factory implements TabelaSchemaFilterFactory {

		@Override
		public TabelaSchemaFilter getFilterInstance() {
			return new ModuleNameOnTablePrefixFilter ();
		}

	}

}
