package com.quantium.mobile.geradores.parsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
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
import com.quantium.mobile.geradores.javabean.ModelSchema.Builder;
import com.quantium.mobile.geradores.util.Constants;
import com.quantium.mobile.geradores.util.LoggerUtil;

public class JsonInputParser implements InputParser {

	private static final String PACKAGE_LIST = "packageList";
	// private static final String TABLE_NAME_TEMPLATE = "%s_%s";
	public static final String INPUT_PARSER_IDENTIFIER = "json";
	private static final String CLASS_LIST = "classList";
	private static final String ATTRIBUTE_LIST = "attributeList";
	private static final String FROM_ASSOCIATIONS_LIST = "fromAssociationList";
	private static final String TO_ASSOCIATIONS_LIST = "toAssociationList";
	private JSONObject json;
	private Collection<JavaBeanSchema> javaBeanSchemas;
	private List<ModelSchema> tabelasJson;
	private Map<String, ModelSchema.Builder> modelSchemaBuilderMap;
	private Map<String, JSONObject> associationsJsonMap;
	private String dateId;
	private String fileId;

	@Override
	public Collection<JavaBeanSchema> getSchemas(GeneratorConfig information, Map<String, Object> defaultProperties)
			throws GeradorException {
		InputStream inputStream;
		try {
			File sqlResource = information.getInputFile();
			inputStream = new FileInputStream(sqlResource);
			String fileContent = IOUtils.toString(inputStream);
			JSONObject json = new JSONObject(fileContent);
			this.json = json;
			generateTableSchema();
			javaBeanSchemas = new ArrayList<JavaBeanSchema>();
			// int dbVersion = information.retrieveDatabaseVersion();
			JavaBeanSchema.Factory factory = new JavaBeanSchema.Factory();
			factory.addFiltroFactory(new ModuleNameOnTablePrefixFilter.Factory());
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

	private void generateTableSchema() throws JSONException {
		mapEntities();
		extractModelSchema();
	}

	private void extractModelSchema() throws JSONException {
		for (JSONObject jsonClass : allClasses) {
			String classId = jsonClass.getString("id");
			ModelSchema.Builder builder = modelSchemaBuilderMap.get(classId);
			List<JSONObject> attributes = jsonArrayToList(jsonClass.optJSONArray(ATTRIBUTE_LIST));
			for (JSONObject jsonAttribute : attributes) {
				String attributeName = jsonAttribute.getString("name");
				String type = jsonAttribute.getString("type");
				boolean isRequired = jsonAttribute.optBoolean("isRequired");
				boolean isUnique = jsonAttribute.optBoolean("isUnique");

				BigDecimal min = extractNumber(jsonAttribute.optString("min"));
				BigDecimal max = extractNumber(jsonAttribute.optString("max"));
				BigDecimal lengthConstraint = null;

				Class<?> classType = convertJsonTypeToJavaType(type);
				boolean isStringType = String.class.equals(classType);

				if (isStringType) {
					/* min < 0 */
					if (min != null && min.compareTo(BigDecimal.ZERO) < 0) {
						throw new RuntimeException(String.format(
								"tamanho minimo de %s.%s menor que 0",
								classId, attributeName));
					}
					/* max < 0 */
					if (max != null && max.compareTo(BigDecimal.ZERO) < 0) {
						throw new RuntimeException(String.format(
								"tamanho maximo de %s.%s menor que 0",
								classId, attributeName));
					}
				}
				if (min != null && max != null) {
					/* min > max */
					if(min.compareTo(max) > 0) {
						throw new RuntimeException(String.format(
								"tamanho minimo de %s.%s maior que o maximo",
								classId, attributeName));
					}
					if (isStringType) {
						/* min == max */
						if (min.equals(max)) {
							lengthConstraint = min; /* = max*/
							min = null;
							max = null;
						}
					}
				}

				if (attributeName == null || attributeName.trim().equals("")) {
					throw new IllegalArgumentException("Attribute name cannot be null or empty.");
				}
				Constraint constraints[] = null;
				{
					List<Constraint> constraintList = new ArrayList<Constraint>();
					if (isUnique)
						constraintList.add(Constraint.unique());
					if (isRequired)
						constraintList.add(Constraint.notNull());
					if (Long.class.equals(classType)) {
						if (min!= null)
							constraintList.add(Constraint.min(min.intValue()));
						if (max != null)
							constraintList.add(Constraint.max(max.intValue()));
					} else if (Double.class.equals(classType)) {
						if (min!= null)
							constraintList.add(Constraint.min(min.doubleValue()));
						if (max != null)
							constraintList.add(Constraint.max(max.doubleValue()));
					} else if (isStringType) {
						if (min!= null)
							constraintList.add(Constraint.min(min.intValue()));
						if (max != null)
							constraintList.add(Constraint.max(max.intValue()));
						if (lengthConstraint != null)
							constraintList.add(Constraint.length(lengthConstraint.intValue()));
					}
					constraints = new Constraint[constraintList.size()];
					constraintList.toArray(constraints);
				}
				if (classType == null && modelSchemaBuilderMap.get(type) == null) {
					if (type.equals(fileId)) {
						classType = String.class;
					} else if (type.equals(dateId)) {
						classType = Date.class;
					} else {
						throw new IllegalArgumentException(String.format("Tipo complexo nao encontrado: %s", type));
					}
				} else if (classType == null && modelSchemaBuilderMap.get(type) != null) {
					String fkId = CamelCaseUtils.camelToLowerAndUnderscores("id_" + attributeName);
					builder.addProperty(fkId, Long.class, constraints);
					ModelSchema tabelaA = modelSchemaBuilderMap.get(type).get();
					ModelSchema tabelaB = builder.get();
					AssociacaoOneToMany assoc = new AssociacaoOneToMany(tabelaA, tabelaB, fkId, !isRequired, "id");
					builder.addAssociation(assoc);
					modelSchemaBuilderMap.get(type).addAssociation(assoc);
					continue;
				}
				if (classType != null) {
					builder.addProperty(CamelCaseUtils.camelToLowerAndUnderscores(attributeName), classType,
							constraints);
				}
			}
		}
		Set<Entry<String, JSONObject>> associationJsonEntrySet = associationsJsonMap.entrySet();
		for (Entry<String, JSONObject> entry : associationJsonEntrySet) {
			JSONObject jsonAssociation = entry.getValue();
			String toClass = jsonAssociation.optString("toClass");
			if (toClass.contains("History")) {
				continue;
			}
			if (toClass.contains("History")) {
				continue;
			}
			String to = jsonAssociation.optString("to");
			String from = jsonAssociation.optString("from");
			String type = jsonAssociation.optString("type");
			Builder fromBuilder = modelSchemaBuilderMap.get(from);
			Builder toBuilder = modelSchemaBuilderMap.get(to);
			if (fromBuilder == null) {
				throw new IllegalArgumentException("from nulo:" + from);
			}
			if (toBuilder == null) {
				throw new IllegalArgumentException("to nulo:" + to);
			}
			String module = fromBuilder.get().getModule();
			String colunaId = "id";
			if ("n..n".equals(type)) {
				String fromName = CamelCaseUtils.camelToLowerAndUnderscores(fromBuilder.get().getName());
				String toName = CamelCaseUtils.camelToLowerAndUnderscores(toBuilder.get().getName());
				String colunaFrom = CamelCaseUtils.camelToLowerAndUnderscores("id_" + fromName);
				String colunaTo = CamelCaseUtils.camelToLowerAndUnderscores("id_" + toName);
				String tableName = "tb_" + fromName + "_join_" + toName;
				// a tabela join ficara no modulo da "from" da
				// associacao
				Associacao assoc = new AssociacaoManyToMany(fromBuilder.get(), toBuilder.get(), colunaFrom, colunaTo,
						colunaId, colunaId, gerarAssociativa(module, tableName, colunaFrom, colunaTo), tableName);
				modelSchemaBuilderMap.get(from).addAssociation(assoc);
				modelSchemaBuilderMap.get(to).addAssociation(assoc);
			}
		}
		Set<Entry<String, ModelSchema.Builder>> entryes = modelSchemaBuilderMap.entrySet();
		Iterator<Entry<String, ModelSchema.Builder>> it = entryes.iterator();
		tabelasJson = new ArrayList<ModelSchema>();
		while (it.hasNext()) {
			tabelasJson.add(it.next().getValue().get());
		}
	}

	List<JSONObject> allClasses;

	private void mapEntities() throws JSONException {
		List<JSONObject> packages = jsonArrayToList(json.optJSONArray(PACKAGE_LIST));
		allClasses = new ArrayList<JSONObject>();
		modelSchemaBuilderMap = new Hashtable<String, ModelSchema.Builder>();
		associationsJsonMap = new Hashtable<String, JSONObject>();
		for (JSONObject jsonPackage : packages) {
			String moduleName = CamelCaseUtils.camelToLowerAndUnderscores(jsonPackage.getString("name"));
			boolean isLibrary = jsonPackage.optBoolean("isLibrary");
			if (moduleName == null || moduleName.trim().equals("")) {
				throw new IllegalArgumentException("Package name cannot be null or empty.");
			}
			List<JSONObject> classes = jsonArrayToList(jsonPackage.optJSONArray(CLASS_LIST));
			
			for (JSONObject jsonClass : classes) {
				String className = jsonClass.getString("name");
				List<JSONObject> fromAssociations = jsonArrayToList(jsonClass.optJSONArray(FROM_ASSOCIATIONS_LIST));
				List<JSONObject> toAssociations = jsonArrayToList(jsonClass.optJSONArray(TO_ASSOCIATIONS_LIST));

				if (className.toUpperCase().contains("HISTORY")) {
					continue;
				}
				if (className == null || className.trim().equals("")) {
					throw new IllegalArgumentException("Class name cannot be null or empty.");
				}
				if (isLibrary) {
					if (className.equals("Date")) {
						dateId = jsonClass.optString("id");
						continue;
					}
					if (className.equals("File")) {
						fileId = jsonClass.optString("id");
						continue;
					}
				}
				if (className.toUpperCase().contains("HISTORY")) {
					continue;
				}
				allClasses.add(jsonClass);
				ModelSchema.Builder tabelaBuilder = ModelSchema.create(moduleName,
						CamelCaseUtils.camelToLowerAndUnderscores(className));
				tabelaBuilder.addProperty("id", convertJsonTypeToJavaType("Long"), Constraint.primaryKey());
				modelSchemaBuilderMap.put(jsonClass.getString("id"), tabelaBuilder);
				for (JSONObject jsonAssociation : fromAssociations) {
					associationsJsonMap.put(jsonAssociation.getString("id"), jsonAssociation);
				}
				for (JSONObject jsonAssociation : toAssociations) {
					associationsJsonMap.put(jsonAssociation.getString("id"), jsonAssociation);
				}
			}
		}
	}

	private static ModelSchema gerarAssociativa(String module, String databaseTable, String colunaFrom, String colunaTo) {
		ModelSchema.Builder tabelaBuilder = ModelSchema.create(module, databaseTable)
				.addProperty("id", Long.class, Constraint.primaryKey())
				.addProperty(colunaFrom, Long.class, Constraint.notNull())
				.addProperty(colunaTo, Long.class, Constraint.notNull());
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
	public void generateSqlResources(GeneratorConfig config, Collection<Table> tables) throws GeradorException {
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
		//if (version > 1)
		{
			String currentSchemaName = "schema_" + version + ".sql";
			File currentSchema = new File(outputDir, currentSchemaName);
			writeSchemasToOutput(tables, currentSchema);
		}
	}

	private static void writeSchemasToOutput(Collection<Table> tables, File output) throws GeradorException {
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

	private static BigDecimal extractNumber(String jsonValue) {
		if (jsonValue == null || "".equals(jsonValue)) {
			return null;
		}
		try {
			return new BigDecimal(jsonValue);
		} catch (NumberFormatException e) {
			LoggerUtil.getLog().error(e.getMessage());
			return null;
		}
	}

}