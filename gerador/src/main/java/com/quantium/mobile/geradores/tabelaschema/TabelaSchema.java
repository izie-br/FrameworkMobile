package com.quantium.mobile.geradores.tabelaschema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoManyToMany;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoOneToOne;
import com.quantium.mobile.geradores.javabean.Constraint;

public class TabelaSchema {

	private String nome;
	private String className;
	private Collection<TabelaSchema.Coluna> colunas = new HashSet<TabelaSchema.Coluna>();
	private Collection<Associacao> associacoes = new HashSet<Associacao>();
	private boolean nonEntityTable = false;

	private TabelaSchema() {
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public static Builder criar(String nome) {
		TabelaSchema tabela = new TabelaSchema();
		tabela.nome = nome;
		return tabela.new Builder();
	}

	public class Builder {

		private Builder() {
			TabelaSchema.this.setNonEntityTable(false);
		}

		public Builder adicionarColuna(String nome, Class<?> type, Constraint... constraints) {
			if (constraints == null) {
				constraints = new Constraint[0];
			} else {
				for (int i = 0; i < constraints.length; i++) {
					if (constraints[i] == null) {
						throw new RuntimeException();
					}
				}
			}
			TabelaSchema.this.colunas.add(new Coluna(nome, type, constraints));
			return this;
		}

		public Builder adicionarColuna(String nome, Class<?> type, Constraint.Type... constraints) {
			Constraint constraintInstances[] = null;
			if (constraints != null) {
				constraintInstances = new Constraint[constraints.length];
				for (int i = 0; i < constraints.length; i++) {
					constraintInstances[i] = new Constraint(constraints[i]);
				}
			}
			TabelaSchema.this.colunas.add(new Coluna(nome, type, constraintInstances));
			return this;
		}

		public TabelaSchema get() {
			return TabelaSchema.this;
		}

		public Builder setNonEntityTable(boolean nonEntityTable) {
			TabelaSchema.this.setNonEntityTable(nonEntityTable);
			return this;
		}

		public Builder setClassName(String className) {
			TabelaSchema.this.setClassName(className);
			return this;
		}

		private TabelaSchema gerarAssociativa(String databaseTable, String colunaFrom, String colunaTo) {
			TabelaSchema.Builder tabelaBuilder = TabelaSchema.criar(databaseTable);
			tabelaBuilder
					.adicionarColuna(colunaFrom, Long.class, Constraint.Type.PRIMARY_KEY, Constraint.Type.NOT_NULL);
			tabelaBuilder.adicionarColuna(colunaTo, Long.class, Constraint.Type.PRIMARY_KEY, Constraint.Type.NOT_NULL);
			return tabelaBuilder.get();
		}

		// KeyTo = é o id na relacional. Exemplo: id_document, referencia é o id
		// na fonte: id;
		public Builder adicionarAssociacaoManyToMany(TabelaSchema tabelaA, TabelaSchema tabelaB, String referenciaA,
				String referenciaB) {
			String colunaFrom = CamelCaseUtils.camelToLowerAndUnderscores("id" + tabelaA.getClassName());
			String colunaTo = CamelCaseUtils.camelToLowerAndUnderscores("id" + tabelaB.getClassName());
			String tableName = CamelCaseUtils.camelToLowerAndUnderscores("tb" + tabelaA.getClassName() + "Join"
					+ tabelaB.getClassName());
			TabelaSchema.this.associacoes.add(new AssociacaoManyToMany(tabelaA, tabelaB, colunaFrom, colunaTo,
					referenciaA, referenciaB, gerarAssociativa(tableName, colunaFrom, colunaTo), tableName));
			return this;
		}

		public Builder adicionarAssociacaoOneToMany(TabelaSchema tabelaA, TabelaSchema tabelaB, boolean nullable,
				String referenciaA, String fkId) {
			System.out.println(String.format("tabelaA: %s - tabelaB: %s - Coluna: %s - Origem: %s", tabelaA.getNome(),
					tabelaB.getNome(), referenciaA, fkId));
			TabelaSchema.this.associacoes.add(new AssociacaoOneToMany(tabelaA, tabelaB, fkId, nullable, referenciaA));
			return this;
		}

		public Builder adicionarAssociacaoOneToOne(TabelaSchema tabelaA, TabelaSchema tabelaB, String keyToA,
				String keyToB) {
			TabelaSchema.this.associacoes.add(new AssociacaoOneToOne(tabelaA, tabelaB, keyToA, keyToB));
			return this;
		}

	}

	public Collection<Associacao> getAssociacoes() {
		return associacoes;
	}

	/**
	 * Nome da tabela
	 * 
	 * @return nome
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Colunas e tipos de dados de cada uma
	 * 
	 * @return Map com pares ( nome_da_coluna , classe_java )
	 */
	public Collection<TabelaSchema.Coluna> getColunas() {
		return new ArrayList<TabelaSchema.Coluna>(colunas);
	}

	public List<TabelaSchema.Coluna> getPrimaryKeys() {
		ArrayList<TabelaSchema.Coluna> keys = new ArrayList<TabelaSchema.Coluna>();
		for (TabelaSchema.Coluna coluna : colunas) {
			label_iterar_colunas: for (Constraint constraint : coluna.getConstraints()) {
				if (constraint.getType() == Constraint.Type.PRIMARY_KEY) {
					keys.add(coluna);
					break label_iterar_colunas;
				}
			}
		}
		return keys;
	}

	public class Coluna {

		private String nome;
		private Class<?> type;
		private Constraint constraints[];

		public Coluna(String nome, Class<?> type, Constraint... constraints) {
			this.nome = nome;
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

		public String getNome() {
			return nome;
		}

		public Class<?> getType() {
			return type;
		}

	}

	public void setNonEntityTable(boolean nonEntityTable) {
		this.nonEntityTable = nonEntityTable;
	}

	public boolean isNonEntityTable() {
		return nonEntityTable;
	}
}
