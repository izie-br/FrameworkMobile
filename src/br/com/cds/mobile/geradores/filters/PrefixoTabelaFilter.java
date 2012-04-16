package br.com.cds.mobile.geradores.filters;

public class PrefixoTabelaFilter extends TabelaSchemaFilter{

	private String prefixo;

	@Override
	public String getNome() {
		String nome = super.getNome();
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
			PrefixoTabelaFilter filtro = new PrefixoTabelaFilter();
			filtro.prefixo = this.prefixo;
			return filtro;
		}
	}

}
