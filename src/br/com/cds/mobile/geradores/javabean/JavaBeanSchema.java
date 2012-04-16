package br.com.cds.mobile.geradores.javabean;

import java.util.Map;

import br.com.cds.mobile.geradores.filters.TabelaSchemaFilter;
import br.com.cds.mobile.geradores.tabelaschema.ColunaSchema;
import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;

public class JavaBeanSchema{

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

	public String getCampo(String coluna){
		return filterChain.getCampo(coluna);
	}

	public String getConstante(String coluna){
		return filterChain.getConstante(coluna);
	}

	

	private void adicionarFiltro(TabelaSchemaFilter filtro){
		filtro.proximoFiltro(this.filterChain);
		this.filterChain = filtro;
	}

	private JavaBeanSchema(){
		filterChain = new FiltroMestre();
	}

	private class FiltroMestre extends TabelaSchemaFilter{

		@Override
		public String getNome() {
			return tabela.getNome();
		}

		@Override
		public Propriedade getPropriedade(String coluna) {
			for(TabelaSchema.Coluna it : tabela.getColunas())
				if(it.getNome().equals(coluna))
					return new Propriedade(it.getNome(), it.getType(), true, true);
			return null;
		}

		@Override
		public String getCampo(String coluna) {
			for(TabelaSchema.Coluna it : tabela.getColunas())
				if(it.getNome().equals(coluna))
					return coluna;
			return null;
		}

		@Override public Propriedade         getPrimaryKey()             { return null; }
		@Override public String              getConstante(String coluna) { return null; }
		@Override public Map<String, String> getAssociacoesTemUm()       { return null; }
		@Override public Map<String, String> getAssociacoesTemMuitos()   { return null; }

	}

	static class Factory{
		
	}
}
