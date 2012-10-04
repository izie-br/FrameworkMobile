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

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.geradores.GeradorDeBeans;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;

public class VelocityDaoFactory {

	//private VelocityEngine ve;
	private File targetDirectory;
	private Template template;
	private VelocityContext parentCtx;

	public VelocityDaoFactory(VelocityEngine ve, File targetDirectory, String _package){
		//this.ve = ve;
		this.targetDirectory = targetDirectory;
		template = ve.getTemplate("DAO.java");
		parentCtx = new VelocityContext();
		parentCtx.put("defaultId", GeradorDeBeans.DEFAULT_ID);
		parentCtx.put("package", _package);
	}

	public void generateDAOImplementationClasses(JavaBeanSchema schema)
			throws IOException
	{
		String targetclass = schema.getNome();
		String classname = targetclass + "DAOimpl";
		String filename = classname + ".java";
		File file = new File(targetDirectory, filename);
		VelocityContext ctx = new VelocityContext(parentCtx);
		ctx.put("Class", classname);
		ctx.put("Target", targetclass);
		ctx.put("target",
		        Character.toLowerCase(targetclass.charAt(0)) +
		        targetclass.substring(1));
		ctx.put("table", schema.getTabela().getNome());
		List<ClassField> fields = new ArrayList<ClassField>();
		List<ClassField> pks = new ArrayList<ClassField>();
		for (String col : schema.getColunas()){
			String klassname = schema.getPropriedade(col)
					.getType().getSimpleName();
			ClassField f = new ClassField(klassname, col);
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

	public void generateDAOAbstractClasses(){
		
	}

	public class ClassField {
		String klass;
		String lowerAndUnderscores;
		public ClassField(String klass, String lowerAndUnderscores){
			this.klass = klass;
			this.lowerAndUnderscores = lowerAndUnderscores;
		}
		public String getKlass(){
			return klass;
		}
		public String getLowerCamel(){
			return CamelCaseUtils.toLowerCamelCase(lowerAndUnderscores);
		}
		public String getLowerAndUnderscores(){
			return lowerAndUnderscores;
		}
		public String getUpperAndUnderscores(){
			return CamelCaseUtils.camelToUpper(getLowerCamel());
		}
	}


}
