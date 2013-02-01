package com.quantium.mobile.geradores.velocity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.util.Constants;

public class VelocityCustomClassesFactory {

	public static void generateDAOFactory(
			String templatename,
			String daoSuffix,
			VelocityEngine ve, Collection<JavaBeanSchema> schemas,
			String basePackage, String genPackage,
			File destDir)
			throws IOException
	{
		String classname = templatename;
		Template t = ve.getTemplate(classname);
		File file = new File(
				Utils.getPackageDir (destDir, genPackage, Constants.DEFAULT_MODULE_NAME),
				classname);
		List<String> klasses = new ArrayList<String>();
		for (JavaBeanSchema schema : schemas){
			if (schema.isNonEntityTable())
				continue;
			StringBuilder sb = new StringBuilder();
			String packageName = Utils.getPackageName (
					basePackage, genPackage, schema.getModule());
			sb.append (packageName);
			sb.append ('.');
			sb.append (CamelCaseUtils.toUpperCamelCase(schema.getNome()));
			klasses.add(sb.toString());
		}
		VelocityContext ctx = new VelocityContext();
		ctx.put("package",
		        Utils.getPackageName (basePackage, genPackage, Constants.DEFAULT_MODULE_NAME));
		ctx.put("Klasses", klasses);
		ctx.put ("DaoPrefix", daoSuffix);
//		ImportHelper importHelper = new ImportHelper (basePackage, genPackage, null);
//		ctx.put ("Imports", importHelper.getFactoryImports (daoSuffix, schemas));

		Writer writer = new OutputStreamWriter(
				new FileOutputStream(file),
				"UTF-8");
		t.merge(ctx, writer);
		writer.close();
	}

}
