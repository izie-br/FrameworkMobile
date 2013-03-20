package com.quantium.mobile.geradores.javabean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.filters.associacao.Associacao;

/**
 * 
 * @author Igor Soares
 *
 */
public final class ModelSchema {

	private final String name;
	private final String module;
	private final Collection<Property> colunas =
			new HashSet<Property>();
	private final Collection<Constraint> constraints =
			new ArrayList<Constraint>();
	private final Collection<Associacao> associacoes =
			new HashSet<Associacao>();

	/**
	 * @see ModelSchema#create(String, String)
	 * @param module
	 * @param name
	 */
	private ModelSchema(String module, String name) {
		this.module = module;
		this.name = name;
	}

	/**
	 * Instancia um {@link Builder}
	 * 
	 * @param module
	 * @param name
	 * @return builder
	 */
	public static Builder create(String module, String name) {
		ModelSchema tabela = new ModelSchema(module, name);
		return tabela.new Builder();
	}

	public final class Builder {

		/**
		 * @see ModelSchema#create(String, String)
		 */
		private Builder() {}

		/**
		 * Adiciona uma propriedade, com o nome e tipo, ao {@link ModelSchema}.
		 * Opcionalmente, adiciona-se as {@link Constraint}'s a esta
		 * propriedade.
		 *
		 * @param name
		 * @param type
		 * @param constraints
		 * @return this
		 */
		public Builder addProperty(String name, Class<?> type,
		                           Constraint... constraints)
		{
			boolean isPrimaryKey = false;
			if (constraints != null) {
				for (Constraint constraint :constraints) {
					if (constraint instanceof Constraint.PrimaryKey ) {
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

		/**
		 * Adiciona constraint.
		 * 
		 * @param constraint
		 * @return this
		 */
		public Builder addConstraint(Constraint constraint) {
			ModelSchema.this.constraints.add(constraint);
			return this;
		}

		/**
		 * Adiciona associacao one-to-many, many-to-one ou many-to-many.
		 * 
		 * @param associcacao
		 * @return this
		 */
		public Builder addAssociation (Associacao assoc) {
			ModelSchema.this.associacoes.add (assoc);
			return this;
		}

		/**
		 * Retorna o {@link ModelSchema} em criacao.
		 * 
		 * @return modelSchema
		 */
		public ModelSchema get() {
			return ModelSchema.this;
		}

	}

	/**
	 * Retorna todas associacoes one-to-many, many-to-one e many-to-many.
	 * 
	 * @return associations (unmodifiable collection)
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
	 * Nome do modulo
	 * 
	 * @return module name
	 */
	public String getModule () {
		return module;
	}

	/**
	 * Nome da classe da entidade
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Todas colunas, em classe {@link Property}
	 * 
	 * @return properties
	 */
	public Collection<Property> getProperties() {
		return Collections.unmodifiableCollection (colunas);
	}

	/**
	 * {@link Property} da chave primaria
	 * 
	 * @return chave primaria
	 */
	public Property getPrimaryKey() {
		for (Property coluna : colunas) {
			for (Constraint constraint : coluna.getConstraints()) {
				if (constraint instanceof Constraint.PrimaryKey) {
					return coluna;
				}
			}
		}
		throw new RuntimeException("Chave primaria nao encontrada");
	}
}
