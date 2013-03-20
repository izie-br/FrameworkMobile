package com.quantium.mobile.geradores.filters;

import java.util.Collection;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.validation.Constraint;

/**
 * <p>
 *   Altera a instancia de {@link Table}, modificando o nome adicionando
 *   um prefixo antes do nome original.
 * </p>
 * <p>
 *   Onde o metodo {@link Table#getName()} retornaria "NOME_DA_TABELA",
 *   com prefixo= "TB_", passara a retornar "TB_NOME_DA_TABELA"
 * </p>
 * 
 * @author Igor Soares
 *
 */
public class PrefixoTabelaFilter extends TabelaSchemaFilter{

	private String prefixo;

	@Override
	public String getName() {
		String nome = super.getName();
		if(nome.startsWith(prefixo))
			nome =  nome.substring(prefixo.length());
		return nome;
	}

	@Override
	public Table getTable() {
		Table table = super.getTable ();
		if (table.getName ().startsWith (prefixo))
			return table;
		Table newtable = new Table ("tb_" + table.getName ());
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

	/**
	 * @see PrefixoTabelaFilter\
	 * 
	 * @author Igor Soares
	 *
	 */
	public static class Factory implements TabelaSchemaFilterFactory{

		private String prefixo;

		/**
		 * <p>Instancia uma Factory de {@link PrefixoTabelaFilter}.</p>
		 * <p>
		 *   Todas as instancias criadas por esta factory terao
		 *   o mesmo {@code prefixo}.
		 * </p>
		 * 
		 * @see PrefixoTabelaFilter
		 * 
		 * @param prefixo prefixo de todas instacias geradas
		 */
		public Factory(String prefixo) {
			super();
			this.prefixo = prefixo;
		}

		@Override
		public TabelaSchemaFilter getFilterInstance() {
			PrefixoTabelaFilter filtro = new PrefixoTabelaFilter();
			filtro.prefixo = this.prefixo;
			return filtro;
		}
	}

}
