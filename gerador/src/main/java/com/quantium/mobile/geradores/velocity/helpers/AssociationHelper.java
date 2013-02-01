package com.quantium.mobile.geradores.velocity.helpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoManyToMany;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;
import com.quantium.mobile.geradores.util.ColumnsUtils;
import com.quantium.mobile.geradores.util.PluralizacaoUtils;

public class AssociationHelper {

	private String module;
	private String klass;
	private String pluralized;

	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getKlass() {
		return klass;
	}
	public void setKlass(String klass) {
		this.klass = klass;
	}
	public String getPluralized() {
		return pluralized;
	}
	public void setPluralized(String pluralized) {
		this.pluralized = pluralized;
	}

	public static void findAssociations(JavaBeanSchema schema, Collection<JavaBeanSchema> allSchemas,
			Collection<OneToManyAssociationHelper> manyToOne,
			Collection<OneToManyAssociationHelper> oneToMany,
			Collection<ManyToManyAssociationHelper> manyToMany)
	{
		String tablename = schema.getModelSchema ().getName ();
		Collection<Associacao> assocs = schema.getAssociacoes();
		if (assocs == null)
			return;
		for (Associacao assoc : assocs) {
			if (assoc instanceof AssociacaoManyToMany) {
				if (manyToMany == null)
					continue;
				AssociacaoManyToMany m2m = (AssociacaoManyToMany) assoc;
				ManyToManyAssociationHelper obj =
						extractManyToManyObject(m2m, schema, allSchemas);
				manyToMany.add(obj);
			} else if (assoc instanceof AssociacaoOneToMany) {
				AssociacaoOneToMany o2m = (AssociacaoOneToMany) assoc;
				if (tablename.equals(assoc.getTabelaB().getName ())) {
					if (manyToOne == null)
						continue;
					OneToManyAssociationHelper obj =
							extractOneToManyObject(o2m, schema, allSchemas);
					manyToOne.add(obj);
					continue;
				} else {
					if (oneToMany == null)
						continue;
					OneToManyAssociationHelper obj =
							extractOneToManyObject(o2m, schema, allSchemas);
					oneToMany.add(obj);
				}
			}
		}
	}

	protected static JavaBeanSchema findSchema(Collection<JavaBeanSchema> allSchemas, String assocTableName, String module) {
		JavaBeanSchema assocSchema = null;
		for (JavaBeanSchema sch : allSchemas) {
			if (sch.getModelSchema ().getName().equals(assocTableName) && sch.getModule().equals(module)) {
				assocSchema = sch;
				break;
			}
		}
		return assocSchema;
	}

	private static ManyToManyAssociationHelper
	extractManyToManyObject(
			AssociacaoManyToMany m2m, JavaBeanSchema schema,
			Collection<JavaBeanSchema> allSchemas)
	{
		String tablename = schema.getModelSchema ().getName();
		ManyToManyAssociationHelper obj = new ManyToManyAssociationHelper ();
		JavaBeanSchema schemaA, schemaB;
		String klassname;
		if (tablename.equals(m2m.getTabelaB().getName())) {
			String assocTableName = m2m.getTabelaA().getName();
			JavaBeanSchema assocSchema = findSchema(allSchemas, assocTableName, m2m.getTabelaA().getModule());
			schemaA = assocSchema;
			schemaB = schema;
			obj.setThisTableA(false);
			obj.setModule (assocSchema.getModule ());
			klassname = CamelCaseUtils.toUpperCamelCase(assocSchema.getNome());
		} else {
			String assocTableName = m2m.getTabelaB().getName();
			JavaBeanSchema assocSchema = findSchema(allSchemas, assocTableName, m2m.getTabelaB().getModule());
			schemaA = schema;
			schemaB = assocSchema;
			obj.setThisTableA(true);
			obj.setModule (assocSchema.getModule ());
			klassname = CamelCaseUtils.toUpperCamelCase(assocSchema.getNome());
		}
		obj.setJoinTable(m2m.getTabelaJuncao().getName ());
		Property refPropA = schemaA.getPropriedade(m2m.getReferenciaA());
		Property keyPropA = new Property(m2m.getKeyToA(), refPropA.getPropertyClass(), false, false);
		obj.setKeyToA(keyPropA);
		obj.setReferenceA(refPropA);
		Property refPropB = schemaB.getPropriedade(m2m.getReferenciaB());
		Property keyPropB = new Property(m2m.getKeyToB(), refPropB.getPropertyClass(), false, false);
		obj.setKeyToB(keyPropB);
		obj.setReferenceB(refPropB);
		obj.setKlass(klassname);
		obj.setPluralized(CamelCaseUtils.toUpperCamelCase(m2m.getKeyToA().substring(2))
				+ PluralizacaoUtils.pluralizar((String) klassname));
		return obj;
	}

	private static OneToManyAssociationHelper
	extractOneToManyObject(
			AssociacaoOneToMany o2m, JavaBeanSchema schema,
			Collection<JavaBeanSchema> allSchemas)
	{
		OneToManyAssociationHelper map = new OneToManyAssociationHelper ();
		String tablename = schema.getModelSchema ().getName();
		JavaBeanSchema schemaA, schemaB;
		JavaBeanSchema assocSchema;
		if (tablename.equals(o2m.getTabelaA().getName())) {
			String assocTableName = o2m.getTabelaB().getName();
			assocSchema = findSchema(allSchemas, assocTableName, o2m.getTabelaB().getModule());
			schemaA = schema;
			schemaB = assocSchema;
			String nome = CamelCaseUtils.toUpperCamelCase(assocSchema.getNome());
			String pluralized = PluralizacaoUtils.pluralizar(nome);
			map.setModule (assocSchema.getModule ());
			map.setKlass(nome);
			map.setPluralized(pluralized);
		} else {
			String assocTableName = o2m.getTabelaA().getName();
			assocSchema = findSchema(allSchemas, assocTableName, o2m.getTabelaA().getModule());
			schemaA = assocSchema;
			schemaB = schema;
			String nome = CamelCaseUtils.toUpperCamelCase(assocSchema.getNome());
			map.setModule (assocSchema.getModule ());
			map.setKlass(nome);
		}
		map.setTable(assocSchema.getTabela ());
		Property fkProp = schemaB.getPropriedade(o2m.getKeyToA());
		map.setForeignKey(fkProp);
		map.setKeyToA(CamelCaseUtils.toUpperCamelCase(o2m.getKeyToA().substring(2)));
		map.setKeyToAPluralized(
				CamelCaseUtils.toUpperCamelCase(o2m.getKeyToA().substring(2))
						+ PluralizacaoUtils.pluralizar((String) map.getKlass()));
		Property refProp = schemaA.getPropriedade(o2m.getReferenciaA());
		map.setReferenceKey(refProp);
		map.setNullable(o2m.isNullable() || ColumnsUtils.isNullable (fkProp));
		return map;
	}

	public static Map<Property, OneToManyAssociationHelper>
	getAssociationsForFK(
			List<Property> fields,
			List<OneToManyAssociationHelper> manyToOne)
	{
		Map<Property, OneToManyAssociationHelper> associationsFromFK =
				new HashMap<Property, OneToManyAssociationHelper>();
		for (OneToManyAssociationHelper o2mObj : manyToOne) {
			for (Property property : fields) {
				if (property.equals(o2mObj.getForeignKey()))
					associationsFromFK.put(property, o2mObj);
			}
		}
		return associationsFromFK;
	}


}
