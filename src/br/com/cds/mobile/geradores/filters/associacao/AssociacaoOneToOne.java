package br.com.cds.mobile.geradores.filters.associacao;

import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;

public class AssociacaoOneToOne extends Associacao{

	private String keyToA;
	private String keyToB;

	public AssociacaoOneToOne(TabelaSchema tabelaA, TabelaSchema tabelaB, String keyToA,
			String keyToB) {
		super(tabelaA,tabelaB);
		if( keyToA==null || keyToB==null)
			throw new RuntimeException(Associacao.ERRO_ARGUMENTOS_NULL_MSG);
		this.keyToA = keyToA;
		this.keyToB = keyToB;
	}

	public String getKeyToA() {
		return keyToA;
	}

	public String getKeyToB() {
		return keyToB;
	}

	@Override
	public int hashCode() {
		return	super.hashCode()  +
			keyToA.hashCode() +
			keyToB.hashCode() ;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssociacaoOneToOne other = (AssociacaoOneToOne) obj;
		if (!(
				(
						keyToA.equals(other.keyToA) &&
						keyToB.equals(other.keyToB)
				) || (
						keyToB.equals(other.keyToA) &&
						keyToA.equals(other.keyToB)
				)
		)){
			return false;
		}
		if (!(
				(
						getTabelaA().equals(other.getTabelaA()) &&
						getTabelaB().equals(other.getTabelaB())
				) || (
						getTabelaB().equals(other.getTabelaA()) &&
						getTabelaA().equals(other.getTabelaB())
				)
		)){
			return false;
		}
		return true;
	}

}
