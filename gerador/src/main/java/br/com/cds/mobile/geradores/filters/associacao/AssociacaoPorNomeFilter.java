package br.com.cds.mobile.geradores.filters.associacao;

import java.util.ArrayList;
import java.util.Collection;

import br.com.cds.mobile.geradores.filters.TabelaSchemaFilter;
import br.com.cds.mobile.geradores.filters.TabelaSchemaFilterFactory;
import br.com.cds.mobile.geradores.javabean.Propriedade;
import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;

public class AssociacaoPorNomeFilter extends TabelaSchemaFilter {

	private static final String PLACEHOLDER = "${TABELA}";
	private static final String ERRO_PADRAO_CHAVE_ESTRANGEIRA_SEM_PLACEHOLDER_MSG =
			"O padrao de chave estrangeira deve conter uma ocorrencia de "+PLACEHOLDER;

	private AssociacoesResolver resolver;

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
		private Collection<Associacao> associacoes;
		private Collection<String> mapeadas;
		private Collection<AssociacaoPorNomeFilter> filtros =
				new ArrayList<AssociacaoPorNomeFilter>();

		private Collection<Associacao> getAssociacoes(TabelaSchema tabela){
			validar();
			Collection<Associacao> associacoesTemUm = new ArrayList<Associacao>();
			for(Associacao associacao : associacoes)
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

		// TODO mapear one-to-one
		private void mapear(){
			associacoes = new ArrayList<Associacao>();
			mapeadas = new ArrayList<String>();
			for(AssociacaoPorNomeFilter filtroTabelaA :filtros){
				String colunaToA = pattern.replace(PLACEHOLDER,filtroTabelaA.getNome());
				for(AssociacaoPorNomeFilter filtroTabelaB : filtros){
					for(TabelaSchema.Coluna coluna : filtroTabelaB.getTabela().getColunas()){
						if(
								filtroTabelaB.getPropriedadeNome(coluna.getNome())
									.equals(colunaToA)
						){
							try{
							Associacao associacao = new AssociacaoOneToMany(
									filtroTabelaA.getTabela(),
									filtroTabelaB.getTabela(),
									colunaToA,
									filtroTabelaA.getTabela().getPrimaryKey().getNome()
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
						}
					}
				}
				mapeadas.add(filtroTabelaA.getTabela().getNome());
			}
		}

	}


	public static class Factory implements TabelaSchemaFilterFactory{

		private AssociacoesResolver resolver;

		public Factory(String pattern) {
			if(pattern==null||!pattern.contains(PLACEHOLDER))
				throw new RuntimeException(ERRO_PADRAO_CHAVE_ESTRANGEIRA_SEM_PLACEHOLDER_MSG);
			resolver = new AssociacoesResolver();
			resolver.pattern = pattern;
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
