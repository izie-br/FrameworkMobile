package com.quantium.mobile.geradores.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.quantium.mobile.geradores.Column;
import com.quantium.mobile.geradores.GeradorDeBeans;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;

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

	public void generateDAOAbstractClasses(JavaBeanSchema schema)
			throws IOException
	{
		generate(schema, "DAO", "GenericDAO", false);
	}

	public void generateDAOImplementationClasses(JavaBeanSchema schema)
			throws IOException
	{
		generate(schema, "DAOImpl", schema.getNome()+"DAO", true);
	}

	private void generate(JavaBeanSchema schema, String suffix,
	                      String base, boolean implementation)
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
		for (String col : schema.getColunas()){
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
		Writer w = new OutputStreamWriter(
				new FileOutputStream(file),
				"UTF-8");
		template.merge(ctx, w);
		w.close();
	}

}
