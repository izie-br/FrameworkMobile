package com.quantium.mobile.geradores.filters;

import java.util.Collection;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.javabean.ModelSchema;
import com.quantium.mobile.geradores.javabean.Property;


public abstract class TabelaSchemaFilter {

	private TabelaSchemaFilter proximo = null;

	/**
	 * retorna o nome da tabela
	 * @return
	 */
	public String getName(){
		return proximo.getName();
	}

	public String getModule () {
		return proximo.getModule ();
	}

	public boolean isNonEntityTable(){
		return proximo.isNonEntityTable();
	}

	protected ModelSchema getModelSchema () {
		return proximo.getModelSchema ();
	}

	public Property getPropriedade(String coluna){
		return proximo.getPropriedade(coluna);
	}

	public Property getPrimaryKey () {
		return proximo.getPrimaryKey ();
	}

	public String getConstante(String coluna){
		return proximo.getConstante(coluna);
	}

	public Collection<Associacao> getAssociacoes(){
		return proximo.getAssociacoes();
	}

	public Table getTable (){
		return proximo.getTable ();
	}

	public final void proximoFiltro(TabelaSchemaFilter filtro){
			proximo = filtro;
	}

}
