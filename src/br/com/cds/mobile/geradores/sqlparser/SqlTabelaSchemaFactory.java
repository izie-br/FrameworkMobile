package br.com.cds.mobile.geradores.sqlparser;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;
import br.com.cds.mobile.geradores.util.SQLiteGeradorUtils;

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

	String REMOVER[] = {
			"on\\s+conflict\\s+\\w+",
			"constraint\\s+\\w",
//			sed -E 's/,[[:space:]]*CONSTRAINT[[:space:]].*/\);/' |
//			sed -E 's/CREATE[[:space:]]+VIEW.*//' |

	};

	private String tratarSchema(String schema) {
		for(String regex : REMOVER){
			Pattern pat = Pattern.compile(regex, Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
			Matcher mobj = pat.matcher(schema);
			if(mobj.find()){
				schema = schema.substring(0, mobj.start()) + schema.substring(mobj.end());
			}
		}
		return schema;
	}

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
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		// Enviando um visitor para preencher os campos
		CreateTableVisitor createTableVisitor = new CreateTableVisitor();
		statement.accept(createTableVisitor);
		// System.out.println(nome+" "+colunas.size()+" colunas");
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
			for(ColumnDefinition colunaDefinition :  columnDefinitions){
				String nomeColuna = colunaDefinition.getColumnName();
				Class<?> tipoColuna = SQLiteGeradorUtils.classeJavaEquivalenteAoTipoSql(
						colunaDefinition.getColDataType().getDataType()
				);
				/*
				 * inserindo as colunas, (nome, tipo) aqui 
				 */
				boolean isPrimaryKey = (colunaDefinition.getColumnSpecStrings() !=null &&
						colunaDefinition.getColumnSpecStrings().contains("PRIMARY")&&
						colunaDefinition.getColumnSpecStrings().contains("KEY")
						);
				// tipos de index "PRIMARY KEY", "UNIQUE", "INDEX"
				if(ct.getIndexes()!=null) for(Object it : ct.getIndexes()){
					Index index = (Index)it;
					isPrimaryKey = isPrimaryKey || (
							index.getColumnsNames().contains(nomeColuna) &&
							index.getType().equals("PRIMARY KEY")
					);
				}
				if(isPrimaryKey)
					tabelaBuilder.adicionarPrimaryKey(nomeColuna, tipoColuna);
				else
					tabelaBuilder.adicionarColuna(nomeColuna, tipoColuna);
				tabela = tabelaBuilder.get();
			}
			if(tabela.getPrimaryKey()==null){
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


}
