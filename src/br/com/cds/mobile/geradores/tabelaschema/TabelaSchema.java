package br.com.cds.mobile.geradores.tabelaschema;

import java.util.Collection;
import java.util.Map;

public interface TabelaSchema {

	/**
	 * Nome da classe
	 * @return nome
	 */
	String getNome();

	/**
	 * Nome da tabela
	 * @return
	 */
	String getTabela();

	/**
	 * Colunas e tipos de dados de cada uma
	 * @return Map com pares ( nome_da_coluna , classe_java )
	 */
	Collection<TabelaSchema.Coluna> getColunas();

	/**
	 * Propriedades e tipos de dados de cada uma
	 * @return Map com pares ( nome_da_propriedade , classe_java )
	 */
	Map<String, Coluna> getPropriedades();

	class Coluna{

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