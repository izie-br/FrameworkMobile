package com.quantium.mobile.geradores.javabean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.filters.associacao.Associacao;

public final class ModelSchema {

	private final String name;
	private final String module;
	private final Collection<Property> colunas =
			new HashSet<Property>();
	private final Collection<Constraint> constraints =
			new ArrayList<Constraint>();
	private final Collection<Associacao> associacoes =
			new HashSet<Associacao>();

	private ModelSchema(String module, String name) {
		this.module = module;
		this.name = name;
	}

	public static Builder create(String module, String name) {
		ModelSchema tabela = new ModelSchema(module, name);
		return tabela.new Builder();
	}

	public final class Builder {

		private Builder() {}

		public Builder addProperty(String name, Class<?> type,
		                           Constraint... constraints)
		{
			boolean isPrimaryKey = false;
			if (constraints != null) {
				for (Constraint constraint :constraints) {
					if (constraint.isTypeOf (Constraint.PRIMARY_KEY)) {
						isPrimaryKey = true;
						break;
					}
				}
			}
			ModelSchema.this.colunas.add(
					new Property(name, type, true, !isPrimaryKey,
					             constraints));
			return this;
		}

		public Builder addProperty(String name, Class<?> type,
		                           Constraint.Type... constraints)
		{
			Constraint constraintImpl [] = null;
			if (constraints != null){
				constraintImpl= new Constraint[constraints.length];
				for (int i=0; i< constraints.length; i++){
					constraintImpl[i] = new Constraint(constraints[i]);
				}
			}
			addProperty(name, type, constraintImpl);
			return this;
		}

		public Builder addConstraint(Constraint constraint) {
			ModelSchema.this.constraints.add(constraint);
			return this;
		}

		public Builder addAssociation (Associacao assoc) {
			ModelSchema.this.associacoes.add (assoc);
			return this;
		}

		public ModelSchema get() {
			return ModelSchema.this;
		}

	}

	/**
	 * 
	 * @return association (unmodifiable collection)
	 */
	public Collection<Associacao> getAssociacoes() {
		return Collections.unmodifiableCollection (associacoes);
	}

	/**
	 * 
	 * @return constraints (unmodifiable collection)
	 */
	public Collection<Constraint> getConstraints () {
		return Collections.unmodifiableCollection (constraints);
	}

	/**
	 * Nome do module
	 * @return module name
	 */
	public String getModule () {
		return module;
	}

	/**
	 * Nome do modelo
	 * 
	 * @return name
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
		return Collections.unmodifiableCollection (colunas);
	}

	public Property getPrimaryKey() {
		for (Property coluna : colunas) {
			for (Constraint constraint : coluna.getConstraints()) {
				if (constraint.isTypeOf(Constraint.PRIMARY_KEY)) {
					return coluna;
				}
			}
		}
		throw new RuntimeException("Chave primaria nao encontrada");
	}
}
