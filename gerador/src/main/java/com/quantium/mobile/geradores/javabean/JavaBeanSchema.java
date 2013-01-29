package com.quantium.mobile.geradores.javabean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.filters.TabelaSchemaFilter;
import com.quantium.mobile.geradores.filters.TabelaSchemaFilterFactory;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import com.quantium.mobile.geradores.util.ColumnsUtils;
import com.quantium.mobile.geradores.util.TableUtil;

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

	private ModelSchema tabela;
	private TabelaSchemaFilter filterChain;

	public ModelSchema getModelSchema () {
		return tabela;
	}

	public Table getTabela() {
		return filterChain.getTable ();
	}

	public String getNome() {
		return filterChain.getName();
	}

	public boolean isNonEntityTable() {
		return filterChain.isNonEntityTable();
	}

	public Property getPropriedade(String coluna) {
		return filterChain.getPropriedade(coluna);
	}

	public Collection<String> getColunas() {
		Collection<String> colunas = new HashSet<String>();
		for (Property coluna : tabela.getProperties ())
			colunas.add(coluna.getNome());
		return colunas;
	}

	public Property getPrimaryKey() {
		return filterChain.getPrimaryKey ();
	}

	public String getConstante(String coluna) {
		return filterChain.getConstante(coluna);
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
		public String getName() {
			return tabela.getName ();
		}

		@Override
		public Property getPrimaryKey() {
			for (Property prop : tabela.getProperties ()) {
				if (ColumnsUtils.checkIfIsPK (prop))
					return prop;
			}
			throw new RuntimeException ();
		}

		@Override
		public boolean isNonEntityTable() {
			Collection<Property> props = tabela.getProperties ();
			Property pk = tabela.getPrimaryKey ();
			Collection<Associacao> associations = tabela.getAssociacoes ();

			property_loop:
			for (Property property : props){
				// confere se eh PK
				if (property.equals (pk)){
					continue property_loop;
				}
				// confere se eh FK
				for (Associacao assoc : associations) {
					if (assoc instanceof AssociacaoOneToMany) {
						AssociacaoOneToMany o2m = (AssociacaoOneToMany) assoc;
						if (o2m.getKeyToA ().equals (property.getNome ()))
							continue property_loop;
					}
				}
				// chega aqui se a propriedade nao for PK nem FK
				return false;
			}
			return true;
		}

		@Override
		public ModelSchema getModelSchema () {
			return JavaBeanSchema.this.tabela;
		}

		@Override
		public Property getPropriedade(String coluna) {
			for (Property it : tabela.getProperties ()) {
				if (it.getNome().equals(coluna)) {
					boolean isNotPrimaryKey = true;
					Constraint constraints[] = it.getConstraints();
					if (constraints != null) {
						for (Constraint constraint : constraints) {
							if (constraint.getType() == Constraint.Type.PRIMARY_KEY) {
								isNotPrimaryKey = false;
							}
						}
					}
					//TODO refazer isso, apenas repassar a property do modelfacade
					return new Property(
							it.getNome(), it.getPropertyClass (),
							true, isNotPrimaryKey, it.getConstraints ());
				}
			}
			throw new IllegalArgumentException(String.format(
					"Coluna '%s' nao encontrada na tabela '%s'",
					coluna, tabela.getName()));
		}

		@Override
		public String getConstante(String coluna) {
			return getPropriedade(coluna).getNome();
		}

		@Override
		public Table getTable () {
			return TableUtil.tableForModelSchema (tabela);
		}

		@Override
		public Collection<Associacao> getAssociacoes() {
			return tabela.getAssociacoes();
		}

	}

	public static class Factory {

		private Collection<TabelaSchemaFilterFactory> filtros = new ArrayList<TabelaSchemaFilterFactory>();

		public void addFiltroFactory(TabelaSchemaFilterFactory filtroFactory) {
			filtros.add(filtroFactory);
		}

		public JavaBeanSchema javaBeanSchemaParaTabela(ModelSchema tabela) {
			JavaBeanSchema javaBeanSchema = new JavaBeanSchema();
			javaBeanSchema.tabela = tabela;
			for (TabelaSchemaFilterFactory filtro : filtros)
				javaBeanSchema.adicionarFiltro(filtro.getFilterInstance());
			return javaBeanSchema;
		}
	}

}
