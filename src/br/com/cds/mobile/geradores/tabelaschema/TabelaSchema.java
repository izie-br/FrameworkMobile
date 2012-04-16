package br.com.cds.mobile.geradores.tabelaschema;

import java.util.Map;

public interface TabelaSchema {

	/**
	 * Nome da tabela
	 * @return nome
	 */
	public abstract String getNome();

	/**
	 * Colunas e tipos de dados de cada coluna da tabela
	 * @return Map com pares ( nome_da_coluna , classe_java )
	 */
	public abstract Map<String, Class<?>> getColunas();

}