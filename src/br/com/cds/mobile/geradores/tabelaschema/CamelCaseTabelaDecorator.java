package br.com.cds.mobile.geradores.tabelaschema;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import br.com.cds.mobile.geradores.util.CamelCaseUtils;


public class CamelCaseTabelaDecorator implements TabelaSchema {

	private TabelaSchema tabelaDecorada;

	public CamelCaseTabelaDecorator(TabelaSchema tabelaDecorada){
		this.tabelaDecorada = tabelaDecorada;
	}

	@Override
	public String getNome() {
		return CamelCaseUtils.toUpperCamelCase(tabelaDecorada.getNome());
	}

	@Override
	public Map<String, TabelaSchema.Coluna> getPropriedades() {
		Map<String, TabelaSchema.Coluna> out = new HashMap<String, TabelaSchema.Coluna>();
		Map<String,TabelaSchema.Coluna> propriedades = tabelaDecorada.getPropriedades();
		for(String nomeColuna : propriedades.keySet()){
			out.put(CamelCaseUtils.tolowerCamelCase(nomeColuna), propriedades.get(nomeColuna));
		}
		return out;
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
