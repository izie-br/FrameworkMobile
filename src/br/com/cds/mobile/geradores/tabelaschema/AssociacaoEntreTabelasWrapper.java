package br.com.cds.mobile.geradores.tabelaschema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AssociacaoEntreTabelasWrapper implements TabelaSchema{

	private TabelaSchema tabelaDecorada;
	private String foreignKeyFormat;
	private Collection<TabelaSchema> hasMany = new HashSet<TabelaSchema>();
	private Collection<TabelaSchema> hasOne  = new HashSet<TabelaSchema>();

	private AssociacaoEntreTabelasWrapper(TabelaSchema tabelaDecorada,String foreignKeyFormat){
		this.tabelaDecorada = tabelaDecorada;
		this.foreignKeyFormat = foreignKeyFormat;
	}

	@Override
	public String getNome() {
		return tabelaDecorada.getNome();
	}

	@Override
	public Map<String, Class<?>> getColunas() {
		Map<String,Class<?>> colunas = new HashMap<String, Class<?>>(tabelaDecorada.getColunas());
		for(TabelaSchema schema: hasOne){
			String colunaChaveEstrangeira = String.format(foreignKeyFormat, schema.getNome());
			if(colunas.keySet().contains(colunaChaveEstrangeira))
				colunas.remove(colunaChaveEstrangeira);
		}
		return colunas;
	}

	public Collection<TabelaSchema> getTabelasHasMany(){
		return new ArrayList<TabelaSchema>(hasMany);
	}

	public Collection<TabelaSchema> getTabelasHasOne(){
		return new ArrayList<TabelaSchema>(hasOne);
	}


	public static class Mapeador{

		private String colunaId = "id";
		private String foreignKeyFormat = "id_%s";

		public Mapeador setColunaId(String coluna){
			this.colunaId = coluna;
			return this;
		}

		/**
		 * Formato de string (estilo printf) com uma ocorrencia de <b>%s</b>
		 * para inseir o nome da tabela estrangeira.
		 * <p>Padrao: "id_%s"</p>
		 * @param formato string de formato (estilo printf) com 
		 * @return
		 */
		public Mapeador setPadraoDoNomeDaForeignKey(String formato){
			if(formato.contains("%s"))
				this.foreignKeyFormat = formato;
			throw new RuntimeException(String.format("String de formato ilegal: %s",formato));
		}

		public Collection<AssociacaoEntreTabelasWrapper> mapear(Collection<TabelaSchema> schemas){
			ArrayList<AssociacaoEntreTabelasWrapper> out = new ArrayList<AssociacaoEntreTabelasWrapper>();
			// decorando todos schemas de entrada
			for(TabelaSchema schema : schemas)
				out.add(new AssociacaoEntreTabelasWrapper(schema,this.foreignKeyFormat));
			for(AssociacaoEntreTabelasWrapper tabela1 : out) for(AssociacaoEntreTabelasWrapper tabela2 : out){
				if(isPrimeiraReferenciaSegunda(tabela1, tabela2)){
					if(isPrimeiraReferenciaSegunda(tabela2, tabela1)){
						// se ambas tabelas se referenciam, ha um relacionamento um-a-um
						tabela1.hasOne.add(tabela2);
						tabela2.hasOne.add(tabela1);
					}
					else{
						// se a primeira referencia a segunda, mas a segunda nao
						// a segunda pode ter varios items da primeira
						tabela1.hasOne.add(tabela2);
						tabela2.hasMany.add(tabela1);
					}
				}
			}
			return out;
		}

		private boolean isPrimeiraReferenciaSegunda(TabelaSchema tabela1,TabelaSchema tabela2){
			Map<String,Class<?>> colunasTabela1 = tabela1.getColunas();
			String colunaAB = String.format(this.foreignKeyFormat, tabela2.getNome());
			return colunasTabela1.keySet().contains(colunaAB);
		}

	}

}
