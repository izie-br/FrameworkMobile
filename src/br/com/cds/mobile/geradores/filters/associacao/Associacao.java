package br.com.cds.mobile.geradores.filters.associacao;

import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;

public abstract class Associacao{

	protected static final String ERRO_ARGUMENTOS_NULL_MSG =
		Associacao.class.getSimpleName() +
		":: Nenhum dos argumentos do contrutor pode ser null";

	private TabelaSchema tabelaA;
	private TabelaSchema tabelaB;

	public Associacao(TabelaSchema tabelaA, TabelaSchema tabelaB) {
		if(tabelaA==null||tabelaB==null)
			throw new RuntimeException(ERRO_ARGUMENTOS_NULL_MSG);
		this.tabelaA = tabelaA;
		this.tabelaB = tabelaB;
	}

	@Override
	public String toString() {
		return	tabelaA.getNome() + " " +
			getClass().getSimpleName() + " " +
			tabelaB.getNome();
	}

	public TabelaSchema getTabelaA() {
		return tabelaA;
	}

	public TabelaSchema getTabelaB() {
		return tabelaB;
	}

	@Override
	public int hashCode() {
		return	tabelaB.hashCode() +
				tabelaA.hashCode() ;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Associacao other = (Associacao) obj;
		if (!tabelaB.equals(other.tabelaB))
			return false;
		if (!tabelaA.equals(other.tabelaA))
			return false;
		return true;
	}

}
