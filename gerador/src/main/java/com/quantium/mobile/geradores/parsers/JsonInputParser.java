package com.quantium.mobile.geradores.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
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

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.geradores.GeneratorConfig;
import com.quantium.mobile.geradores.GeradorException;
import com.quantium.mobile.geradores.filters.PrefixoTabelaFilter;
import com.quantium.mobile.geradores.javabean.Constraint;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.tabelaschema.TabelaSchema;
import com.quantium.mobile.geradores.tabelaschema.TabelaSchema.Builder;

public class JsonInputParser implements InputParser {

	private static final String PACKAGE_LIST = "packageList";
	// private static final String TABLE_NAME_TEMPLATE = "%s_%s";
	public static final String INPUT_PARSER_IDENTIFIER = "json";
	private static final String CLASS_LIST = "classList";
	private static final String ATTRIBUTE_LIST = "attributeList";
	private static final String FROM_ASSOCIATIONS_LIST = "fromAssociationList";
	private static final String TO_ASSOCIATIONS_LIST = "toAssociationList";

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
			List<TabelaSchema> tabelasJson = generateTableSchema(json);
			Collection<JavaBeanSchema> javaBeanSchemas = new ArrayList<JavaBeanSchema>();
			// int dbVersion = information.retrieveDatabaseVersion();
			JavaBeanSchema.Factory factory = new JavaBeanSchema.Factory();
			factory.addFiltroFactory(new PrefixoTabelaFilter.Factory("tb_"));
			// gerando os JavaBeanSchemas
			for (TabelaSchema tabela : tabelasJson) {
				javaBeanSchemas.add(factory.javaBeanSchemaParaTabela(tabela));
			}
			return javaBeanSchemas;
		} catch (IOException e) {
			throw new GeradorException(e);
		} catch (JSONException e) {
			throw new GeradorException(e);
		}
	}

	Hashtable<String, TabelaSchema.Builder> hashtable = new Hashtable<String, TabelaSchema.Builder>();

	private List<TabelaSchema> generateTableSchema(JSONObject json) throws JSONException {
		List<TabelaSchema> list = new ArrayList<TabelaSchema>();
		List<JSONObject> packages = jsonArrayToList(json.optJSONArray(PACKAGE_LIST));
		for (JSONObject jsonPackage : packages) {
			List<JSONObject> classes = jsonArrayToList(jsonPackage.optJSONArray(CLASS_LIST));
			if (jsonPackage.optBoolean("isLibrary")) {
				continue;
			}
			for (JSONObject jsonClass : classes) {
				String databaseTable = jsonClass.getString("name");
				TabelaSchema.Builder tabelaBuilder = TabelaSchema.criar(CamelCaseUtils.camelToLowerAndUnderscores("Tb"
						+ databaseTable));
				tabelaBuilder.setClassName(databaseTable);
				tabelaBuilder.adicionarColuna("id", convertJsonTypeToJavaType("Long"),
						Constraint.Type.PRIMARY_KEY);
				hashtable.put(jsonClass.getString("id"), tabelaBuilder);
			}
		}
		for (JSONObject jsonPackage : packages) {
			List<JSONObject> classes = jsonArrayToList(jsonPackage.optJSONArray(CLASS_LIST));
			for (JSONObject jsonClass : classes) {
				List<JSONObject> fromAssociations = jsonArrayToList(jsonClass.optJSONArray(FROM_ASSOCIATIONS_LIST));
				List<JSONObject> toAssociations = jsonArrayToList(jsonClass.optJSONArray(TO_ASSOCIATIONS_LIST));
				TabelaSchema.Builder tabelaBuilder = hashtable.get(jsonClass.getString("id"));
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
						constraints = new Constraint.Type[] {
								Constraint.Type.NOT_NULL,
								Constraint.Type.UNIQUE
						};
					} else {
						if (isUnique) {
							constraints = new Constraint.Type[] { Constraint.Type.UNIQUE };
						} else if (isRequired) {
							constraints = new Constraint.Type[] { Constraint.Type.NOT_NULL };
						}
					}
					if (type == null) {
						if (hashtable.get(jsonAttribute.optString("type")) == null) {
							throw new IllegalArgumentException(String.format("Tipo complexo nao encontrado: %s",
									jsonAttribute.optString("type")));
						}
						String fkId = CamelCaseUtils.camelToLowerAndUnderscores("id_" + attributeName);
						tabelaBuilder.adicionarColuna(fkId, Long.class, constraints);
						TabelaSchema tabelaA = hashtable.get(jsonAttribute.optString("type")).get();
						TabelaSchema tabelaB = tabelaBuilder.get();
						tabelaBuilder.adicionarAssociacaoOneToMany(tabelaA, tabelaB, !isRequired, "id");
						hashtable.get(jsonAttribute.optString("type")).adicionarAssociacaoOneToMany(tabelaA, tabelaB,
								!isRequired, "id");
					} else {
						tabelaBuilder.adicionarColuna(CamelCaseUtils.camelToLowerAndUnderscores(attributeName), type,
								constraints);
					}
				}
				for (JSONObject jsonAssociation : fromAssociations) {
					TabelaSchema from = hashtable.get(jsonAssociation.optString("from")).get();
					TabelaSchema to = hashtable.get(jsonAssociation.optString("to")).get();
					String colunaId = "id";
					if ("n..n".equals(jsonAssociation.optString("type"))) {
						tabelaBuilder.adicionarAssociacaoManyToMany(from, to, colunaId, colunaId);
					}
				}
				for (JSONObject jsonAssociation : toAssociations) {
					TabelaSchema from = hashtable.get(jsonAssociation.optString("from")).get();
					TabelaSchema to = hashtable.get(jsonAssociation.optString("to")).get();
					String colunaId = "id";
					if ("n..n".equals(jsonAssociation.optString("type"))) {
						tabelaBuilder.adicionarAssociacaoManyToMany(from, to, colunaId, colunaId);
					}
				}
			}
		}
		Set<Entry<String, Builder>> entryes = hashtable.entrySet();
		Iterator<Entry<String, Builder>> it = entryes.iterator();
		while (it.hasNext()) {
			list.add(it.next().getValue().get());
		}
		return list;
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
		if (type.equals("_EWZWilp8EeKiP5ww_mNJrA")) {
			return java.util.Date.class;
		}
		if (type.equals("_EWZWj1p8EeKiP5ww_mNJrA")) {
			return java.lang.String.class;
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

}