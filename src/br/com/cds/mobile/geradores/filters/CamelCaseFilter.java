package br.com.cds.mobile.geradores.filters;

import br.com.cds.mobile.geradores.javabean.Propriedade;
import br.com.cds.mobile.geradores.util.CamelCaseUtils;

public class CamelCaseFilter extends TabelaSchemaFilter{

	@Override
	public String getNome() {
		return CamelCaseUtils.toUpperCamelCase(super.getNome());
	}

	public Propriedade getPropriedade(String coluna) {
		Propriedade p=  super.getPropriedade(coluna);
		return new Propriedade(
				CamelCaseUtils.tolowerCamelCase(p.getNome()),
				p.getType(),
				p.isGet(),
				p.isSet()
		);
	}

	@Override
	public Propriedade getPrimaryKey() {
		Propriedade p=  super.getPrimaryKey();
		return new Propriedade(
				CamelCaseUtils.tolowerCamelCase(p.getNome()),
				p.getType(),
				p.isGet(),
				p.isSet()
		);

	}

	@Override
	public String getConstante(String coluna) {
		return CamelCaseUtils.camelToUpper(super.getConstante(coluna));
	}

	public static class Factory implements TabelaSchemaFilterFactory{
		@Override
		public TabelaSchemaFilter getFilterInstance() {
			return new CamelCaseFilter();
		}
	}

}
