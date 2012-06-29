package com.quantium.mobile.geradores.filters.associacao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.quantium.mobile.geradores.filters.TabelaSchemaFilter;
import com.quantium.mobile.geradores.filters.TabelaSchemaFilterFactory;
import com.quantium.mobile.geradores.javabean.Propriedade;
import com.quantium.mobile.geradores.tabelaschema.TabelaSchema;


public class AssociacaoPorNomeFilter extends TabelaSchemaFilter {

	private static final String COLUMN_TABLE_NAME_REGEX = "\\w[\\w_\\d]*";
	private static final String SEPARATOR = "[,|]";
	private static final String MULTIPLE_COLUMN_TABLE_NAME_REGEX =
		COLUMN_TABLE_NAME_REGEX +
		"("+ SEPARATOR + COLUMN_TABLE_NAME_REGEX + ")*";
	private static final String TABLE_PLACEHOLDER_WITH_FIXED_NAMES =
		"\\{TABLE(=(" +MULTIPLE_COLUMN_TABLE_NAME_REGEX+ "))?\\}";
	private static final String COLUMN_PLACEHOLDER_REGEX_WITH_FIXED_NAMES =
		"\\{COLUMN(=(" +MULTIPLE_COLUMN_TABLE_NAME_REGEX+ "))?\\}";

	private static final int COLUMN_INDEX_IN_FORMAT_STRING = 2;
	private static final int TABLE_INDEX_IN_FORMAT_STRING = 1;
	private static final int PLACEHOLDER_FIXED_NAMES_GROUP = 2;


	private static final String ERRO_PADRAO_CHAVE_ESTRANGEIRA_SEM_PLACEHOLDER_MSG_FORMAT =
		"O padrao de chave estrangeira deve conter uma ocorrencia de %s";

	private AssociacoesResolver resolver;

	@Override
	public boolean isNonEntityTable(){
		for(Associacao a : resolver.getAssociacoes()){
			if(
				a instanceof AssociacaoManyToMany &&
				((AssociacaoManyToMany)a).getTabelaJuncao()
					.getNome().equals(
						super.getTabela().getNome()
					)
			) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Propriedade getPropriedade(String coluna) {
		Propriedade p = super.getPropriedade(coluna);
		ArrayList<String> assoc = new ArrayList<String>();
		for(Associacao associacao : resolver.getAssociacoes(getTabela()))
			if(associacao instanceof AssociacaoOneToMany)
				assoc.add(
						((AssociacaoOneToMany)associacao)
						.getKeyToA()
				);
		if(assoc.contains(coluna)){
			p.setSet(false);
			p.setGet(false);
		}
		return p;
	}

	/**
	 * Metodo criado para nao ocorrer recursao infinita em
	 * no metodo "mapear"do resolver
	 * @param coluna
	 * @return
	 */
	private String getPropriedadeNome(String coluna){
		return super.getPropriedade(coluna).getNome();
	}

	@Override
	public Collection<Associacao> getAssociacoes() {
		Collection<Associacao> associacoesTemUm = super.getAssociacoes();
		associacoesTemUm.addAll(resolver.getAssociacoes(getTabela()));
		return associacoesTemUm;
	}

	private static class AssociacoesResolver{

		private String pattern;
		private Collection<String> onlyTables;
		private Collection<String> onlyColumns;
		private Collection<Associacao> associacoes;
		private Collection<String> mapeadas;
		private Collection<AssociacaoPorNomeFilter> filtros =
				new ArrayList<AssociacaoPorNomeFilter>();

		private Collection<Associacao> getAssociacoes(){
			validar();
			return associacoes;
		}

		private Collection<Associacao> getAssociacoes(TabelaSchema tabela){
			Collection<Associacao> associacoesTemUm = new ArrayList<Associacao>();
			for(Associacao associacao : getAssociacoes())
				if(
						associacao.getTabelaA().equals(tabela) ||
						associacao.getTabelaB().equals(tabela)
				)
					associacoesTemUm.add(associacao);
			return associacoesTemUm;
		}

		public void validar() {
			if(associacoes==null||mapeadas==null)
				mapear();
			for(AssociacaoPorNomeFilter filtro :filtros)
				if(!mapeadas.contains(filtro.getTabela().getNome()))
					mapear();
		}

		private void mapear(){
			associacoes = new ArrayList<Associacao>();
			mapeadas = new ArrayList<String>();
			for(AssociacaoPorNomeFilter filtroTabelaA :filtros){
				for(AssociacaoPorNomeFilter filtroTabelaB : filtros){
					if (onlyTables!=null && !onlyTables.contains(filtroTabelaB.getNome()))
						continue;
					for(TabelaSchema.Coluna colunaA : filtroTabelaA.getTabela().getColunas()){
						if (onlyColumns !=null && !onlyColumns.contains(colunaA.getNome()))
							continue;
						Object args[] = new String[2];
						args[TABLE_INDEX_IN_FORMAT_STRING-1] = filtroTabelaA.getNome();
						args[COLUMN_INDEX_IN_FORMAT_STRING-1] = filtroTabelaA.getPropriedadeNome(colunaA.getNome());
						// exemplo: colunaToA = "id_outratbela"
						String colunaToA = String.format(pattern,args);
						for(TabelaSchema.Coluna coluna : filtroTabelaB.getTabela().getColunas()){
							if(
									filtroTabelaB.getPropriedadeNome(coluna.getNome())
										.equals(colunaToA)
							){
								boolean nullable = ! Arrays.asList(coluna.getConstraints())
									.contains(TabelaSchema.NOT_NULL_CONSTRAINT);
								try{
								Associacao associacao = new AssociacaoOneToMany(
										filtroTabelaA.getTabela(),
										filtroTabelaB.getTabela(),
										colunaToA,
										nullable,
										colunaA.getNome()
								);
								associacoes.add(associacao);
								System.out.println(associacao);
								}catch (RuntimeException e){
									System.err.println("Erro na tabela "+filtroTabelaA.getNome());
									System.err.println("Relacao com "+filtroTabelaB.getNome()+" "+colunaToA);
									System.err.println(colunaToA);
									throw e;
								}
								break;
							} // endif(filtroTabelaB.getPropriedadeNome(coluna.getNome()))
						} // endfor(TabelaSchema.Coluna coluna : filtroTabelaB.getTabela().getColunas())
					} // endfor(TabelaSchema.Coluna colunaA : filtroTabelaA.getTabela().getColunas())
				} // endfor(AssociacaoPorNomeFilter filtroTabelaB : filtros)
				mapeadas.add(filtroTabelaA.getTabela().getNome());
			} // endfor(AssociacaoPorNomeFilter filtroTabelaA :filtros)
			mapManyToManyRelationships();
		}

		private void mapManyToManyRelationships(){
			ArrayList<Associacao> associationsCopy = new ArrayList<Associacao>(associacoes);
			for(Associacao associationA : associationsCopy){
				TabelaSchema table = associationA.getTabelaB();
				if(
					!(associationA instanceof AssociacaoOneToMany) ||
					table.getColunas().size()>3
				){
					continue;
				}
				AssociacaoPorNomeFilter filter = null;
				for(AssociacaoPorNomeFilter f : filtros){
					if(f.getTabela().equals(table)){
						filter = f;
						break;
					}
				}
				for(Associacao associationB : associationsCopy){
					if(
						!associationB.getTabelaB().equals(table) ||
						!(associationB instanceof AssociacaoOneToMany)
					){
						continue;
					}
					boolean isManyToMany = true;
					for(TabelaSchema.Coluna coluna : table.getColunas()){
						if(!(
							filter.getPropriedadeNome(coluna.getNome()).equals(
								((AssociacaoOneToMany)associationA).getKeyToA()
							) ||
							filter.getPropriedadeNome(coluna.getNome()).equals(
								((AssociacaoOneToMany)associationB).getKeyToA()
							) || (
								table.getPrimaryKeys().size()==1 &&
								table.getPrimaryKeys().get(0).equals(coluna)
							)
						)){
							isManyToMany = false;
						}
					}
					if(isManyToMany){
						associacoes.remove(associationA);
						associacoes.remove(associationB);
						AssociacaoManyToMany manyToMany =
							new AssociacaoManyToMany(
								associationA.getTabelaA(),
								associationB.getTabelaA(),
								((AssociacaoOneToMany)associationA).getKeyToA(),
								((AssociacaoOneToMany)associationB).getKeyToA(),
								((AssociacaoOneToMany)associationA).getReferenciaA(),
								((AssociacaoOneToMany)associationB).getReferenciaA(),
								table,
								""
							);
						if (!associacoes.contains(manyToMany))
							associacoes.add(manyToMany);
					}
				} // for(Associacao associationB : associationsCopy)
			} // for(Associacao associationA : associationsCopy)
		}

	}


	public static class Factory implements TabelaSchemaFilterFactory{

		private AssociacoesResolver resolver;

		public Factory(String pattern) {
			Collection<String> onlyTables = extractFixedNames(
					pattern,
					TABLE_PLACEHOLDER_WITH_FIXED_NAMES
			);
			Pattern pat = Pattern.compile(TABLE_PLACEHOLDER_WITH_FIXED_NAMES);
			Matcher mobj = pat.matcher(pattern);
			mobj.find();
			pattern =
					pattern.substring(0, mobj.start())+
					"%"+TABLE_INDEX_IN_FORMAT_STRING+"$s"+
					pattern.substring(mobj.end());

			Collection<String> onlyCollumns = extractFixedNames(
					pattern,
					COLUMN_PLACEHOLDER_REGEX_WITH_FIXED_NAMES
			);
			pat = Pattern.compile(COLUMN_PLACEHOLDER_REGEX_WITH_FIXED_NAMES);
			mobj = pat.matcher(pattern);
			mobj.find();
			pattern =
					pattern.substring(0, mobj.start())+
					"%"+COLUMN_INDEX_IN_FORMAT_STRING+"$s"+
					pattern.substring(mobj.end());

			resolver = new AssociacoesResolver();
			resolver.pattern = pattern;
			resolver.onlyColumns = onlyCollumns;
			resolver.onlyTables = onlyTables;
		}

		protected Collection<String> extractFixedNames(String pattern, String placeholder) {
			Collection<String> fixedNames = null;
			Pattern tableRegex = Pattern.compile(placeholder);
			Matcher mobj = tableRegex.matcher(pattern);
			if(!mobj.find())
				throw new RuntimeException( String.format (
						ERRO_PADRAO_CHAVE_ESTRANGEIRA_SEM_PLACEHOLDER_MSG_FORMAT,
						placeholder
				));
			if( mobj.group(PLACEHOLDER_FIXED_NAMES_GROUP)!=null ){
				fixedNames = new HashSet<String>();
				String[] names = mobj.group(PLACEHOLDER_FIXED_NAMES_GROUP).split(SEPARATOR);
				fixedNames.addAll(Arrays.asList(names));
			}
			return fixedNames;
		}

		@Override
		public TabelaSchemaFilter getFilterInstance() {
			AssociacaoPorNomeFilter filtro = new AssociacaoPorNomeFilter();
			// associando os filtros ao resolver bidirecionalmente
			resolver.filtros.add(filtro);
			filtro.resolver = this.resolver;
			return filtro;
		}
		
	}

}
