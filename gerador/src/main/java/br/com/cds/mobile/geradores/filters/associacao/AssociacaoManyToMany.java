package br.com.cds.mobile.geradores.filters.associacao;

import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;

public class AssociacaoManyToMany extends AssociacaoOneToMany {

	private String nome;
	private String tabelaJuncao;

	public AssociacaoManyToMany(
			TabelaSchema tabelaA, TabelaSchema tabelaB,
			String keyToA, String keyToB,
			String nome, String tabelaJuncao
	) {
		super(tabelaA, tabelaB,keyToA,keyToB);
		this.nome = nome;
		this.tabelaJuncao = tabelaJuncao;
	}

	public String getNome() {
		return nome;
	}

	public String getTabelaJuncao() {
		return tabelaJuncao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());
		result = prime * result
				+ ((tabelaJuncao == null) ? 0 : tabelaJuncao.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssociacaoManyToMany other = (AssociacaoManyToMany) obj;
		if (nome == null) {
			if (other.nome != null)
				return false;
		} else if (!nome.equals(other.nome))
			return false;
		if (tabelaJuncao == null) {
			if (other.tabelaJuncao != null)
				return false;
		} else if (!tabelaJuncao.equals(other.tabelaJuncao))
			return false;
		return true;
	}

	

}
