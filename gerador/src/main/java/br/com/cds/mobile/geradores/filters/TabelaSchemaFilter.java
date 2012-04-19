package br.com.cds.mobile.geradores.filters;

import java.util.Collection;

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
