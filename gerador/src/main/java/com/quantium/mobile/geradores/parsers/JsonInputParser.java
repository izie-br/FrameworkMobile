package com.quantium.mobile.geradores.parsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.GeneratorConfig;
import com.quantium.mobile.geradores.GeradorException;
import com.quantium.mobile.geradores.dbschema.SQLiteSchemaGenerator;
import com.quantium.mobile.geradores.filters.ModuleNameOnTablePrefixFilter;
import com.quantium.mobile.geradores.filters.PrefixoTabelaFilter;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoManyToMany;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.ModelSchema;
import com.quantium.mobile.geradores.util.Constants;

public class JsonInputParser implements InputParser {

	private static final String PACKAGE_LIST = "packageList";
	// private static final String TABLE_NAME_TEMPLATE = "%s_%s";
	public static final String INPUT_PARSER_IDENTIFIER = "json";
	private static final String CLASS_LIST = "classList";
	private static final String ATTRIBUTE_LIST = "attributeList";
	private static final String FROM_ASSOCIATIONS_LIST = "fromAssociationList";

	// private static final String TO_ASSOCIATIONS_LIST = "toAssociationList";

	@Override
	public Collection<JavaBeanSchema> getSchemas(GeneratorConfig information, Map<String, Object> defaultProperties)
			throws GeradorException {
		InputStream inputStream;
		try {
			File sqlResource = information.getInputFile();
			inputStream = new FileInputStream(sqlResource);
			String fileContent = IOUtils.toString(inputStream);
			JSONObject json = new JSONObject(fileContent);
			List<ModelSchema> tabelasJson = generateTableSchema(json);
			Collection<JavaBeanSchema> javaBeanSchemas = new ArrayList<JavaBeanSchema>();
			// int dbVersion = information.retrieveDatabaseVersion();
			JavaBeanSchema.Factory factory = new JavaBeanSchema.Factory();
			factory.addFiltroFactory (new ModuleNameOnTablePrefixFilter.Factory());
			factory.addFiltroFactory(new PrefixoTabelaFilter.Factory("tb_"));
			// gerando os JavaBeanSchemas
			for (ModelSchema tabela : tabelasJson) {
				javaBeanSchemas.add(factory.javaBeanSchemaParaTabela(tabela));
			}
			return javaBeanSchemas;
		} catch (IOException e) {
			throw new GeradorException(e);
		} catch (JSONException e) {
			throw new GeradorException(e);
		}
	}

	Hashtable<String, ModelSchema.Builder> hashtable =
			new Hashtable<String, ModelSchema.Builder>();
	String dateId;
	String fileId;

	private List<ModelSchema> generateTableSchema(JSONObject json)
			throws JSONException
	{
		List<ModelSchema> list = new ArrayList<ModelSchema>();
		List<JSONObject> packages = jsonArrayToList(json.optJSONArray(PACKAGE_LIST));
		for (JSONObject jsonPackage : packages) {
			String moduleName = CamelCaseUtils.camelToLowerAndUnderscores (
					jsonPackage.getString ("name"));

			List<JSONObject> classes = jsonArrayToList(jsonPackage.optJSONArray(CLASS_LIST));
			for (JSONObject jsonClass : classes) {
				String databaseTable = jsonClass.getString("name");
				if (jsonPackage.optBoolean("isLibrary")) {
					if (databaseTable.equals("Date")) {
						dateId = jsonClass.optString("id");
						continue;
					}
					if (databaseTable.equals("File")) {
						fileId = jsonClass.optString("id");
						continue;
					}
				}
				if (databaseTable.toUpperCase().contains("HISTORY")) {
					continue;
				}
				ModelSchema.Builder tabelaBuilder =
						ModelSchema.create (
								moduleName,
								CamelCaseUtils.camelToLowerAndUnderscores(
										databaseTable
								)
						);
				tabelaBuilder.addProperty (
						"id", convertJsonTypeToJavaType("Long"),
						Constraint.Type.PRIMARY_KEY);
				hashtable.put(jsonClass.getString("id"), tabelaBuilder);
			}
		}
		for (JSONObject jsonPackage : packages) {
			List<JSONObject> classes = jsonArrayToList(jsonPackage.optJSONArray(CLASS_LIST));
			for (JSONObject jsonClass : classes) {
				if (jsonClass.getString("name").toUpperCase().contains("HISTORY")) {
					continue;
				}
				List<JSONObject> fromAssociations = jsonArrayToList(jsonClass.optJSONArray(FROM_ASSOCIATIONS_LIST));
//				List<JSONObject> toAssociations = jsonArrayToList(jsonClass.optJSONArray(TO_ASSOCIATIONS_LIST));
				ModelSchema.Builder tabelaBuilder = hashtable.get(jsonClass.getString("id"));
				if (tabelaBuilder == null) {
					continue;
				}
				List<JSONObject> attributes = jsonArrayToList(jsonClass.optJSONArray(ATTRIBUTE_LIST));
				for (JSONObject jsonAttribute : attributes) {
					String attributeName = jsonAttribute.getString("name");
					Class<?> type = convertJsonTypeToJavaType(jsonAttribute.getString("type"));
					boolean isRequired = jsonAttribute.optBoolean("isRequired");
					boolean isUnique = jsonAttribute.optBoolean("isUnique");
					Constraint.Type[] constraints = null;
					if (isUnique && isRequired) {
						constraints = new Constraint.Type[] { Constraint.Type.NOT_NULL, Constraint.Type.UNIQUE };
					} else {
						if (isUnique) {
							constraints = new Constraint.Type[] { Constraint.Type.UNIQUE };
						} else if (isRequired) {
							constraints = new Constraint.Type[] { Constraint.Type.NOT_NULL };
						}
					}
					if (type == null && hashtable.get(jsonAttribute.optString("type")) == null) {
						if (jsonAttribute.optString("type").equals(fileId)) {
							type = String.class;
						} else if (jsonAttribute.optString("type").equals(dateId)) {
							type = Date.class;
						} else {
							throw new IllegalArgumentException(String.format("Tipo complexo nao encontrado: %s",
									jsonAttribute.optString("type")));
						}
					} else if (type == null && hashtable.get(jsonAttribute.optString("type")) != null) {
						String fkId = CamelCaseUtils.camelToLowerAndUnderscores("id_" + attributeName);
						tabelaBuilder.addProperty (fkId, Long.class, constraints);
						ModelSchema tabelaA = hashtable.get(jsonAttribute.optString("type")).get();
						ModelSchema tabelaB = tabelaBuilder.get();
						AssociacaoOneToMany assoc = new AssociacaoOneToMany (
								tabelaA, tabelaB,
								fkId, !isRequired,
								"id"
						);
						tabelaBuilder.addAssociation (assoc);
						hashtable.get(jsonAttribute.optString("type"))
								.addAssociation (assoc);
						continue;
					}
					if (type != null) {
						tabelaBuilder.addProperty (
								CamelCaseUtils.camelToLowerAndUnderscores(attributeName),
								type,
								constraints);
					}
				}
				for (JSONObject jsonAssociation : fromAssociations) {
					ModelSchema from = hashtable.get(jsonAssociation.optString("from")).get();
					ModelSchema to = hashtable.get(jsonAssociation.optString("to")).get();
					String colunaId = "id";
					if ("n..n".equals(jsonAssociation.optString("type"))) {
						String fromName = CamelCaseUtils.camelToLowerAndUnderscores(from.getName ());
						String toName = CamelCaseUtils.camelToLowerAndUnderscores(to.getName());
						String colunaFrom = CamelCaseUtils.camelToLowerAndUnderscores("id_" + fromName);
						String colunaTo = CamelCaseUtils.camelToLowerAndUnderscores("id_" + toName);
						String tableName = "tb_" + fromName + "_join_" + toName;
						// a tabela join ficara no modulo da "from" da associacao
						String module = from.getModule ();
						Associacao assoc = new AssociacaoManyToMany (
								from, to,
								colunaTo, colunaFrom,
								colunaId, colunaId,
								gerarAssociativa (module, tableName, colunaFrom, colunaTo),
								tableName);
						hashtable.get(jsonAssociation.optString("from")).addAssociation(assoc);
						hashtable.get(jsonAssociation.optString("to")).addAssociation(assoc);
					}
				}
//				for (JSONObject jsonAssociation : toAssociations) {
//					ModelSchema from = hashtable.get(jsonAssociation.optString("from")).get();
//					ModelSchema to = hashtable.get(jsonAssociation.optString("to")).get();
//					String colunaId = "id";
//					if ("n..n".equals(jsonAssociation.optString("type"))) {
//						String colunaFrom = CamelCaseUtils.camelToLowerAndUnderscores("id" + from.getName ());
//						String colunaTo = CamelCaseUtils.camelToLowerAndUnderscores("id" + to.getName ());
//						String tableName = CamelCaseUtils.camelToLowerAndUnderscores("tb" + from.getName() + "Join"
//						                + to.getName());
//						Associacao assoc = new AssociacaoManyToMany (
//								from, to,
//								colunaTo, colunaFrom,
//								colunaId, colunaId,
//								gerarAssociativa (tableName, colunaFrom, colunaTo),
//								tableName);
//						tabelaBuilder.addAssociation(assoc);
//					}
//				}
			}
		}
		Set<Entry<String, ModelSchema.Builder>> entryes = hashtable.entrySet();
		Iterator<Entry<String, ModelSchema.Builder>> it = entryes.iterator();
		while (it.hasNext()) {
			list.add(it.next().getValue().get());
		}
		return list;
	}

	private static ModelSchema gerarAssociativa(
			String module, String databaseTable,
			String colunaFrom, String colunaTo) {
		ModelSchema.Builder tabelaBuilder =
				ModelSchema.create(module, databaseTable)
				.addProperty ("id", Long.class, Constraint.PRIMARY_KEY)
				.addProperty (colunaFrom, Long.class, Constraint.Type.NOT_NULL)
				.addProperty(colunaTo, Long.class, Constraint.Type.NOT_NULL);
		return tabelaBuilder.get();
	}

	private Class<?> convertJsonTypeToJavaType(String type) {
		if (type == null) {
			return java.lang.String.class;
		}
		if (type.equals("String")) {
			return java.lang.String.class;
		}
		if (type.equals("Date")) {
			return java.util.Date.class;
		}
		if (type.equals("Long")) {
			return java.lang.Long.class;
		}
		if (type.equals("Integer")) {
			return java.lang.Long.class;
		}
		if (type.equals("Boolean")) {
			return java.lang.Boolean.class;
		}
		if (type.equals("Float")) {
			return java.lang.Double.class;
		}
		if (type.equals("Double")) {
			return java.lang.Double.class;
		}
		return null;
	}

	private List<JSONObject> jsonArrayToList(JSONArray array) throws JSONException {
		List<JSONObject> list = new ArrayList<JSONObject>();
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				list.add(array.getJSONObject(i));
			}
		}
		return list;
	}

	@Override
	public void generateSqlResources(
			GeneratorConfig config, Collection<Table> tables)
			throws GeradorException
	{
		File outputDir = config.getMigrationsOutputDir();
		if (outputDir == null)
			return;
		if (outputDir.exists() && !outputDir.isDirectory()) {
			throw new GeradorException("Diretorio de saida de migracoes nao eh diretorio." + " Arquivo encontrado em "
					+ outputDir.getAbsolutePath());
		}

		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		final String expectedName = Constants.DB_VERSION_PREFIX + "1.sql";
		File firstVersion = new File(outputDir, expectedName);
		if (!firstVersion.exists())
			writeSchemasToOutput(tables, firstVersion);

		int version = config.retrieveDatabaseVersion();
		if (version > 1) {
			String currentSchemaName = "schema_" + version + ".sql";
			File currentSchema = new File(outputDir, currentSchemaName);
			writeSchemasToOutput(tables, currentSchema);
		}
	}

	private static void writeSchemasToOutput(
			Collection<Table> tables, File output)
			throws GeradorException
	{
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
			SQLiteSchemaGenerator generator = new SQLiteSchemaGenerator();
			for (Table table : tables) {
				writer.append(generator.getSchemaFor(table));
			}
			writer.close();
		} catch (Exception e) {
			throw new GeradorException(e);
		}

	}

}