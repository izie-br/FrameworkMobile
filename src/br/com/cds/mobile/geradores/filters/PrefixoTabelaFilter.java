package br.com.cds.mobile.geradores.filters;

public class PrefixoTabelaFilter extends TabelaSchemaFilter{

	private String prefixo;

	private PrefixoTabelaFilter(String prefixo){
		this.prefixo = prefixo;
	}

	@Override
	public String getNome() {
		String nome = getProximo().getNome();
		if(nome.startsWith(prefixo))
			nome =  nome.substring(prefixo.length());
		return nome;
	}

	public static class Factory implements TabelaSchemaFilterFactory{

		private String prefixo;

		public Factory(String prefixo) {
			super();
			this.prefixo = prefixo;
		}

		@Override
		public TabelaSchemaFilter getFilterInstance() {
			return new PrefixoTabelaFilter(prefixo);
		}
	}

}
