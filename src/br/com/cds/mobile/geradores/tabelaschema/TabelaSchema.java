package br.com.cds.mobile.geradores.tabelaschema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class TabelaSchema {

	private String nome;
	private Collection<TabelaSchema.Coluna> colunas = new HashSet<TabelaSchema.Coluna>();

	private TabelaSchema(){}

	static Builder criar(String nome){
		TabelaSchema tabela = new TabelaSchema();
		tabela.nome = nome;
		return tabela.new Builder();
	}

	class Builder{

		public void adicionarColuna(String nome, Class<?> type){
			TabelaSchema.this.colunas.add(new Coluna(nome, type));
		}

		public TabelaSchema get(){
			return TabelaSchema.this;
		}

	}


	/**
	 * Nome da tabela
	 * @return nome
	 */
	public String getNome(){
		return nome;
	}

	/**
	 * Colunas e tipos de dados de cada uma
	 * @return Map com pares ( nome_da_coluna , classe_java )
	 */
	public Collection<TabelaSchema.Coluna> getColunas(){
		return new ArrayList<TabelaSchema.Coluna>(colunas);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colunas == null) ? 0 : colunas.hashCode());
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TabelaSchema other = (TabelaSchema) obj;
		if (colunas == null) {
			if (other.colunas != null)
				return false;
		} else if (!colunas.equals(other.colunas))
			return false;
		if (nome == null) {
			if (other.nome != null)
				return false;
		} else if (!nome.equals(other.nome))
			return false;
		return true;
	}

	public class Coluna{

		private String nome;
		private Class<?> type;

		public Coluna(String nome, Class<?> type){
			this.nome = nome;
			this.type = type;
		}


		public String getNome() {
			return nome;
		}

		public Class<?> getType() {
			return type;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((nome == null) ? 0 : nome.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Coluna other = (Coluna) obj;
			if (nome == null) {
				if (other.nome != null)
					return false;
			} else if (!nome.equals(other.nome))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

	}

}
