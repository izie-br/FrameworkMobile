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
import com.quantium.mobile.geradores.filters.associacao.AssociacaoPorNomeFilter;
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
			System.out.println("sqlResource:" + sqlResource);
			System.out.println("sqlResource2:" + sqlResource.exists());
			inputStream = new FileInputStream(sqlResource);
			String fileContent = IOUtils.toString(inputStream);
			JSONObject json = new JSONObject(fileContent);
			List<TabelaSchema> tabelasJson = generateTableSchema(json);
			Collection<JavaBeanSchema> javaBeanSchemas = new ArrayList<JavaBeanSchema>();
			int dbVersion = information.retrieveDatabaseVersion();
			JavaBeanSchema.Factory factory = new JavaBeanSchema.Factory();
			factory.addFiltroFactory(new PrefixoTabelaFilter.Factory("tb_"));
			factory.addFiltroFactory(new AssociacaoPorNomeFilter.Factory("{COLUMN=id}_{TABLE}"));
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
			String packageName = jsonPackage.getString("name");
			for (JSONObject jsonClass : classes) {
				System.out.println("jsonClass:" + jsonClass);
				List<JSONObject> attributes = jsonArrayToList(jsonClass.optJSONArray(ATTRIBUTE_LIST));
				String databaseTable = jsonClass.getString("name");
				TabelaSchema.Builder tabelaBuilder = TabelaSchema.criar(databaseTable.toLowerCase());
				tabelaBuilder.adicionarColuna("_id", convertJsonTypeToJavaType("Long"),
						TabelaSchema.PRIMARY_KEY_CONSTRAINT);
				for (JSONObject jsonAttribute : attributes) {
					String attributeName = jsonAttribute.getString("name");
					tabelaBuilder.adicionarColuna(CamelCaseUtils.camelToLowerAndUnderscores(attributeName),
							convertJsonTypeToJavaType(jsonAttribute.getString("type")));
				}
				hashtable.put(jsonClass.getString("id"), tabelaBuilder);
			}
		}
		// Set<Entry<String, TabelaSchema.Builder>> keys = hashtable.entrySet();
		// while (keys.iterator().hasNext()) {
		// Entry<String, TabelaSchema.Builder> entry = keys.iterator().next();
		//
		// }
		for (JSONObject jsonPackage : packages) {
			List<JSONObject> classes = jsonArrayToList(jsonPackage.optJSONArray(CLASS_LIST));
			String packageName = jsonPackage.getString("name");
			for (JSONObject jsonClass : classes) {
				List<JSONObject> fromAssociations = jsonArrayToList(jsonClass.optJSONArray(FROM_ASSOCIATIONS_LIST));
				List<JSONObject> toAssociations = jsonArrayToList(jsonClass.optJSONArray(TO_ASSOCIATIONS_LIST));
				TabelaSchema.Builder builder = hashtable.get(jsonClass.getString("id"));
				// List<JSONObject> toAssociations =
				// jsonArrayToList(json.getJSONArray(TO_ASSOCIATIONS_LIST));
				for (JSONObject jsonAssociation : fromAssociations) {
					TabelaSchema from = hashtable.get(jsonAssociation.optString("from")).get();
					TabelaSchema to = hashtable.get(jsonAssociation.optString("to")).get();
					String colunaId = "_id";
					// TabelaSchema.Builder associativa =
					// gerarAssociativa(tableName.toLowerCase(), colunaFrom,
					// colunaTo);
					// associativa.setNonEntityTable(true);
					// hashtable.put(jsonAssociation.optString("id"),
					// associativa);
					if ("n..n".equals(jsonAssociation.optString("type"))) {
						builder.adicionarAssociacaoManyToMany(from, to, colunaId, colunaId);
					}
					if ("1..n".equals(jsonAssociation.optString("type"))) {
						builder.adicionarAssociacaoOneToMany(from, to, false, colunaId);
						hashtable.get(jsonAssociation.optString("to")).adicionarColuna(
								CamelCaseUtils.camelToLowerAndUnderscores("id_" + from.getNome()), Long.class);
					}
				}
				for (JSONObject jsonAssociation : toAssociations) {
					TabelaSchema from = hashtable.get(jsonAssociation.optString("from")).get();
					TabelaSchema to = hashtable.get(jsonAssociation.optString("to")).get();
					String colunaId = "_id";
					// TabelaSchema.Builder associativa =
					// gerarAssociativa(tableName.toLowerCase(), colunaFrom,
					// colunaTo);
					// associativa.setNonEntityTable(true);
					// hashtable.put(jsonAssociation.optString("id"),
					// associativa);
					if ("n..n".equals(jsonAssociation.optString("type"))) {
						builder.adicionarAssociacaoManyToMany(from, to, colunaId, colunaId);
					}
					if ("1..n".equals(jsonAssociation.optString("type"))) {
						builder.adicionarAssociacaoOneToMany(from, to, false, colunaId);
//						hashtable.get(jsonAssociation.optString("from")).adicionarColuna(
//								CamelCaseUtils.camelToLowerAndUnderscores("id_" + to.getNome()), Long.class);
					}
				}
			}
		}
		Set<Entry<String, Builder>> entryes = hashtable.entrySet();
		System.out.println("total:" + hashtable.size());
		Iterator<Entry<String, Builder>> it = entryes.iterator();
		int i = 0;
		while (it.hasNext()) {
			i++;
			System.out.println("i:" + i);
			// Entry<String, Builder> next = ;
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
		if (type.equals("Boolean")) {
			return java.lang.Boolean.class;
		}
		return java.lang.String.class;
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