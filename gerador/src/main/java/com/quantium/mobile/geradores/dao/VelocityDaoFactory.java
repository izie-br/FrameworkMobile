package com.quantium.mobile.geradores.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.quantium.mobile.geradores.Column;
import com.quantium.mobile.geradores.GeradorDeBeans;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Propriedade;
import com.quantium.mobile.geradores.util.ColumnsUtils;

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

		ArrayList<Object> nullable = new ArrayList<Object>();
		ArrayList<Object> nonNullable = new ArrayList<Object>();
		findAssociations(schema, allSchemas, nullable, nonNullable);
		ctx.put("nullableAssociations", nullable);

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
	private void findAssociations(
			JavaBeanSchema schema, Collection<JavaBeanSchema> allSchemas,
			Collection<Object> nullable,
			Collection<Object> nonNullable)
	{
		String tablename = schema.getTabela().getNome();
		Collection<Associacao> assocs = schema.getAssociacoes();
		if (assocs == null)
			return;
		for (Associacao assoc : assocs){
			if (assoc instanceof AssociacaoOneToMany){
				if (tablename.equals(assoc.getTabelaA().getNome()))
					continue;
				String assocTableName = assoc.getTabelaB().getNome();
				JavaBeanSchema assocSchema = null;
				for (JavaBeanSchema sch : allSchemas){
					if (sch.getTabela().getNome().equals(assocTableName)){
						assocSchema = sch;
						break;
					}
				}
				AssociacaoOneToMany o2m = (AssociacaoOneToMany) assoc;
				if (o2m.isNullable()){
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
					nullable.add(map);
				}
			}
		}
	}

}
