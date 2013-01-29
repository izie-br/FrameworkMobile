package com.quantium.mobile.geradores.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.geradores.GeneratorConfig;
import com.quantium.mobile.geradores.GeradorException;
import com.quantium.mobile.geradores.filters.ModuleNameOnTablePrefixFilter;
import com.quantium.mobile.geradores.filters.PrefixoTabelaFilter;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoPorNomeFilter;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.ModelSchema;
import com.quantium.mobile.geradores.sqlparser.SqlTabelaSchemaFactory;
import com.quantium.mobile.geradores.util.Constants;
import com.quantium.mobile.geradores.util.LoggerUtil;
import com.quantium.mobile.geradores.util.SQLiteGeradorUtils;
import com.quantium.mobile.shared.util.XMLUtil;

public class SQLiteInputParser implements InputParser {
	
	public static final String INPUT_PARSER_IDENTIFIER = "sqlite";

	/**
	 * Extrai as JavaBeanSchema's de um arquivo de strings do padrao android
	 *   com o schema do banco.
	 * <ul>
	 *   <li>Busca o arquivo sql.xml e le todos os scripts ate a
	 *       versao do banco no DB.java;</li>
	 *   <li>Escreve todos os scripts em um banco sqlite na maquina de
	 *       desenvolvimento, deste modo todos os ALTER TABLE e DROP sao
	 *       processados</li>
	 *   <li>Retira um DUMP do banco resultante;</li>
	 *   <li>Cria uma fabrica de JavaBeanSchema, baseada no SqlParser e
	 *       adiciona a ela filtros;</li>
	 *   <li>Usa a fabrica e o DUMP para criar a lista de JavaBeanSchema</li>
	 * </ul>
	 * @param sqlResource arquivo de strings Android
	 * @param defaultProperties propriedads
	 * @param dbVersion
	 * @return
	 * @throws IOException
	 */
	@Override
	public Collection<JavaBeanSchema> getSchemas(GeneratorConfig information,
			Map<String, Object> defaultProperties) throws GeradorException {
		Collection<JavaBeanSchema> javaBeanSchemas = new ArrayList<JavaBeanSchema>();

		File sqlResource = information.getInputFile();
		int dbVersion = information.retrieveDatabaseVersion();
		String val = getSqlTill(sqlResource, dbVersion);
		Collection<ModelSchema> tabelasBanco;
		try {
			if (val != null) {
				val = sqliteSchema(val);
				BufferedReader reader = new BufferedReader(
						new StringReader(val));
				String line = reader.readLine();
				while (line != null) {
					LoggerUtil.getLog().info(line);
					line = reader.readLine();
				}
			}

			tabelasBanco = getTabelasDoSchema(new StringReader(val),
					(String) defaultProperties.get(Constants.PROPERTIY_IGNORED));
		} catch (IOException e) {
			throw new GeradorException(e);
		}

		JavaBeanSchema.Factory factory = new JavaBeanSchema.Factory();
		factory.addFiltroFactory(new ModuleNameOnTablePrefixFilter.Factory());
		factory.addFiltroFactory(new PrefixoTabelaFilter.Factory("tb_"));
		factory.addFiltroFactory(new AssociacaoPorNomeFilter.Factory(
				"{COLUMN=id}_{TABLE}"));

		// gerando os JavaBeanSchemas
		for (ModelSchema tabela : tabelasBanco)
			javaBeanSchemas.add(factory.javaBeanSchemaParaTabela(tabela));
		return javaBeanSchemas;
	}

	/**
	 * Busca todos os scripts com nome db_versao_X onde X e um numero menor
	 * que o parametro de versao
	 * @param sqlResource arquivo de xml com scripts SQL
	 * @param version     versao
	 * @return
	 */
	protected String getSqlTill(File sqlResource, Integer version) {
		StringBuilder out = new StringBuilder();
		List<String> nodes = XMLUtil.xpath(sqlResource, "//string["
				+ "contains(@name,\""+Constants.DB_VERSION_PREFIX+"\") and "
				+ "number(substring(@name,11)) < " + (++version) + "]//text()");
		for (String node : nodes)
			out.append(node);
		return out.toString();
	}

	private String sqliteSchema(String sql) {
		try {
			return SQLiteGeradorUtils.getSchema(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Le um script e busca todos CREATE TABLE, gerando TableSchema deles
	 * 
	 * @param input stream do script
	 * @param ignored lista de tabelas ignoradas, separadas por virgulas (REFATORAR!)
	 * @return
	 * @throws IOException
	 */
	public Collection<ModelSchema> getTabelasDoSchema(
			Reader input, String ignored)
			throws IOException
	{
		BufferedReader reader = new BufferedReader(input);
		Collection<ModelSchema> tabelas = new ArrayList<ModelSchema>();
		SqlTabelaSchemaFactory factory = new SqlTabelaSchemaFactory();
		Pattern createTablePattern = Pattern
				.compile(
						"CREATE\\s+(TEMP\\w*\\s+)?TABLE\\s+(IF\\s+NOT\\s+EXISTS\\s+)?(\\w+)",
						Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		for (;;) {
			StringBuilder sb = new StringBuilder();
			int c;
			for (;;) {
				c = reader.read();
				if (c < 0)
					break;
				sb.append((char) c);
				if (c == ';')
					break;
			}
			String createTableStatement = sb.toString();
			String ignoredTables[] = getIgnoredTables(ignored);
			if (createTableStatement == null
					|| createTableStatement.matches("^[\\s\\n]*$")) {
				break;
			}
			Matcher mobj = createTablePattern.matcher(createTableStatement);
			if (!mobj.find() || checkIfIgnored(mobj.group(3), ignoredTables)) {
				LoggerUtil.getLog().info("IGNORED::" + sb.toString());
				continue;
			}
			ModelSchema tabela = factory
					.gerarTabelaSchema(createTableStatement);
			tabelas.add(tabela);
			LoggerUtil.getLog().info("tabela: " + tabela.getName ());
		}
		return tabelas;
	}

	private String [] getIgnoredTables(String ignored){
		return ignored == null ? new String[0] : ignored.split("[\\|,]");
	}
	
	private static boolean checkIfIgnored(String table, String ignored[]){
		if (ignored == null)
			return false;
		for (String str : ignored){
			if(table.equalsIgnoreCase(str))
				return true;
		}
		return false;
	}

	@Override
	public void generateSqlResources(
			GeneratorConfig config,
			Collection<Table> tables)
			throws GeradorException
	{
		/* no-op */
	}

}
