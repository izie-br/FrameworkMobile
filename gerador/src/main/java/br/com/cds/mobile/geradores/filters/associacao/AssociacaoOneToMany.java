package br.com.cds.mobile.geradores.filters.associacao;

import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;

public class AssociacaoOneToMany extends Associacao{

	private String keyToA;
	private String referenciaA;

	public AssociacaoOneToMany(
			TabelaSchema tabelaA, TabelaSchema tabelaB,
			String keyToA, String referenciaA
	) {
		super(tabelaA, tabelaB);
		if(keyToA == null || referenciaA == null)
			throw new RuntimeException(Associacao.ERRO_ARGUMENTOS_NULL_MSG);
		this.keyToA = keyToA;
		this.referenciaA = referenciaA;
	}

	public String getKeyToA() {
		return keyToA;
	}
	public String getReferenciaA() {
		return referenciaA;
	}

	@Override
	public int hashCode() {
		return	super.hashCode()  +
			keyToA.hashCode() +
			referenciaA.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssociacaoOneToMany other = (AssociacaoOneToMany) obj;
		if (!keyToA.equals(other.keyToA))
			return false;
		if (!referenciaA.equals(other.referenciaA))
			return false;
		if (!getTabelaA().equals(other.getTabelaA()))
			return false;
		if (!getTabelaB().equals(other.getTabelaB()))
			return false;
		return true;
	}


}
