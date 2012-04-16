package br.com.cds.mobile.geradores.tabelaschema;

import java.util.Collection;
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
	public Map<String, TabelaSchema.Coluna> getPropriedades() {
		return tabelaDecorada.getPropriedades();
	}

	@Override
	public String getTabela() {
		return tabelaDecorada.getTabela();
	}

	@Override
	public Collection<TabelaSchema.Coluna> getColunas() {
		return tabelaDecorada.getColunas();
	}

}
