package br.com.cds.mobile.geradores.sqlparser;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public class SqlTabelaSchema implements TabelaSchema {


	private String nome;
	private Map<String,Class<?>> colunas = new HashMap<String, Class<?>>();

	/**
	 * @param schema Statement CREATE TABLE da tasbela em string
	 */
	public SqlTabelaSchema(String schema){
		// iniciando o parser
		CCJSqlParserManager manager = new CCJSqlParserManager();
		// iniciando o statement
		net.sf.jsqlparser.statement.Statement statement = null;
		try {
			statement = manager.parse(new StringReader(schema));
		} catch (JSQLParserException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		// Enviando um visitor para preencher os campos
		statement.accept(new CreateTableVisitor());
		// System.out.println(nome+" "+colunas.size()+" colunas");
	}

	private final class CreateTableVisitor implements StatementVisitor {

		@Override
		public void visit(CreateTable ct) {
			/*
			 * inserindo o nome da tabela aqui
			 */
			SqlTabelaSchema.this.nome = ct.getTable().getName();

			@SuppressWarnings("unchecked")
			List<ColumnDefinition> columnDefinitions = (List<ColumnDefinition>)ct.getColumnDefinitions();
			for(ColumnDefinition coluna :  columnDefinitions){
				String nomeColuna = coluna.getColumnName();
				Class<?> tipoColuna = classeJavaEquivalenteAoTipoSql(coluna.getColDataType().getDataType());
				/*
				 * inserindo as colunas, (nome, tipo) aqui 
				 */
				SqlTabelaSchema.this.colunas.put(nomeColuna,tipoColuna);
			}
		}

		private static final String MSG_ERRO = "Este metodo nao deveria ter sido chamado."+
				"Confira o schema da tabela se ha linhas que nao sao CREATE TABLE";
		@Override public void visit(Truncate arg0) { throw new RuntimeException(MSG_ERRO); }
		@Override public void visit(Drop arg0)     { throw new RuntimeException(MSG_ERRO); }
		@Override public void visit(Replace arg0)  { throw new RuntimeException(MSG_ERRO); }
		@Override public void visit(Insert arg0)   { throw new RuntimeException(MSG_ERRO); }
		@Override public void visit(Update arg0)   { throw new RuntimeException(MSG_ERRO); }
		@Override public void visit(Delete arg0)   { throw new RuntimeException(MSG_ERRO); }
		@Override public void visit(Select arg0)   { throw new RuntimeException(MSG_ERRO); }
	}

	public static Class<?> classeJavaEquivalenteAoTipoSql(String sqlType){
		/********************
		 * INT              *
		 * INTEGER          *
		 * TINYINT          *
		 * SMALLINT         *
		 * MEDIUMINT        *
		 * INT2             *
		 * INT8             *
		 *******************/
		if(
				sqlType.equalsIgnoreCase("int") ||
				sqlType.equalsIgnoreCase("integer") ||
				sqlType.equalsIgnoreCase("tinyint") ||
				sqlType.equalsIgnoreCase("smallint") ||
				sqlType.equalsIgnoreCase("mediumint") ||
				sqlType.equalsIgnoreCase("int2") ||
				sqlType.equalsIgnoreCase("int8")
		)
			return Long.class;
		/********************
		 * BIGINT           *
		 * UNSIGNED BIG INT *
		 *******************/

		if(
				sqlType.equalsIgnoreCase("bigint") ||
				sqlType.equalsIgnoreCase("unsiged big int")
		)
			return BigInteger.class;
		/**************************
		 * CHARACTER(20)          *
		 * VARCHAR(255)           *
		 * VARYING CHARACTER(255) *
		 * NCHAR(55)              *
		 * NATIVE CHARACTER(70)   *
		 * NVARCHAR(100)          *
		 * TEXT                   *
		 * CLOB                   *
		 *************************/
		if(
				sqlType.equalsIgnoreCase("text") ||
				sqlType.equalsIgnoreCase("clob") ||
				sqlType.matches("[\\s\\w]*[cC][hH][aA][rR].*")
		)
			return String.class;
		/********************
		 * REAL             *
		 * DOUBLE           *
		 * DOUBLE PRECISION *
		 * FLOAT            *
		 *******************/
		if(
				sqlType.equalsIgnoreCase("real") ||
				sqlType.equalsIgnoreCase("double") ||
				sqlType.equalsIgnoreCase("double precision") ||
				sqlType.equalsIgnoreCase("float")
		)
			return Double.class;
		/*****************
		 * NUMERIC       *
		 * DECIMAL(10,5) *
		 ****************/
		if(
				sqlType.equalsIgnoreCase("numeric") ||
				sqlType.matches("[\\w]*[d][D][e][E][c][C][i][I][m][M][a][A][l][L].*")
		)
			return BigDecimal.class;
		/************
		 * DATE     *
		 * DATETIME *
		 ***********/
		if(
				sqlType.equalsIgnoreCase("date") ||
				sqlType.equalsIgnoreCase("datetime")
		)
			return Date.class;
		/***********
		 * BOOLEAN *
		 **********/
		if(sqlType.equalsIgnoreCase("boolean"))
			return Boolean.class;
		/**********
		 * BLOB   *
		 * outros *
		 *********/
		throw new RuntimeException("DataType \'"+sqlType+"\' sem uma classe java correspodente.\n"+
				"---FIXME---@"+SqlTabelaSchema.class.getName()+"::"+
				new Object(){}.getClass().getEnclosingMethod().getName());
	}

	@Override
	public String getNome() {
		return nome;
	}

	@Override
	public Map<String, Class<?>> getColunas() {
		return new HashMap<String, Class<?>>(colunas);
	}

}
