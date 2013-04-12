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
 * <p>
 *   Classe base dos esquemas usados pelo gerador. Usa uma {@link ModelSchema
 *   como unica fonte de dados. Dados derivados sao obtidos por processamento
 *   dos filtros.
 * </p>
 * <p>Filtro eh um Design Pattern GOF (chain of responsibility)</p>
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

	private final ModelSchema modelSchema;
	private TabelaSchemaFilter filterChain;

	/**
	 * Retorna o {@link ModelSchema} que eh a fonte de dados desta instancia
	 * de {@link JavaBeanSchema}. Nao altere o ModelSchema.
	 * @return modelschema
	 */
	public ModelSchema getModelSchema () {
		return modelSchema;
	}

	/**
	 * <p>Retorna o esquema da tabela (SQL, relacional) para esta entidade.</p>
	 * <p>
	 *   Deve ser identica aquela representada pela constante _TABLE
	 *   presente nas classes de entidade geradas.
	 * </p>
	 * 
	 * @return esquema da tabela relacional para esta entidade
	 */
	public Table getTabela() {
		return filterChain.getTable ();
	}

	public String getNome() {
		return filterChain.getName();
	}

	public String getModule () {
		return filterChain.getModule ();
	}

	/**
	 * Confere este o javabeanschema eh uma entidade, ou uma representacao de
	 * tabela associativa.
	 * 
	 * @return true se eh uma associativa
	 */
	public boolean isNonEntityTable() {
		return filterChain.isNonEntityTable();
	}

	/**
	 * <p>
	 *   Busca uma propriedade {@link Property} cujo nome da coluna seja o
	 *   mesmo do argumento.
	 * </p>
	 * <p>
	 *   Use {@link JavaBeanSchema#getColunas()} para obter os nomes
	 *   das colunas
	 * </p>
	 * @param nome da coluna
	 * @return {@link Property}
	 */
	public Property getPropriedade(String coluna) {
		return filterChain.getPropriedade(coluna);
	}

	/**
	 * Retorna todos nomes de colunas.
	 * @return nomes de colunas
	 */
	public Collection<String> getColunas() {
		Collection<String> colunas = new HashSet<String>();
		for (Property coluna : modelSchema.getProperties ())
			colunas.add(coluna.getNome());
		return colunas;
	}

	public Property getPrimaryKey() {
		return filterChain.getPrimaryKey ();
	}

	/**
	 * Associacoes que envolvem esta entidade, sejam elas one-to-many,
	 * many-to-one ou many-to-many.
	 * @return todas associacoes
	 */
	public Collection<Associacao> getAssociacoes() {
		return filterChain.getAssociacoes();
	}

	/**
	 * Soh deve ser acessado pela factory
	 * @param filtro
	 */
	private void adicionarFiltro(TabelaSchemaFilter filtro) {
		filtro.proximoFiltro(this.filterChain);
		this.filterChain = filtro;
	}

	/**
	 * O construtor privado soh eh acessado pela factory
	 */
	private JavaBeanSchema(ModelSchema tabela) {
		filterChain = new FiltroRaiz();
		this.modelSchema = tabela;
	}

	/**
	 * <p>
	 *   O filtro raiz busca dados diretamente da fonte, {@link ModelSchema},
	 *   em vez de buscar no próximo filtro.
	 * </p>
	 * <p>Nao deve haver proximo filtro em relacao a este.</p>
	 * 
	 * @author Igor Soares
	 *
	 */
	private class FiltroRaiz extends TabelaSchemaFilter {

		@Override
		public String getName() {
			return modelSchema.getName ();
		}

		@Override
		public String getModule() {
			return modelSchema.getModule ();
		}

		@Override
		public Property getPrimaryKey() {
			for (Property prop : modelSchema.getProperties ()) {
				if (ColumnsUtils.checkIfIsPK (prop))
					return prop;
			}
			throw new RuntimeException ();
		}

		@Override
		public boolean isNonEntityTable() {
//			Property pk = modelSchema.getPrimaryKey ();
//			Collection<Associacao> associations =
//					modelSchema.getAssociacoes ();
//
//			// Note:: loop com LABEL
//			property_loop:
//			for (Property property : modelSchema.getProperties ()){
//
//				// confere se eh PK
//				if (property.equals (pk)){
//					// Note: usando label
//					continue property_loop;
//				}
//
//				// confere se eh FK
//				for (Associacao assoc : associations) {
//					if (assoc instanceof AssociacaoOneToMany) {
//						AssociacaoOneToMany o2m =
//								(AssociacaoOneToMany) assoc;
//						String name = property.getNome ();
//						if (o2m.getKeyToA ().equals (name)) {
//							// Note: usando label
//							continue property_loop;
//						}
//					}
//				}
//
//				// chega aqui houver pelo menos uma propriedade
//				// que nao for PK nem FK
//				// Eh uma "entity table"
//				return false;
//			}

			// chega aqui houver apenas propriedades
			// que sao PK ou FK
			return false;
		}

		@Override
		public ModelSchema getModelSchema () {
			return JavaBeanSchema.this.modelSchema;
		}

		@Override
		public Property getPropriedade(String coluna) {
			for (Property property : modelSchema.getProperties ()) {
				if (property.getNome().equals(coluna)) {
					boolean isNotPrimaryKey = true;
					Constraint constraints[] = property.getConstraints();
					if (constraints != null) {
						for (Constraint constraint : constraints) {
							if (constraint instanceof Constraint.PrimaryKey) {
								isNotPrimaryKey = false;
							}
						}
					}
					return new Property(
							property.getNome(), property.getPropertyClass (),
							/*get=*/ true,
							/*set=*/ isNotPrimaryKey,
							property.getConstraints ());
				}
			}
			throw new IllegalArgumentException(String.format(
					"Coluna '%s' nao encontrada na tabela '%s'",
					coluna, modelSchema.getName()));
		}

		@Override
		public Table getTable () {
			return TableUtil.tableForModelSchema (modelSchema);
		}

		@Override
		public Collection<Associacao> getAssociacoes() {
			return modelSchema.getAssociacoes();
		}

	}

	/**
	 * <p>
	 *   Usa instancias de {@link ModelSchema} para criar instancias de
	 *   {@link JavaBeanSchema}.
	 * </p>
	 * <p>
	 *   Recebe instancias de {@link TabelaSchemaFilterFactory}, que sao
	 *   usadas para instanciar {@link TabelaSchemaFilter} para os
	 *   {@link JavaBeanSchema}.
	 * </p>
	 * <p>
	 *   Os filtros sao adicionados aos {@link JavaBeanSchema} seguindo a
	 *   mesma ordem em que as {@link TabelaSchemaFilterFactory} foram
	 *   aqui inseridas.
	 * </p>
	 * 
	 * @author Igor Soares
	 */
	public static class Factory {

		private Collection<TabelaSchemaFilterFactory> filtros =
				new ArrayList<TabelaSchemaFilterFactory>();

		public void addFiltroFactory (TabelaSchemaFilterFactory factory) {
			filtros.add(factory);
		}

		public JavaBeanSchema javaBeanSchemaParaTabela(ModelSchema modelSchema)
		{
			JavaBeanSchema javaBeanSchema = new JavaBeanSchema(modelSchema);
			for (TabelaSchemaFilterFactory filtro : filtros)
				javaBeanSchema.adicionarFiltro(filtro.getFilterInstance());
			return javaBeanSchema;
		}
	}

}
