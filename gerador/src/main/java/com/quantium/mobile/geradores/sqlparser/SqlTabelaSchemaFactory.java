package com.quantium.mobile.geradores.sqlparser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.quantium.mobile.framework.utils.StringUtil;
import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.javabean.ModelSchema;
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

	private static final String PRIMARY_KEY_CONSTRAINT = "PRIMARY KEY";
	private static final String UNIQUE_CONSTRAINT = "UNIQUE";
	private static final String NOT_NULL_CONSTRAINT = "NOT NULL";

	/**
	 * @param schema
	 *            Statement CREATE TABLE de uma tabela em string
	 */
	public ModelSchema gerarTabelaSchema(String schema) {
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

		private ModelSchema tabela;

		public ModelSchema getTabela() {
			return tabela;
		}

		@Override
		public void visit(CreateTable ct) {
			/*
			 * inserindo o nome da tabela aqui
			 */
			ModelSchema.Builder tabelaBuilder = ModelSchema.create(
					"default",
					ct.getTable().getName().toLowerCase());
			// colunas
			@SuppressWarnings("unchecked")
			List<ColumnDefinition> columnDefinitions = (List<ColumnDefinition>) ct.getColumnDefinitions();
			int primaryKeyCount = 0;
			tabela = tabelaBuilder.get();
			for (ColumnDefinition colunaDefinition : columnDefinitions) {

				// nome da coluna
				String nomeColuna = colunaDefinition.getColumnName().toLowerCase();

				// tipo
				Class<?> tipoColuna = SQLiteGeradorUtils.classeJavaEquivalenteAoTipoSql(colunaDefinition
						.getColDataType().getDataType());

				// constraints da coluna
				@SuppressWarnings("unchecked")
				List<Constraint> constraints = extractColumnConstraints(
						tipoColuna, colunaDefinition, ct.getIndexes());
				Constraint arrConstraints[] = new Constraint[constraints.size()];
				constraints.toArray(arrConstraints);

				tabelaBuilder.addProperty (nomeColuna, tipoColuna, arrConstraints);

				/*
				 * contagem e validacao de primaryKeys
				 */
				boolean isPrimaryKey = false;
				for (Constraint constraint : arrConstraints) {
					if (constraint instanceof Constraint.PrimaryKey) {
						isPrimaryKey = true;
						break;
					}
				}
				System.out.println("isPrimaryKey:" + isPrimaryKey);
				if (isPrimaryKey) {
					if (primaryKeyCount >= 1) {
						throw new RuntimeException(tabela.getName () + " tem mais de uma primary key");
					}
					if (tipoColuna != Long.class) {
						throw new RuntimeException(tabela.getName () + " tem primarykey NON-INTEGER");
					}
					primaryKeyCount++;
				}
			}
			if (primaryKeyCount == 0) {
				throw new RuntimeException(tabela.getName () + " sem primary key");
			}
		}

		private static final String MSG_ERRO =
				"Este metodo nao deveria ter sido chamado."
				+ "Confira o schema da tabela se ha linhas que nao sao CREATE TABLE";

		@Override
		public void visit(Truncate arg0) {
			throw new RuntimeException(MSG_ERRO);
		}

		@Override
		public void visit(Drop arg0) {
			throw new RuntimeException(MSG_ERRO);
		}

		@Override
		public void visit(Replace arg0) {
			throw new RuntimeException(MSG_ERRO);
		}

		@Override
		public void visit(Insert arg0) {
			throw new RuntimeException(MSG_ERRO);
		}

		@Override
		public void visit(Update arg0) {
			throw new RuntimeException(MSG_ERRO);
		}

		@Override
		public void visit(Delete arg0) {
			throw new RuntimeException(MSG_ERRO);
		}

		@Override
		public void visit(Select arg0) {
			throw new RuntimeException(MSG_ERRO);
		}
	}

	private static class MatchRemover {

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

	private static List<Constraint> extractColumnConstraints(
			Class<?> type, ColumnDefinition colunaDefinition,
			List<Index> indexes) {
		@SuppressWarnings("unchecked")
		List<String> specStrings = colunaDefinition.getColumnSpecStrings();
		int indexOfPrimaryKeyConstraint = findConstraint(specStrings, PRIMARY_KEY_CONSTRAINT);
		// tipos de index "PRIMARY KEY", "UNIQUE", "INDEX"
		boolean isPrimaryKey = (indexOfPrimaryKeyConstraint >= 0);
		// refatorar
		for (int i = 0; indexes != null && i < indexes.size(); i++) {
			Index index = (Index) indexes.get(i);
			isPrimaryKey = isPrimaryKey
					|| (index.getColumnsNames().contains(colunaDefinition.getColumnName()) && index.getType()
							.equalsIgnoreCase(PRIMARY_KEY_CONSTRAINT));
		}
		List<Constraint> constraints = new ArrayList<Constraint>();
		if (isPrimaryKey)
			constraints.add(Constraint.primaryKey());
		if (findConstraint(specStrings, UNIQUE_CONSTRAINT) >= 0)
			constraints.add(Constraint.unique());
		if (findConstraint(specStrings, NOT_NULL_CONSTRAINT) >= 0)
			constraints.add(Constraint.notNull());
		Constraint max = getMaxFromColumnType(type, colunaDefinition);
		if (max != null)
			constraints.add(max);
		return constraints;
	}

	private static Constraint getMaxFromColumnType(Class<?> type,
			ColumnDefinition colunaDefinition) {
		@SuppressWarnings("unchecked")
		List<String> args = colunaDefinition.getColDataType().getArgumentsStringList();
		if (type.equals(String.class) && args != null && args.size() == 1 ){
			try {
				Short maxLength = Short.parseShort(args.get(0));
				return Constraint.max(maxLength);
			} catch (RuntimeException e) {
				LoggerUtil.getLog().error(StringUtil.getStackTrace(e));
			}
		}
		return null;
	}

	private static int findConstraint(List<String> specStrings, String constrt) {
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
		for (int i = 1; i < keyStrings.length; i++) {
			if (!(specStrings.get(indexOfPrimary + i).equalsIgnoreCase(keyStrings[i]))) {
				return -1;
			}
		}
		return indexOfPrimary;
	}

	private static final String REMOVER[] = { "on\\s+conflict\\s+\\w+", "constraint\\s+\\w+",
	// sed -E 's/,[[:space:]]*CONSTRAINT[[:space:]].*/\);/' |
	// sed -E 's/CREATE[[:space:]]+VIEW.*//' |

	};

	private static final MatchRemover ADICIONAR_A_COLUNA[] = {
			new MatchRemover(
					",\\s*foreign\\s+key\\s*\\(\\s*(\\w+)\\s*\\)\\s+(references\\s+\\w+\\s*\\(\\s*\\w+\\s*\\))", 1, 2),
			new MatchRemover(",\\s*(unique)\\s*\\((\\s*\\w+\\s*(,\\s*\\w+\\s*)*)\\)", 2, 1) };

	private static String COLUNA =
	// inicio
	"[\\(,]\\s*" +
	// nome da coluna
			"(\\w+)" +
			// tipo e qualificadores sem parenteses
			"\\s+[^,\\(\\)]*" +
			// grupos com parentses e mais qualificadores
			"((\\([^\\)]*\\))\\s*[^,\\)]*)*";

	// fim da declaracao da coluna
	// note que esta regex nao inclui a virgula ou fechamento de parenteses

	private static String tratarSchema(String schema) {
		for (String regex : REMOVER) {
			Pattern pat = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
			Matcher mobj = pat.matcher(schema);
			if (mobj.find()) {
				schema = schema.substring(0, mobj.start()) + schema.substring(mobj.end());
			}
		}
		for (MatchRemover regex : ADICIONAR_A_COLUNA) {
			Pattern pat = Pattern.compile(regex.regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
			Matcher mobj = pat.matcher(schema);
			if (mobj.find()) {
				// DEBUG
				// String match = mobj.group(2);
				String result = schema.substring(0, mobj.start()) + schema.substring(mobj.end());
				String colunas[] = mobj.group(regex.colunasGroup).split(",");

				String paraAdicionar = " " + mobj.group(regex.adicionarGroup);
				for (String coluna : colunas) {
					Pattern colPat = Pattern.compile(COLUNA, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
					Matcher colMatch = colPat.matcher(result);
					boolean colunaEncontrada = false;
					while (!colunaEncontrada) {
						if (!colMatch.find())
							throw new RuntimeException(coluna + " nao encontrada em:\\n" + schema);
						// DEBUG
						// String colmatch = colMatch.group();
						String debuggroups[] = new String[colMatch.groupCount()];
						for (int i = 0; i < debuggroups.length; i++)
							debuggroups[i] = colMatch.group(i);
						if (colMatch.group(1).trim().equalsIgnoreCase(coluna.trim())) {
							result = result.substring(0, colMatch.end()) + paraAdicionar
									+ result.substring(colMatch.end());
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
