package br.com.cds.mobile.geradores.filters;

import java.util.Map;

import br.com.cds.mobile.geradores.javabean.Propriedade;

public abstract class TabelaSchemaFilter {

	private TabelaSchemaFilter proximo = null;

	/**
	 * retorna o nome da tabela
	 * @return
	 */
	public String getNome(){
		return proximo.getNome();
	}

	public Propriedade getPropriedade(String coluna){
		return proximo.getPropriedade(coluna);
	}

	public Propriedade getPrimaryKey(){
		return proximo.getPrimaryKey();
	}

	public String getConstante(String coluna){
		return proximo.getConstante(coluna);
	}

	public Map<String,String> getAssociacoesTemUm(){
		return proximo.getAssociacoesTemUm();
	}

	public Map<String,String> getAssociacoesTemMuitos(){
		return proximo.getAssociacoesTemMuitos();
	}

	public final TabelaSchemaFilter getProximo(){
		return proximo;
	}

	public final void proximoFiltro(TabelaSchemaFilter filtro){
			proximo = filtro;
	}


}
