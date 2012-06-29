package com.quantium.mobile.geradores.sqlparser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.quantium.mobile.geradores.tabelaschema.TabelaSchema;
import com.quantium.mobile.geradores.util.LoggerUtil;
import com.quantium.mobile.geradores.util.SQLiteGeradorUtils;


import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public class SqlTabelaSchemaFactory {

	/**
	 * @param schema Statement CREATE TABLE de uma tabela em string
	 */
	public TabelaSchema gerarTabelaSchema(String schema){
		// iniciando o parser
		CCJSqlParserManager manager = new CCJSqlParserManager();
		// iniciando o statement
		net.sf.jsqlparser.statement.Statement statement = null;
		schema = tratarSchema(schema);
		try {
			statement = manager.parse(new StringReader(schema));
		} catch (JSQLParserException e) {
			LoggerUtil.getLog().error(e);
			LoggerUtil.getLog().error(schema);
			throw new RuntimeException(e);
		}
		// Enviando um visitor para preencher os campos
		CreateTableVisitor createTableVisitor = new CreateTableVisitor();
		statement.accept(createTableVisitor);
		return createTableVisitor.getTabela();
	}

	public final class CreateTableVisitor implements StatementVisitor {

		private TabelaSchema tabela;

		public TabelaSchema getTabela() {
			return tabela;
		}

		@Override
		public void visit(CreateTable ct) {
			/*
			 * inserindo o nome da tabela aqui
			 */
			TabelaSchema.Builder tabelaBuilder = TabelaSchema.criar(ct.getTable().getName());
			// colunas
			@SuppressWarnings("unchecked")
			List<ColumnDefinition> columnDefinitions = (List<ColumnDefinition>)ct.getColumnDefinitions();
			int primaryKeyCount = 0;
			for(ColumnDefinition colunaDefinition :  columnDefinitions){
				String nomeColuna = colunaDefinition.getColumnName();
				Class<?> tipoColuna = SQLiteGeradorUtils.classeJavaEquivalenteAoTipoSql(
						colunaDefinition.getColDataType().getDataType()
				);
				@SuppressWarnings("unchecked")
				List<String> constraints = extractColumnConstraints(
						colunaDefinition, ct.getIndexes()
				);
				if( constraints.contains(TabelaSchema.PRIMARY_KEY_CONSTRAINT) ) {
					primaryKeyCount++;
				}
				String arrConstraints [] = new String[constraints.size()];
				constraints.toArray(arrConstraints);
				tabelaBuilder.adicionarColuna(nomeColuna, tipoColuna, arrConstraints);
				tabela = tabelaBuilder.get();
			}
			if(primaryKeyCount==0){
				throw new RuntimeException(tabela.getNome()+" sem primary key");
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


	private static class MatchRemover{

		private String regex;
		private int colunasGroup;
		private int adicionarGroup;

		public MatchRemover(String regex, int colunasGroup, int adicionarGroup) {
			super();
			this.regex = regex;
			this.colunasGroup = colunasGroup;
			this.adicionarGroup = adicionarGroup;
		}

		
	}

	private List<String> extractColumnConstraints(
			ColumnDefinition colunaDefinition,
			List<Index> indexes
	) {
		@SuppressWarnings("unchecked")
		List<String> specStrings = colunaDefinition.getColumnSpecStrings();
		int indexOfPrimaryKeyConstraint =
				findConstraint(specStrings, TabelaSchema.PRIMARY_KEY_CONSTRAINT);
		// tipos de index "PRIMARY KEY", "UNIQUE", "INDEX"
		boolean isPrimaryKey = (indexOfPrimaryKeyConstraint >= 0);
		for(int i = 0; indexes!=null && i < indexes.size(); i++){
			Index index = (Index)indexes.get(i);
			isPrimaryKey = isPrimaryKey || (
					index.getColumnsNames()
						.contains(colunaDefinition.getColumnName()) &&
					index.getType()
						.equalsIgnoreCase(TabelaSchema.PRIMARY_KEY_CONSTRAINT)
			);
		}
		List<String> constraints = new ArrayList<String>();
		if(isPrimaryKey)
				constraints.add(TabelaSchema.PRIMARY_KEY_CONSTRAINT);
		if (findConstraint(specStrings, TabelaSchema.UNIQUE_CONSTRAINT) >= 0)
				constraints.add(TabelaSchema.UNIQUE_CONSTRAINT);
		if (findConstraint(specStrings, TabelaSchema.NOT_NULL_CONSTRAINT) >= 0)
				constraints.add(TabelaSchema.NOT_NULL_CONSTRAINT);
		return constraints;
	}

	private int findConstraint (List<String> specStrings, String constrt) {
		if (specStrings == null || constrt == null)
			return -1;
		String[] keyStrings = constrt.split("\\s+");
		int indexOfPrimary = -1;
		for (int i = 0; i < specStrings.size(); i++) {
			if (specStrings.get(i).equalsIgnoreCase(keyStrings[0])) {
				indexOfPrimary = i;
				break;
			}
		}
		if (indexOfPrimary < 0)
			return -1;
		for (int i = 1; i < keyStrings.length ; i++) {
			if ( !(
					specStrings.get(indexOfPrimary+i)
						.equalsIgnoreCase(keyStrings[i])
			) ) {
				return -1;
			}
		}
		return indexOfPrimary;
	}

	private static final String REMOVER[] = {
		"on\\s+conflict\\s+\\w+",
		"constraint\\s+\\w+",
//		sed -E 's/,[[:space:]]*CONSTRAINT[[:space:]].*/\);/' |
//		sed -E 's/CREATE[[:space:]]+VIEW.*//' |

	};

	private static final MatchRemover ADICIONAR_A_COLUNA[] = {
		new MatchRemover(
			",\\s*foreign\\s+key\\s*\\(\\s*(\\w+)\\s*\\)\\s+(references\\s+\\w+\\s*\\(\\s*\\w+\\s*\\))",
			1,
			2
		),
		new MatchRemover(
			",\\s*(unique)\\s*\\((\\s*\\w+\\s*(,\\s*\\w+\\s*)*)\\)",
			2,
			1
		)
	};

	private static String COLUNA = 
		// inicio
		"[\\(,]\\s*"+
		// nome da coluna
		"(\\w+)"+
		// tipo e qualificadores sem parenteses
		"\\s+[^,\\(\\)]*"+
		// grupos com parentses e mais qualificadores
		"((\\([^\\)]*\\))\\s*[^,\\)]*)*";
		// fim da declaracao da coluna
		// note que esta regex nao inclui a virgula ou fechamento de parenteses

private String tratarSchema(String schema) {
	for(String regex : REMOVER){
		Pattern pat = Pattern.compile(regex, Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
		Matcher mobj = pat.matcher(schema);
		if(mobj.find()){
			schema = schema.substring(0, mobj.start()) + schema.substring(mobj.end());
		}
	}
	for(MatchRemover regex : ADICIONAR_A_COLUNA){
		Pattern pat = Pattern.compile(regex.regex, Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
		Matcher mobj = pat.matcher(schema);
		if(mobj.find()){
			// DEBUG
			//String match = mobj.group(2);
			String result =  schema.substring(0, mobj.start()) + schema.substring(mobj.end());
			String colunas[] = mobj.group(regex.colunasGroup).split(",");
			
			String paraAdicionar = " " + mobj.group(regex.adicionarGroup);
			for(String coluna : colunas ){
				Pattern colPat = Pattern.compile(COLUNA, Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
				Matcher colMatch = colPat.matcher(result);
				boolean colunaEncontrada = false;
				while(!colunaEncontrada){
					if(!colMatch.find())
						throw new RuntimeException(coluna+" nao encontrada em:\\n"+schema);
					// DEBUG
					//String colmatch = colMatch.group();
					String debuggroups[] = new String[colMatch.groupCount()];
					for(int i=0;i<debuggroups.length;i++)
						debuggroups[i] = colMatch.group(i);
					if(colMatch.group(1).trim().equalsIgnoreCase(coluna.trim())){
						result = result.substring(0, colMatch.end()) + paraAdicionar+result.substring(colMatch.end());
						colunaEncontrada = true;
					}
				}
			}
			schema = result;
		}
	}
	return schema;
}

}
