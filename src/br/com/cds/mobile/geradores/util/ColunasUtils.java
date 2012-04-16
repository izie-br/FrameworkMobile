package br.com.cds.mobile.geradores.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import br.com.cds.mobile.geradores.javabean.JavaBeanSchema;
import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;
import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema.Coluna;

public class ColunasUtils {


	private static final class ComparadorPorTipoENome implements
			Comparator<TabelaSchema.Coluna> {
		@Override
		public int compare(Coluna col1, Coluna col2) {
			if(!col1.getType().equals(col2.getType()))
				return col1.getType().getName().compareTo(col2.getType().getName());
			return col1.getNome().compareTo(col2.getNome());
		}
	}

	public static List<String> colunasOrdenadasDoJavaBeanSchema(
			JavaBeanSchema javaBeanSchema) {

		Set<TabelaSchema.Coluna> setOrdenado = 
			new TreeSet<TabelaSchema.Coluna>(
				new ComparadorPorTipoENome()
			);
		setOrdenado.addAll(javaBeanSchema.getTabela().getColunas());

		List<String> colunasEmOrdem = new ArrayList<String>(setOrdenado.size());
		for(TabelaSchema.Coluna coluna: setOrdenado)
			colunasEmOrdem.add(coluna.getNome());

		return colunasEmOrdem;

	}
}
