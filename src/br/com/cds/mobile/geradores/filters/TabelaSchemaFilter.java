package br.com.cds.mobile.geradores.filters;

import java.util.Collection;
import java.util.Map;

import br.com.cds.mobile.geradores.filters.associacao.Associacao;
import br.com.cds.mobile.geradores.javabean.Propriedade;
import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;

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

	public Collection<Associacao> getAssociacoesTemUm(){
		return proximo.getAssociacoesTemUm();
	}

	public Collection<Associacao> getAssociacoesTemMuitos(){
		return proximo.getAssociacoesTemMuitos();
	}

	protected TabelaSchema getTabela(){
		return proximo.getTabela();
	}

	public final void proximoFiltro(TabelaSchemaFilter filtro){
			proximo = filtro;
	}


}
