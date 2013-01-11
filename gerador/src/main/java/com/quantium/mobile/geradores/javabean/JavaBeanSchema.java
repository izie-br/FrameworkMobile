package com.quantium.mobile.geradores.javabean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.quantium.mobile.geradores.filters.TabelaSchemaFilter;
import com.quantium.mobile.geradores.filters.TabelaSchemaFilterFactory;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.tabelaschema.TabelaSchema;

/**
 * Classe base dos esquemas usados pelo gerador. Usa uma Tabela schema como
 * unica fonte de dados. Dados derivados sao obtidos por processamento dos
 * filtros. Filtro e um Design Pattern GOF (chain of responsibility)
 * 
 * Fluxo das chamadas ao JavaBeanSchema
 * <ul>
 * <li>Requisita informacao ao JavaBeanSchema</li>
 * <li>JavaBeanSchema chama um metodo no FilterChain</li>
 * <li>Cada filtro e chamado e, em geral, chama o filtro subsequente</li>
 * <li>O FiltroRaiz nao tem filtro subsequente, e busca o dado cru no
 * TabelaSchema</li>
 * <li>O dado processado por todos filtros e retornado</li>
 * </ul>
 * 
 * <pre>
 * JavaBeanSchema -> FilterChain... -> Filtro raiz -> TabelaSchema
 * </pre>
 * 
 * @author Igor Soares
 * 
 */
public class JavaBeanSchema {

	private String constanteDaTabela = "TABELA";
	private TabelaSchema tabela;
	private TabelaSchemaFilter filterChain;

	public TabelaSchema getTabela() {
		return this.tabela;
	}

	public String getNome() {
		return filterChain.getNome();
	}

	public boolean isNonEntityTable() {
		return filterChain.isNonEntityTable();
	}

	public Property getPropriedade(String coluna) {
		return filterChain.getPropriedade(coluna);
	}

	public Collection<String> getColunas() {
		Collection<String> colunas = new HashSet<String>();
		for (TabelaSchema.Coluna coluna : tabela.getColunas())
			colunas.add(coluna.getNome());
		return colunas;
	}

	public Property getPrimaryKey() {
		Collection<TabelaSchema.Coluna> colunas = tabela.getPrimaryKeys();
		if (colunas.size() != 1)
			return null;
		return getPropriedade(colunas.iterator().next().getNome());
	}

	public Collection<String> getPrimaryKeyColumns() {
		Collection<String> props = new ArrayList<String>();
		Collection<TabelaSchema.Coluna> colunas = tabela.getPrimaryKeys();
		for (TabelaSchema.Coluna coluna : colunas)
			props.add(coluna.getNome());
		return props;
	}

	public String getConstante(String coluna) {
		return filterChain.getConstante(coluna);
	}

	public String getConstanteDaTabela() {
		return constanteDaTabela;
	}

	public Collection<Associacao> getAssociacoes() {
		return filterChain.getAssociacoes();
	}

	private void adicionarFiltro(TabelaSchemaFilter filtro) {
		filtro.proximoFiltro(this.filterChain);
		this.filterChain = filtro;
	}

	/**
	 * O construtor privado soh eh acessado pela factory
	 */
	private JavaBeanSchema() {
		filterChain = new FiltroRaiz();
	}

	private class FiltroRaiz extends TabelaSchemaFilter {

		@Override
		public String getNome() {
			return tabela.getNome();
		}

		@Override
		public boolean isNonEntityTable() {
			return tabela.isNonEntityTable();
		}

		@Override
		public Property getPropriedade(String coluna) {
			for (TabelaSchema.Coluna it : tabela.getColunas()) {
				boolean isNotPrimaryKey = true;
				Constraint constraints[] = it.getConstraints();
				if (constraints != null) {
					for (Constraint constraint : constraints) {
						if (constraint.getType() == Constraint.Type.PRIMARY_KEY) {
							isNotPrimaryKey = false;
						}
					}
				}
				if (it.getNome().equals(coluna)) {
					//TODO refazer isso, apenas repassar a property do modelfacade
					return new Property(it.getNome(), it.getType(), true,
						isNotPrimaryKey,
						(isNotPrimaryKey)?
							new Constraint[]{} :
							new Constraint[]{new Constraint(Constraint.Type.PRIMARY_KEY)});
				}
			}
			throw new IllegalArgumentException(String.format("Coluna '%s' nao encontrada na tabela '%s'", coluna,
					tabela.getNome()));
		}

		@Override
		public String getConstante(String coluna) {
			return getPropriedade(coluna).getNome();
		}

		@Override
		protected TabelaSchema getTabela() {
			return JavaBeanSchema.this.getTabela();
		}

		@Override
		public Collection<Associacao> getAssociacoes() {
			return tabela.getAssociacoes();
		}

	}

	public static class Factory {

		private String constanteDaTabela;
		private Collection<TabelaSchemaFilterFactory> filtros = new ArrayList<TabelaSchemaFilterFactory>();

		public void addFiltroFactory(TabelaSchemaFilterFactory filtroFactory) {
			filtros.add(filtroFactory);
		}

		public void setConstanteComNomeDaTabela(String constante) {
			constanteDaTabela = constante;
		}

		public JavaBeanSchema javaBeanSchemaParaTabela(TabelaSchema tabela) {
			JavaBeanSchema javaBeanSchema = new JavaBeanSchema();
			javaBeanSchema.tabela = tabela;
			for (TabelaSchemaFilterFactory filtro : filtros)
				javaBeanSchema.adicionarFiltro(filtro.getFilterInstance());
			if (constanteDaTabela != null)
				javaBeanSchema.constanteDaTabela = constanteDaTabela;
			return javaBeanSchema;
		}
	}

}
