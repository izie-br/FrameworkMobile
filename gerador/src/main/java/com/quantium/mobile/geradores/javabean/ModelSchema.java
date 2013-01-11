package com.quantium.mobile.geradores.javabean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.quantium.mobile.geradores.filters.associacao.Associacao;

public final class ModelSchema {

	private String name;
	private Collection<ModelSchema.Column> colunas = new HashSet<ModelSchema.Column>();
	private Collection<Constraint> constraints = new ArrayList<Constraint>();
	private Collection<Associacao> associacoes = new HashSet<Associacao>();
	private boolean nonEntityTable = false;

	private ModelSchema() {}

	public static Builder criar(String name) {
		ModelSchema tabela = new ModelSchema();
		tabela.name = name;
		return tabela.new Builder();
	}

	public final class Builder {

		private Builder() {
			ModelSchema.this.nonEntityTable = false;
		}

		public Builder addColumn(String nome, Class<?> type, Constraint... constraints) {
			ModelSchema.this.colunas.add(new Column(nome, type, constraints));
			return this;
		}

		public Builder addConstraint(Constraint constraint) {
			ModelSchema.this.constraints.add(constraint);
			return this;
		}

		public ModelSchema get() {
			return ModelSchema.this;
		}

		public Builder setNonEntityTable(boolean nonEntityTable) {
			ModelSchema.this.nonEntityTable = nonEntityTable;
			return this;
		}

	}

	public boolean isNonEntityTable() {
		return nonEntityTable;
	}

	public Collection<Associacao> getAssociacoes() {
		return associacoes;
	}

	/**
	 * Nome da tabela
	 * 
	 * @return nome
	 */
	public String getName() {
		return name;
	}

	/**
	 * Colunas e tipos de dados de cada uma
	 * 
	 * @return Map com pares ( nome_da_coluna , classe_java )
	 */
	public Collection<ModelSchema.Column> getColunas() {
		return new ArrayList<ModelSchema.Column>(colunas);
	}

	public ModelSchema.Column getPrimaryKey() {
		for (ModelSchema.Column coluna : colunas) {
			for (Constraint constraint : coluna.getConstraints()) {
				if (constraint.getType() == Constraint.Type.PRIMARY_KEY) {
					return coluna;
				}
			}
		}
		throw new RuntimeException("Chave primaria nao encontrada");
	}

	public final class Column {

		private String name;
		private Class<?> type;
		private Constraint constraints[];

		public Column(String name, Class<?> type, Constraint... constraints) {
			this.name = name;
			this.type = type;
			this.constraints = constraints;
		}

		public Constraint[] getConstraints() {
			if (constraints == null || constraints.length == 0)
				return new Constraint[0];
			Constraint copy[] = new Constraint[constraints.length];
			System.arraycopy(constraints, 0, copy, 0, copy.length);
			return copy;
		}

		public String getName() {
			return name;
		}

		public Class<?> getType() {
			return type;
		}

	}

}
