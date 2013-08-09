package com.quantium.mobile.geradores.parsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.izie.utils.mmtojson.Association;
import br.com.izie.utils.mmtojson.Attribute;
import br.com.izie.utils.mmtojson.Clazz;
import br.com.izie.utils.mmtojson.Parser;

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

public class MMInputParser implements InputParser {

	public static final String INPUT_PARSER_IDENTIFIER = "mm";

	@Override
	public Collection<JavaBeanSchema> getSchemas(GeneratorConfig config,
			Map<String, Object> defaultProperties) throws GeradorException {
		Map<Clazz, ModelSchema.Builder> allClasses = new HashMap<Clazz, ModelSchema.Builder>();
		Collection<JavaBeanSchema> javaBeanSchemas = new ArrayList<JavaBeanSchema>();
		JavaBeanSchema.Factory factory = new JavaBeanSchema.Factory();
		factory.addFiltroFactory(new ModuleNameOnTablePrefixFilter.Factory());
		factory.addFiltroFactory(new PrefixoTabelaFilter.Factory("tb_"));
		Parser parser = new Parser(config.getInputFile());
		List<Clazz> parsed = parser.doParse();
		for (Clazz clazz : parsed) {
			ModelSchema.Builder tabelaBuilder = ModelSchema.create(clazz
					.getModule(), CamelCaseUtils
					.camelToLowerAndUnderscores(clazz.getSingularName().replace(" ", "")));
			for (Attribute attribute : clazz.getAttributeList()) {
				Class<?> classType = attribute.getType();

				if (!classType.equals(List.class)
						&& !classType.equals(Clazz.class)) {
					tabelaBuilder.addProperty(CamelCaseUtils
							.camelToLowerAndUnderscores(attribute.getName()),
							classType, constraintsFromAttribute(attribute));
				}
			}
			tabelaBuilder.addProperty("id", String.class,
					Constraint.primaryKey());
			tabelaBuilder.addProperty("created_at", Date.class);
			tabelaBuilder.addProperty("last_modified", Date.class);
			allClasses.put(clazz, tabelaBuilder);
		}
		Set<Clazz> keys = allClasses.keySet();
		for (Clazz clazz : keys) {
			for (Association association : clazz.getAssociationList()) {
				String fkId = CamelCaseUtils.camelToLowerAndUnderscores("id_"
						+ association.getSource().getName());
				ModelSchema tabelaA = allClasses.get(association.getFrom())
						.get();
				ModelSchema tabelaB = allClasses.get(association.getTo()).get();
				// System.out.println("association:" + association);
				Associacao assoc = null;
				if (association.isHasOne()) {
					// System.out.println("Adicionando propriedade " + fkId
					// + " na tabela " + association.getFrom().getPluralName());
					allClasses.get(association.getFrom()).addProperty(fkId,
							String.class,
							constraintsFromAttribute(association.getSource()));
					assoc = new AssociacaoOneToMany(tabelaB, tabelaA, fkId,
							!association.getSource().isRequired(), "id");
				} else if (association.isHasMany()) {
					// System.out.println("Adicionando propriedade " + fkId
					// + " na tabela " + association.getTo().getPluralName());
					// allClasses.get(association.getTo()).addProperty(fkId,
					// String.class,
					// constraintsFromAttribute(association.getSource()));
					// assoc = new AssociacaoOneToMany(tabelaA, tabelaB, fkId,
					// !association.getSource().isRequired(), "id");
				} else if (association.isManyToMany()) {
					String fromName = CamelCaseUtils
							.camelToLowerAndUnderscores(association.getFrom()
									.getSingularName());
					String toName = CamelCaseUtils
							.camelToLowerAndUnderscores(association.getTo()
									.getSingularName());
					String colunaFrom = CamelCaseUtils
							.camelToLowerAndUnderscores("id_" + fromName);
					String colunaTo = CamelCaseUtils
							.camelToLowerAndUnderscores("id_" + toName);
					String tableName = "tb_" + fromName + "_join_" + toName;
					// a tabela join ficara no modulo da "from" da
					// associacao
					assoc = new AssociacaoManyToMany(tabelaA, tabelaB,
							colunaFrom, colunaTo, "id", "id", gerarAssociativa(
									clazz.getModule(), tableName, colunaFrom,
									colunaTo), tableName);
				} else {
					throw new GeradorException(
							"Tipo de relacionamento inválido na classe "
									+ clazz.getPluralName()
									+ " no relacionamento "
									+ association.getSource().getName());
				}
				if (assoc != null) {
					allClasses.get(clazz).addAssociation(assoc);
					allClasses.get(association.getTo()).addAssociation(assoc);
				}
			}

		}
		for (ModelSchema.Builder builder : allClasses.values()) {
			javaBeanSchemas
					.add(factory.javaBeanSchemaParaTabela(builder.get()));
		}
		return javaBeanSchemas;
	}

	private static ModelSchema gerarAssociativa(String module,
			String databaseTable, String colunaFrom, String colunaTo) {
		ModelSchema.Builder tabelaBuilder = ModelSchema
				.create(module, databaseTable)
				// .addProperty("id", String.class, Constraint.primaryKey())
				.addProperty(colunaFrom, String.class, Constraint.primaryKey())
				.addProperty(colunaTo, String.class, Constraint.primaryKey());
		return tabelaBuilder.get();
	}

	private Constraint[] constraintsFromAttribute(Attribute attribute) {
		Constraint constraints[] = null;
		{
			List<Constraint> constraintList = new ArrayList<Constraint>();
			if (attribute.isUnique())
				constraintList.add(Constraint.unique());
			if (attribute.isRequired())
				constraintList.add(Constraint.notNull());
			if (Long.class.equals(attribute.getType())) {
				if (attribute.getMin() != null)
					constraintList.add(Constraint.min(attribute.getMin()
							.intValue()));
				if (attribute.getMax() != null)
					constraintList.add(Constraint.max(attribute.getMax()
							.intValue()));
			} else if (Double.class.equals(attribute.getType())) {
				if (attribute.getMin() != null)
					constraintList.add(Constraint.min(attribute.getMin()
							.doubleValue()));
				if (attribute.getMax() != null)
					constraintList.add(Constraint.max(attribute.getMax()
							.doubleValue()));
			} else if (String.class.equals(attribute.getType())) {
				if (attribute.getMin() != null)
					constraintList.add(Constraint.min(attribute.getMin()
							.intValue()));
				if (attribute.getMax() != null)
					constraintList.add(Constraint.max(attribute.getMax()
							.intValue()));
				if (attribute.getLength() != null)
					constraintList.add(Constraint.length(attribute.getLength()
							.intValue()));
			}
			constraints = new Constraint[constraintList.size()];
			constraintList.toArray(constraints);
			return constraints;
		}
	}

	@Override
	public void generateSqlResources(GeneratorConfig config,
			Collection<Table> tables) throws GeradorException {
		File outputDir = config.getMigrationsOutputDir();
		if (outputDir == null)
			return;
		if (outputDir.exists() && !outputDir.isDirectory()) {
			throw new GeradorException(
					"Diretorio de saida de migracoes nao eh diretorio."
							+ " Arquivo encontrado em "
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
		// if (version > 1)
		{
			String currentSchemaName = "schema_" + version + ".sql";
			File currentSchema = new File(outputDir, currentSchemaName);
			writeSchemasToOutput(tables, currentSchema);
		}
	}

	private static void writeSchemasToOutput(Collection<Table> tables,
			File output) throws GeradorException {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(output), "UTF-8"));
			SQLiteSchemaGenerator generator = new SQLiteSchemaGenerator();
			for (Table table : tables) {
				writer.append(generator.getSchemaFor(table));
			}
			writer.close();
		} catch (Exception e) {
			throw new GeradorException(e);
		}

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
		if (type.contains("List of")) {
			return java.util.List.class;
		}
		if (type.contains("Object of")) {
			return java.lang.Object.class;
		}
		return null;
	}

}
