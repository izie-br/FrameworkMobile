package com.quantium.mobile.geradores.velocity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.geradores.GeradorDeBeans;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoManyToMany;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;
import com.quantium.mobile.geradores.util.ColumnsUtils;
import com.quantium.mobile.geradores.util.PluralizacaoUtils;

public class VelocityDaoFactory {

	//private VelocityEngine ve;
	private File targetDirectory;
	private Template template;
	private VelocityContext parentCtx;

	public VelocityDaoFactory(VelocityEngine ve, File targetDirectory, String genPackage){
		//this.ve = ve;
		this.targetDirectory = targetDirectory;
		template = ve.getTemplate("DAO.java");
		parentCtx = new VelocityContext();
		parentCtx.put("defaultId", GeradorDeBeans.DEFAULT_ID);
		parentCtx.put("package", genPackage);
		//parentCtx.put("basePackage", basePackage);
	}

	public void generateDAOAbstractClasses(
			JavaBeanSchema schema, Collection<JavaBeanSchema> allSchemas)
			throws IOException
	{
		generate(schema, "DAO", "GenericDAO", false, allSchemas);
	}

	public void generateDAOImplementationClasses(
			JavaBeanSchema schema, Collection<JavaBeanSchema> allSchemas)
			throws IOException
	{
		generate(schema, "DAOSQLite",
		         CamelCaseUtils.toUpperCamelCase(schema.getNome())+"DAO",
		         true, allSchemas);
	}

	private void generate(JavaBeanSchema schema, String suffix,
	                      String base, boolean implementation,
	                      Collection<JavaBeanSchema> allSchemas)
			throws IOException
	{
		if (schema.isNonEntityTable())
			return;
		String targetclass = CamelCaseUtils.toUpperCamelCase(schema.getNome());
		String classname = targetclass + suffix;
		String filename = classname + ".java";
		File file = new File(targetDirectory, filename);
		VelocityContext ctx = new VelocityContext(parentCtx);
		ctx.put("Klass", classname);
		ctx.put("BaseClass", base);
		ctx.put("Target", targetclass);
		ctx.put("table", schema.getTabela().getNome());
		List<Property> fields = new ArrayList<Property>();
		List<Property> pks = new ArrayList<Property>();
		for (String col : ColumnsUtils.orderedColumnsFromJavaBeanSchema(schema)){
			Property f = schema.getPropriedade(col);
			for (String pk : schema.getPrimaryKeyColumns()){
				if (col.equals(pk))
					pks.add(f);
			}
			fields.add(f);
		}
		ctx.put("fields", fields);
		if (pks.size()==1)
			ctx.put("primaryKey", pks.get(0));
		ctx.put("primaryKeys", pks);

		ArrayList<Object> oneToMany = new ArrayList<Object>();
		ArrayList<Object> manyToMany = new ArrayList<Object>();
		ArrayList<Object> manyToOne = new ArrayList<Object>();
		findAssociations(schema, allSchemas, manyToOne, oneToMany, manyToMany);
		ctx.put("oneToManyAssociations", oneToMany);
		ctx.put("manyToManyAssociations", manyToMany);
		ctx.put("manyToOneAssociations", manyToOne);
		//ctx.put("ForeignKeys", getForeignKeys(schema));

		Writer w = new OutputStreamWriter(
				new FileOutputStream(file),
				"UTF-8");
		template.merge(ctx, w);
		w.close();
	}

	//Os maps de Nullable devem conter:
	//   - Table com o nome da tabela
	//   - ForeignKey com a Column da chave estrangeira
	//   - ReferenceKey com a Column da tabela atual
	public static void findAssociations(
			JavaBeanSchema schema,
			Collection<JavaBeanSchema> allSchemas,
			Collection<Object> manyToOne,
			Collection<Object> oneToMany,
			Collection<Object> manyToMany)
	{
		String tablename = schema.getTabela().getNome();
		Collection<Associacao> assocs = schema.getAssociacoes();
		if (assocs == null)
			return;
		for (Associacao assoc : assocs){
			if (assoc instanceof AssociacaoManyToMany){
				if (manyToMany == null)
					continue;
				AssociacaoManyToMany m2m = (AssociacaoManyToMany) assoc;
				Object obj = extractManyToManyObject(m2m, schema, allSchemas);
				manyToMany.add(obj);
			}
			else if (assoc instanceof AssociacaoOneToMany){
				AssociacaoOneToMany o2m = (AssociacaoOneToMany) assoc;
				if (tablename.equals(assoc.getTabelaB().getNome())){
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

	protected static JavaBeanSchema findSchema(
			Collection<JavaBeanSchema> allSchemas, String assocTableName) {
		JavaBeanSchema assocSchema = null;
		for (JavaBeanSchema sch : allSchemas){
			if (sch.getTabela().getNome().equals(assocTableName)){
				assocSchema = sch;
				break;
			}
		}
		return assocSchema;
	}

	private static Object extractManyToManyObject(
			AssociacaoManyToMany m2m,
			JavaBeanSchema schema, Collection<JavaBeanSchema> allSchemas)
	{
		String tablename = schema.getTabela().getNome();
		HashMap<String, Object> map = new HashMap<String, Object>();
		JavaBeanSchema schemaA, schemaB;
		String klassname;
		if (tablename.equals(m2m.getTabelaB().getNome())){
			String assocTableName = m2m.getTabelaA().getNome();
			JavaBeanSchema assocSchema = findSchema(allSchemas,
					assocTableName);
			schemaA = assocSchema;
			schemaB = schema;
			map.put("IsThisTableA", false);
			klassname = CamelCaseUtils.toUpperCamelCase(
					assocSchema.getNome());
		} else {
			String assocTableName = m2m.getTabelaB().getNome();
			JavaBeanSchema assocSchema = findSchema(allSchemas,
					assocTableName);
			schemaA = schema;
			schemaB = assocSchema;
			map.put("IsThisTableA", true);
			klassname = CamelCaseUtils.toUpperCamelCase(
					assocSchema.getNome());
		}
		String joinTableUpper = CamelCaseUtils.camelToUpper(
				CamelCaseUtils.toLowerCamelCase(
						m2m.getTabelaJuncao().getNome()));
		map.put("JoinTableUpper", joinTableUpper);
		map.put("JoinTable", m2m.getTabelaJuncao().getNome());
		Property refPropA = schemaA.getPropriedade(
				m2m.getReferenciaA());
		Property keyPropA = new Property(
				m2m.getKeyToA(), refPropA.getPropertyClass(),
				false, false, false);
			map.put("KeyToA", keyPropA);
			map.put("ReferenceA", refPropA);
		Property refPropB = schemaB.getPropriedade(
				m2m.getReferenciaB());
		Property keyPropB = new Property(
				m2m.getKeyToB(), refPropB.getPropertyClass(),
				false, false, false);
		map.put("KeyToB", keyPropB);
		map.put("ReferenceB", refPropB);
		map.put("Klass", klassname);
		map.put("Pluralized", PluralizacaoUtils.pluralizar(
				klassname));
		return map;
	}

	private static Object extractOneToManyObject(
			AssociacaoOneToMany o2m,
			JavaBeanSchema schema, Collection<JavaBeanSchema> allSchemas)
	{
		HashMap<String, Object> map =
				new HashMap<String, Object>();
		String tablename = schema.getTabela().getNome();
		JavaBeanSchema schemaA, schemaB;
		if (tablename.equals(o2m.getTabelaA().getNome())){
			String assocTableName = o2m.getTabelaB().getNome();
			JavaBeanSchema assocSchema = findSchema(allSchemas,
					assocTableName);
			schemaA = schema;
			schemaB = assocSchema;
			String nome = CamelCaseUtils.toUpperCamelCase(
					assocSchema.getNome());
			String pluralized = PluralizacaoUtils.pluralizar(nome);
			map.put("Klass", nome);
			map.put("Pluralized", pluralized);
		} else {
			String assocTableName = o2m.getTabelaA().getNome();
			JavaBeanSchema assocSchema = findSchema(allSchemas,
					assocTableName);
			schemaA = assocSchema;
			schemaB = schema;
			String nome = CamelCaseUtils.toUpperCamelCase(
					assocSchema.getNome());
			map.put("Klass", nome);
		}
		map.put("Table", o2m.getTabelaB().getNome());
		Property fkProp = schemaB.getPropriedade(o2m.getKeyToA());
		map.put("ForeignKey", fkProp);
		Property refProp = schemaA.getPropriedade(o2m.getReferenciaA());
		map.put("ReferenceKey", refProp);
		map.put("Nullable", o2m.isNullable());
		return map;
	}

}
