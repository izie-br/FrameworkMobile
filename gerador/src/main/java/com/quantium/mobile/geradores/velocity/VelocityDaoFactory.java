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
import com.quantium.mobile.geradores.Column;
import com.quantium.mobile.geradores.GeradorDeBeans;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoManyToMany;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Propriedade;
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
		generate(schema, "DAOSQLite", schema.getNome()+"DAO", true,
		         allSchemas);
	}

	private void generate(JavaBeanSchema schema, String suffix,
	                      String base, boolean implementation,
	                      Collection<JavaBeanSchema> allSchemas)
			throws IOException
	{
		if (schema.isNonEntityTable())
			return;
		String targetclass = schema.getNome();
		String classname = targetclass + suffix;
		String filename = classname + ".java";
		File file = new File(targetDirectory, filename);
		VelocityContext ctx = new VelocityContext(parentCtx);
		ctx.put("Klass", classname);
		ctx.put("BaseClass", base);
		ctx.put("implementation", implementation);
		ctx.put("Target", targetclass);
		ctx.put("target",
		        Character.toLowerCase(targetclass.charAt(0)) +
		        targetclass.substring(1));
		ctx.put("table", schema.getTabela().getNome());
		List<Column> fields = new ArrayList<Column>();
		List<Column> pks = new ArrayList<Column>();
		for (String col : ColumnsUtils.orderedColumnsFromJavaBeanSchema(schema)){
			String klassname = schema.getPropriedade(col)
					.getType().getSimpleName();
			Column f = new Column(klassname, col);
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
		findAssociations(schema, allSchemas, null, oneToMany, manyToMany);
		ctx.put("oneToManyAssociations", oneToMany);
		ctx.put("manyToManyAssociations", manyToMany);

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
			JavaBeanSchema schema, Collection<JavaBeanSchema> allSchemas,
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
				HashMap<String, Object> map = new HashMap<String, Object>();
				if (tablename.equals(assoc.getTabelaB().getNome())){
					String assocTableName = assoc.getTabelaA().getNome();
					JavaBeanSchema assocSchema = null;
					for (JavaBeanSchema sch : allSchemas){
						if (sch.getTabela().getNome().equals(assocTableName)){
							assocSchema = sch;
							break;
						}
					}
					map.put("IsThisTableA", false);
					String joinTableUpper = CamelCaseUtils.camelToUpper(
							CamelCaseUtils.toLowerCamelCase(
									m2m.getTabelaJuncao().getNome()));
					map.put("JoinTableUpper", joinTableUpper);
					map.put("JoinTable", m2m.getTabelaJuncao().getNome());
					Propriedade refAprop =
							schema.getPropriedade(m2m.getReferenciaA());
					Column keyColA = new Column(
							refAprop.getType().getSimpleName(),
							m2m.getKeyToA());
					Column refColA = new Column(
							refAprop.getType().getSimpleName(),
							m2m.getReferenciaA());
						map.put("KeyToA", keyColA);
						map.put("ReferenceA", refColA);
					Propriedade refBprop = assocSchema.getPropriedade(
							m2m.getReferenciaB());
					Column keyColB = new Column(
							refBprop.getType().getSimpleName(),
							m2m.getKeyToB());
					Column refColB = new Column(
							refBprop.getType().getSimpleName(),
							m2m.getReferenciaB());
					map.put("KeyToB", keyColB);
					map.put("ReferenceB", refColB);
					map.put("Klass", assocSchema.getNome());
					map.put("Pluralized", PluralizacaoUtils.pluralizar(
							assocSchema.getNome()));
				} else {
					String assocTableName = assoc.getTabelaB().getNome();
					JavaBeanSchema assocSchema = null;
					for (JavaBeanSchema sch : allSchemas){
						if (sch.getTabela().getNome().equals(assocTableName)){
							assocSchema = sch;
							break;
						}
					}
					map.put("IsThisTableA", true);
					String joinTableUpper = CamelCaseUtils.camelToUpper(
							CamelCaseUtils.toLowerCamelCase(
									m2m.getTabelaJuncao().getNome()));
					map.put("JoinTableUpper", joinTableUpper);
					map.put("JoinTable", m2m.getTabelaJuncao().getNome());
					Propriedade refAprop = assocSchema.getPropriedade(
							m2m.getReferenciaA());
					Column keyColA = new Column(
							refAprop.getType().getSimpleName(),
							m2m.getKeyToA());
					Column refColA = new Column(
						refAprop.getType().getSimpleName(),
						m2m.getReferenciaA());
					map.put("KeyToA", keyColA);
					map.put("ReferenceA", refColA);
					Propriedade refBprop = schema.getPropriedade(
							m2m.getReferenciaB());
					Column keyColB = new Column(
							refBprop.getType().getSimpleName(),
							m2m.getKeyToB());
					Column refColB = new Column(
							refBprop.getType().getSimpleName(),
							m2m.getReferenciaB());
					map.put("KeyToB", keyColB);
					map.put("ReferenceB", refColB);
					map.put("Klass", assocSchema.getNome());
					map.put("Pluralized", PluralizacaoUtils.pluralizar(
							assocSchema.getNome()));
					
				}
				manyToMany.add(map);
			}
			else if (assoc instanceof AssociacaoOneToMany){
				AssociacaoOneToMany o2m = (AssociacaoOneToMany) assoc;
				if (tablename.equals(assoc.getTabelaB().getNome())){
					if (manyToOne == null)
						continue;
					String assocTableName = assoc.getTabelaA().getNome();
					JavaBeanSchema assocSchema = null;
					for (JavaBeanSchema sch : allSchemas){
						if (sch.getTabela().getNome().equals(assocTableName)){
							assocSchema = sch;
							break;
						}
					}
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("Klass", assocSchema.getNome());
					Propriedade fkprop =
							schema.getPropriedade(o2m.getKeyToA());
					Column fk = new Column(fkprop.getType().getSimpleName(),
					                       o2m.getKeyToA());
					map.put("ForeignKey", fk);
					Propriedade refprop =
							assocSchema.getPropriedade(o2m.getReferenciaA());
					Column ref = new Column(
							refprop.getType().getSimpleName(),
							o2m.getReferenciaA());
					map.put("ReferenceKey", ref);
					manyToOne.add(map);
					continue;
				}
				String assocTableName = assoc.getTabelaB().getNome();
				JavaBeanSchema assocSchema = null;
				for (JavaBeanSchema sch : allSchemas){
					if (sch.getTabela().getNome().equals(assocTableName)){
						assocSchema = sch;
						break;
					}
				}
				if (oneToMany == null)
					continue;
				HashMap<String, Object> map =
						new HashMap<String, Object>();
				map.put("Table", o2m.getTabelaB().getNome());
				Propriedade fkprop =
						assocSchema.getPropriedade(o2m.getKeyToA());
				Column fk = new Column(fkprop.getType().getSimpleName(),
				                       o2m.getKeyToA());
				map.put("ForeignKey", fk);
				Propriedade refprop =
						schema.getPropriedade(o2m.getReferenciaA());
				Column ref = new Column(
						refprop.getType().getSimpleName(),
						o2m.getReferenciaA());
				map.put("ReferenceKey", ref);
				String nome = assocSchema.getNome();
				String pluralized = PluralizacaoUtils.pluralizar(nome);
				map.put("Klass", nome);
				map.put("Pluralized", pluralized);
				map.put("Nullable", o2m.isNullable());
				oneToMany.add(map);
			}
		}
	}

}
