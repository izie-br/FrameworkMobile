package com.quantium.mobile.geradores.filters;

import java.util.Collection;

import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.javabean.Propriedade;
import com.quantium.mobile.geradores.tabelaschema.TabelaSchema;


public abstract class TabelaSchemaFilter {

	private TabelaSchemaFilter proximo = null;

	/**
	 * retorna o nome da tabela
	 * @return
	 */
	public String getNome(){
		return proximo.getNome();
	}

	public boolean isNonEntityTable(){
		return proximo.isNonEntityTable();
	}

	public Propriedade getPropriedade(String coluna){
		return proximo.getPropriedade(coluna);
	}

	public String getConstante(String coluna){
		return proximo.getConstante(coluna);
	}

	public Collection<Associacao> getAssociacoes(){
		return proximo.getAssociacoes();
	}

	protected TabelaSchema getTabela(){
		return proximo.getTabela();
	}

	public final void proximoFiltro(TabelaSchemaFilter filtro){
			proximo = filtro;
	}


}
