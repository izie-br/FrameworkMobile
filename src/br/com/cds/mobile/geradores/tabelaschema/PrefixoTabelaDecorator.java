package br.com.cds.mobile.geradores.tabelaschema;

import java.util.Map;


public class PrefixoTabelaDecorator implements TabelaSchema {

	private TabelaSchema tabelaDecorada;
	private String prefixo;

	public PrefixoTabelaDecorator(String prefixo, TabelaSchema tabelaDecorada){
		this.tabelaDecorada = tabelaDecorada;
		this.prefixo = prefixo;
	}

	@Override
	public String getNome() {
		String nome = tabelaDecorada.getNome();
		if(nome.startsWith(prefixo))
			return nome.substring(prefixo.length());
		return nome;
	}

	@Override
	public Map<String, Class<?>> getColunas() {
		return tabelaDecorada.getColunas();
	}

}
