package br.com.cds.mobile.geradores.javabean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import br.com.cds.mobile.geradores.filters.TabelaSchemaFilter;
import br.com.cds.mobile.geradores.filters.TabelaSchemaFilterFactory;
import br.com.cds.mobile.geradores.filters.associacao.Associacao;
import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;

public class JavaBeanSchema{

	private String constanteDaTabela = "TABELA";
	private TabelaSchema tabela;
	private TabelaSchemaFilter filterChain;

	public TabelaSchema getTabela(){
		return this.tabela;
	}

	public String getNome(){
		return filterChain.getNome();
	}

	public Propriedade getPropriedade(String coluna){
		return filterChain.getPropriedade(coluna);
	}

	public Collection<String> getColunas(){
		Collection<String> colunas = new HashSet<String>();
		for(TabelaSchema.Coluna coluna : tabela.getColunas())
			colunas.add(coluna.getNome());
		return colunas;
	}

	public Propriedade getPrimaryKey(){
		Collection<TabelaSchema.Coluna> colunas = tabela.getPrimaryKeys();
		if(colunas.size()!=1)
			return null;
		return getPropriedade(colunas.iterator().next().getNome());
	}

	public Collection<String> getPrimaryKeyColumns(){
		Collection<String> props = new ArrayList<String>();
		Collection<TabelaSchema.Coluna> colunas = tabela.getPrimaryKeys();
		for(TabelaSchema.Coluna coluna :colunas)
			props.add(coluna.getNome());
		return props;
	}

	public String getConstante(String coluna){
		return filterChain.getConstante(coluna);
	}

	public String getConstanteDaTabela() {
		return constanteDaTabela;
	}

	public Collection<Associacao> getAssociacoes(){
		return filterChain.getAssociacoes();
	}

	private void adicionarFiltro(TabelaSchemaFilter filtro){
		filtro.proximoFiltro(this.filterChain);
		this.filterChain = filtro;
	}


	/**
	 * O construtor privado soh eh acessado pela factory
	 */
	private JavaBeanSchema(){
		filterChain = new FiltroRaiz();
	}

	private class FiltroRaiz extends TabelaSchemaFilter{

		@Override
		public String getNome() {
			return tabela.getNome();
		}

		@Override
		public Propriedade getPropriedade(String coluna) {
			for(TabelaSchema.Coluna it : tabela.getColunas()){
				if(it.getNome().equals(coluna)) {
					return new Propriedade(
							it.getNome(),
							it.getType(),
							true,
							true
					);
				}
			}
			return null;
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
			return new ArrayList<Associacao>();
		}

	}

	public static class Factory{

		private String constanteDaTabela;
		private Collection<TabelaSchemaFilterFactory> filtros = new ArrayList<TabelaSchemaFilterFactory>();

		public void addFiltroFactory(TabelaSchemaFilterFactory filtroFactory){
			filtros.add(filtroFactory);
		}

		public void setConstanteComNomeDaTabela(String constante){
			constanteDaTabela = constante;
		}

		public JavaBeanSchema javaBeanSchemaParaTabela(TabelaSchema tabela){
			JavaBeanSchema javaBeanSchema = new JavaBeanSchema();
			javaBeanSchema.tabela = tabela;
			for(TabelaSchemaFilterFactory filtro : filtros)
				javaBeanSchema.adicionarFiltro(filtro.getFilterInstance());
			if(constanteDaTabela!=null)
				javaBeanSchema.constanteDaTabela = constanteDaTabela;
			return javaBeanSchema;
		}

	}

}
