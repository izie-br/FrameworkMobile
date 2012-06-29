package com.quantium.mobile.geradores.filters.associacao;

import com.quantium.mobile.geradores.tabelaschema.TabelaSchema;

public class AssociacaoManyToMany extends Associacao {

	private AssociacaoOneToMany children[] = new AssociacaoOneToMany[2];
	private String nome;

	public AssociacaoManyToMany(
			TabelaSchema tabelaA, TabelaSchema tabelaB,
			String keyToA, String keyToB,
			String referenciaA, String referenciaB,
			TabelaSchema tabelaJuncao,
			String nome
	) {
		super(tabelaA,tabelaB);
		this.children[0] = new AssociacaoOneToMany(
			tabelaA, tabelaJuncao,
			keyToA, false,
			referenciaA
		);
		this.children[1] = new AssociacaoOneToMany(
			tabelaB, tabelaJuncao,
			keyToB, false,
			referenciaB
		);
		this.nome = nome;
	}

	public String getNome() {
		return nome;
	}

	public TabelaSchema getTabelaJuncao() {
		return children[0].getTabelaB();
	}

	public String getKeyToA(){
		return children[0].getKeyToA();
	}

	public String getKeyToB(){
		return children[1].getKeyToA();
	}

	public String getReferenciaA(){
		return children[0].getReferenciaA();
	}

	public String getReferenciaB(){
		return children[1].getReferenciaA();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((nome == null)? 0 : nome.hashCode());
		result = prime * result +
			// tem que ser igual se A e B sao trocados entre si
			((children[0] == null) ? 0 : children[0].hashCode()) +
			((children[1] == null) ? 0 : children[1].hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AssociacaoManyToMany other = (AssociacaoManyToMany) obj;
		if(
			(nome == null) ?
				(other.nome != null) :
				(!nome.equals(other.nome))
		) {
			return false;
		}
		if ( !(
			(
				children[0].equals( other.children[0]) &&
				children[1].equals( other.children[1])
			) || (
				children[0].equals( other.children[1]) &&
				children[1].equals( other.children[0])
			)
		)) {
			return false;
		}
		return true;
	}

}
