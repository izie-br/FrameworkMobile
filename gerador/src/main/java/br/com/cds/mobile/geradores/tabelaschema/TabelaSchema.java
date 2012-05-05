package br.com.cds.mobile.geradores.tabelaschema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class TabelaSchema {

	public static final String PRIMARY_KEY_CONSTRAINT = "PRIMARY KEY";
	public static final String UNIQUE_CONSTRAINT = "UNIQUE";
	public static final String FOREIGN_KEY_CONSTRAINT = "FOREIGN KEY";
	public static final String NOT_NULL_CONSTRAINT = "NOT NULL";
	public static final String DEFAULT_CONSTRAINT = "DEFAULT";
	public static final String CHECK_CONSTRAINT = "CHECK";


	private String nome;
	private Collection<TabelaSchema.Coluna> colunas = new HashSet<TabelaSchema.Coluna>();

	private TabelaSchema(){}

	public static Builder criar(String nome){
		TabelaSchema tabela = new TabelaSchema();
		tabela.nome = nome;
		return tabela.new Builder();
	}

	public class Builder{

		private Builder(){}

		public Builder adicionarColuna(String nome, Class<?> type, String...constraints){
			TabelaSchema.this.colunas.add(new Coluna(nome, type,constraints));
			return this;
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

	public List<TabelaSchema.Coluna> getPrimaryKeys(){
		ArrayList<TabelaSchema.Coluna> keys =
				new ArrayList<TabelaSchema.Coluna>();
		for(TabelaSchema.Coluna coluna : colunas){
			label_iterar_colunas:
			for(String constraint : coluna.getConstraints()){
				if(constraint.equalsIgnoreCase(PRIMARY_KEY_CONSTRAINT)){
					keys.add(coluna);
					break label_iterar_colunas;
				}
			}
		}
		return keys;
	}

	public class Coluna{

		private String nome;
		private Class<?> type;
		private String constraints[];

		public Coluna(String nome, Class<?> type, String...constraints){
			this.nome = nome;
			this.type = type;
			this.constraints = constraints;
		}

		public String [] getConstraints() {
			if(constraints==null || constraints.length ==0)
				return new String[0];
			String copy [] = new String[constraints.length];
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

}
