package br.com.cds.mobile.geradores.tabelaschema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AssociacaoEntreTabelasWrapper implements TabelaSchema{

	private TabelaSchema tabela;
	private String foreignKeyFormat;
	private Collection<TabelaSchema> hasMany = new HashSet<TabelaSchema>();
	private Collection<TabelaSchema> hasOne  = new HashSet<TabelaSchema>();

	private AssociacaoEntreTabelasWrapper(TabelaSchema tabelaDecorada,String foreignKeyFormat){
		this.tabela = tabelaDecorada;
		this.foreignKeyFormat = foreignKeyFormat;
	}

	@Override
	public String getNome() {
		return tabela.getNome();
	}

	@Override
	public Map<String, TabelaSchema.Coluna> getPropriedades() {
		Map<String,TabelaSchema.Coluna> colunas =
				new HashMap<String, TabelaSchema.Coluna>(tabela.getPropriedades());
		// montando uma lista com todos os elementos de hasOne e hasMany
		ArrayList<TabelaSchema> todasAssociacoes = new ArrayList<TabelaSchema>(hasOne);
		todasAssociacoes.addAll(hasMany);
		// removendo todas essas colunas
		for(TabelaSchema schema: todasAssociacoes){
			String colunaChaveEstrangeira = String.format(foreignKeyFormat, schema.getNome());
			if(colunas.keySet().contains(colunaChaveEstrangeira)){
				colunas.remove(colunaChaveEstrangeira);
				System.out.println(colunaChaveEstrangeira+ "da classe "+getNome()+" removida");;
			}
		}
		return colunas;
	}

	@Override
	public String getTabela() {
		return tabela.getTabela();
	}

	@Override
	public Collection<TabelaSchema.Coluna> getColunas() {
		return tabela.getColunas();
	}

	public TabelaSchema getWrappedTabelaSchema(){
		return tabela;
	}

	public Collection<TabelaSchema> getTabelasHasMany(){
		return new ArrayList<TabelaSchema>(hasMany);
	}

	public Collection<TabelaSchema> getTabelasHasOne(){
		return new ArrayList<TabelaSchema>(hasOne);
	}


	public static class Mapeador{

		private String foreignKeyFormat = "id%s";

		/**
		 * Cria um mapeador com uma string de formato.
		 * String de formato no estilo printf, com uma ocorrencia de <b>%s</b>
		 * para inseir o nome da tabela estrangeira.
		 * <p>Padrao: "id%s"</p>
		 * @param formato string de formato (estilo printf) com 
		 * @return
		 */
		public static Mapeador mapeadorComPadraoDaForeignKey(String formato){
			Mapeador mapeador = new Mapeador();
			if(formato.contains("%s"))
				mapeador.foreignKeyFormat = formato;
			else
				throw new RuntimeException(String.format("String de formato ilegal: %s",formato));
			return mapeador;
		}

		/**
		 * Busca nas tabelas por relacoes entre as tabelas
		 * @param schemas tabelas
		 * @return tabelas com associacoes tracadas
		 */
		public Map<String,AssociacaoEntreTabelasWrapper> mapear(Collection<TabelaSchema> schemas){
			Map<String,AssociacaoEntreTabelasWrapper> out =
					new HashMap<String, AssociacaoEntreTabelasWrapper>();
			// decorando todos schemas de entrada
			for(TabelaSchema schema : schemas)
				out.put(
						schema.getNome(),
						new AssociacaoEntreTabelasWrapper(schema,this.foreignKeyFormat)
				);
			// buscando relacoes entre as tabelas
			for(AssociacaoEntreTabelasWrapper tabela1 : out.values())
				for(AssociacaoEntreTabelasWrapper tabela2 : out.values()){
					if(isPrimeiraReferenciaSegunda(tabela1, tabela2)){
						if(isPrimeiraReferenciaSegunda(tabela2, tabela1)){
							// se ambas tabelas se referenciam, ha um relacionamento um-a-um
							tabela1.hasOne.add(tabela2);
							tabela2.hasOne.add(tabela1);
						}
						else{
							// se a primeira referencia a segunda, mas nao o inverso
							// a segunda pode ter varios items da primeira
							tabela1.hasOne .add(tabela2);
							tabela2.hasMany.add(tabela1);
						}
					}
					else if(isPrimeiraReferenciaSegunda(tabela2, tabela1)){
						// se a segunda referencia a primera, mas nao o inverso
						tabela1.hasMany.add(tabela2);
						tabela2.hasOne .add(tabela1);
					}

			}
			return out;
		}

		private boolean isPrimeiraReferenciaSegunda(TabelaSchema tabela1,TabelaSchema tabela2){
			Map<String,TabelaSchema.Coluna> colunasTabela1 = tabela1.getPropriedades();
			String colunaAB = String.format(this.foreignKeyFormat, tabela2.getNome());
			return colunasTabela1.keySet().contains(colunaAB);
		}

	}
}

