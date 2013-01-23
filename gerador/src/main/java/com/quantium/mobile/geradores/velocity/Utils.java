package com.quantium.mobile.geradores.velocity;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoManyToMany;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;
import com.quantium.mobile.geradores.util.PluralizacaoUtils;

public class Utils {

	public static final int HAS_NULLABLE_ASSOCIATION = 1;
	public static final int HAS_NOT_NULLABLE_ASSOCIATION = 1 << 1;
	public static final int HAS_DATE_FIELD = 1 << 2;
	public static final int HAS_DATE_PK = 1 << 3;

	public static int getOptions(JavaBeanSchema schema) {
		Collection<Associacao> assocs = schema.getAssociacoes();
		int returnValue = 0;
		if (!(assocs == null)) {
			for (Object obj : assocs) {
				if (!(obj instanceof AssociacaoOneToMany))
					continue;

				AssociacaoOneToMany o2m = (AssociacaoOneToMany) obj;
				if (!(o2m.getTabelaA().equals(schema.getTabela())))
					continue;

				if (o2m.isNullable())
					returnValue |= HAS_NULLABLE_ASSOCIATION;
				else
					returnValue |= HAS_NOT_NULLABLE_ASSOCIATION;
			}
		}
		Collection<String> fieldNames = schema.getColunas();
		if (fieldNames != null) {
			for (String fieldName : fieldNames) {
				Property prop = schema.getPropriedade(fieldName);
				if (prop.getKlass().equals(Date.class.getSimpleName())) {
					returnValue |= HAS_DATE_FIELD;
					if (prop.isPrimaryKey())
						returnValue |= HAS_DATE_PK;
				}
			}
		}
		return returnValue;
	}

	// Os maps de Nullable devem conter:
	// - Table com o nome da tabela
	// - ForeignKey com a Column da chave estrangeira
	// - ReferenceKey com a Column da tabela atual
	public static void findAssociations(JavaBeanSchema schema, Collection<JavaBeanSchema> allSchemas,
			Collection<Object> manyToOne, Collection<Object> oneToMany, Collection<Object> manyToMany) {
		String tablename = schema.getTabela().getNome();
		Collection<Associacao> assocs = schema.getAssociacoes();
		if (assocs == null)
			return;
		for (Associacao assoc : assocs) {
			if (assoc instanceof AssociacaoManyToMany) {
				if (manyToMany == null)
					continue;
				AssociacaoManyToMany m2m = (AssociacaoManyToMany) assoc;
				Object obj = extractManyToManyObject(m2m, schema, allSchemas);
				manyToMany.add(obj);
			} else if (assoc instanceof AssociacaoOneToMany) {
				AssociacaoOneToMany o2m = (AssociacaoOneToMany) assoc;
				if (tablename.equals(assoc.getTabelaB().getNome())) {
					if (manyToOne == null)
						continue;
					Object obj = extractOneToManyObject(o2m, schema, allSchemas);
					manyToOne.add(obj);
					continue;
				} else {
					if (oneToMany == null)
						continue;
					Object obj = extractOneToManyObject(o2m, schema, allSchemas);
					oneToMany.add(obj);
				}
			}
		}
	}

	protected static JavaBeanSchema findSchema(Collection<JavaBeanSchema> allSchemas, String assocTableName) {
		JavaBeanSchema assocSchema = null;
		for (JavaBeanSchema sch : allSchemas) {
			if (sch.getTabela().getNome().equals(assocTableName)) {
				assocSchema = sch;
				break;
			}
		}
		return assocSchema;
	}

	private static Object extractManyToManyObject(AssociacaoManyToMany m2m, JavaBeanSchema schema,
			Collection<JavaBeanSchema> allSchemas) {
		String tablename = schema.getTabela().getNome();
		HashMap<String, Object> map = new HashMap<String, Object>();
		JavaBeanSchema schemaA, schemaB;
		String klassname;
		if (tablename.equals(m2m.getTabelaB().getNome())) {
			String assocTableName = m2m.getTabelaA().getNome();
			JavaBeanSchema assocSchema = findSchema(allSchemas, assocTableName);
			schemaA = assocSchema;
			schemaB = schema;
			map.put("IsThisTableA", false);
			klassname = CamelCaseUtils.toUpperCamelCase(assocSchema.getNome());
		} else {
			String assocTableName = m2m.getTabelaB().getNome();
			JavaBeanSchema assocSchema = findSchema(allSchemas, assocTableName);
			schemaA = schema;
			schemaB = assocSchema;
			map.put("IsThisTableA", true);
			klassname = CamelCaseUtils.toUpperCamelCase(assocSchema.getNome());
		}
		String joinTableUpper = CamelCaseUtils.camelToUpper(CamelCaseUtils.toLowerCamelCase(m2m.getTabelaJuncao()
				.getNome()));
		map.put("JoinTableUpper", joinTableUpper);
		map.put("JoinTable", m2m.getTabelaJuncao().getNome());
		Property refPropA = schemaA.getPropriedade(m2m.getReferenciaA());
		Property keyPropA = new Property(m2m.getKeyToA(), refPropA.getPropertyClass(), false, false);
		map.put("KeyToA", keyPropA);
		map.put("ReferenceA", refPropA);
		Property refPropB = schemaB.getPropriedade(m2m.getReferenciaB());
		Property keyPropB = new Property(m2m.getKeyToB(), refPropB.getPropertyClass(), false, false);
		map.put("KeyToB", keyPropB);
		map.put("ReferenceB", refPropB);
		map.put("Klass", klassname);
		map.put("Pluralized", PluralizacaoUtils.pluralizar(klassname));
		return map;
	}

	private static Object extractOneToManyObject(AssociacaoOneToMany o2m, JavaBeanSchema schema,
			Collection<JavaBeanSchema> allSchemas) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		String tablename = schema.getTabela().getNome();
		JavaBeanSchema schemaA, schemaB;
		if (tablename.equals(o2m.getTabelaA().getNome())) {
			String assocTableName = o2m.getTabelaB().getNome();
			JavaBeanSchema assocSchema = findSchema(allSchemas, assocTableName);
			schemaA = schema;
			schemaB = assocSchema;
			String nome = CamelCaseUtils.toUpperCamelCase(assocSchema.getNome());
			String pluralized = PluralizacaoUtils.pluralizar(nome);
			map.put("Klass", nome);
			map.put("Pluralized", pluralized);
		} else {
			String assocTableName = o2m.getTabelaA().getNome();
			JavaBeanSchema assocSchema = findSchema(allSchemas, assocTableName);
			schemaA = assocSchema;
			schemaB = schema;
			String nome = CamelCaseUtils.toUpperCamelCase(assocSchema.getNome());
			map.put("Klass", nome);
		}
		if (o2m.getKeyToA().equals("id_owners")) {
			System.out.println("Pare");
		}
		map.put("Table", o2m.getTabelaB().getNome());
		Property fkProp = schemaB.getPropriedade(o2m.getKeyToA());
		map.put("ForeignKey", fkProp);
		map.put("KeyToA", CamelCaseUtils.toUpperCamelCase(o2m.getKeyToA().substring(2)));
		map.put("KeyToAPluralized",
				CamelCaseUtils.toUpperCamelCase(o2m.getKeyToA().substring(2))
						+ PluralizacaoUtils.pluralizar((String) map.get("Klass")));
		Property refProp = schemaA.getPropriedade(o2m.getReferenciaA());
		map.put("ReferenceKey", refProp);
		map.put("Nullable", o2m.isNullable());
		return map;
	}

	public static Map<Property, Object> getAssociationsForFK(List<Property> fields, List<Object> manyToOne) {
		Map<Property, Object> associationsFromFK = new HashMap<Property, Object>();
		for (Object o2mObj : manyToOne) {
			@SuppressWarnings("unchecked")
			Map<String, Object> o2mMap = (Map<String, Object>) o2mObj;
			for (Property property : fields) {
				if (property.equals(o2mMap.get("ForeignKey")))
					associationsFromFK.put(property, o2mObj);
			}
		}
		return associationsFromFK;
	}

}
