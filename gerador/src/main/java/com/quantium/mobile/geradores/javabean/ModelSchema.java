package com.quantium.mobile.geradores.javabean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.quantium.mobile.geradores.filters.associacao.Associacao;

public final class ModelSchema {

	private String name;
	private Collection<Property> colunas = new HashSet<Property>();
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

		public Builder addProperty(String nome, Class<?> type,
		                           Constraint... constraints)
		{
			boolean isPrimaryKey = false;
			if (constraints != null) for (Constraint constraint :constraints) {
				if (constraint.getType() == Constraint.Type.PRIMARY_KEY){
					isPrimaryKey = true;
					break;
				}
			}
			ModelSchema.this.colunas.add(
					new Property(nome, type, true, !isPrimaryKey, constraints));
			return this;
		}

		public Builder addProperty(String nome, Class<?> type,
		                           Constraint.Type... constraints)
		{
			Constraint constraintImpl [] = new Constraint[constraints.length];
			for (int i=0; i< constraints.length; i++){
				constraintImpl[i] = new Constraint(constraints[i]);
			}
			addProperty(nome, type, constraintImpl);
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
	public Collection<Property> getProperties() {
		return new ArrayList<Property>(colunas);
	}

	public Property getPrimaryKey() {
		for (Property coluna : colunas) {
			for (Constraint constraint : coluna.getConstraints()) {
				if (constraint.getType() == Constraint.Type.PRIMARY_KEY) {
					return coluna;
				}
			}
		}
		throw new RuntimeException("Chave primaria nao encontrada");
	}
}
