package com.quantium.mobile.geradores.velocity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
			String basePackage, String genPackage, String voPackage,
			File destDir)
			throws IOException
	{
		String classname = templatename;
		Template t = ve.getTemplate(classname);
		File file = new File(
				Utils.getPackageDir (destDir, genPackage, Constants.DEFAULT_MODULE_NAME),
				classname);
		Map<String,String> klasses = getKlassesMap(
				basePackage, genPackage, voPackage, daoSuffix, schemas);
		VelocityContext ctx = new VelocityContext();
		ctx.put("package",
		        Utils.getPackageName (basePackage, genPackage, Constants.DEFAULT_MODULE_NAME));
		ctx.put("Klasses", klasses);
//		ImportHelper importHelper = new ImportHelper (basePackage, genPackage, null);
//		ctx.put ("Imports", importHelper.getFactoryImports (daoSuffix, schemas));

		Writer writer = new OutputStreamWriter(
				new FileOutputStream(file),
				"UTF-8");
		t.merge(ctx, writer);
		writer.close();
	}

	public static Map<String,String> getKlassesMap (
			String basePackage, String genPackage, String voPackage,
			String daoSuffix,
			Collection<JavaBeanSchema> schemas) {
		Map<String,String> klasses = new HashMap<String, String>();
		for (JavaBeanSchema schema : schemas){
			if (schema.isNonEntityTable())
				continue;
			StringBuilder klassFullName = new StringBuilder();
			{
				String packageName = Utils.getPackageName (
						basePackage, voPackage, schema.getModule());
				klassFullName.append (packageName);
				klassFullName.append ('.');
				klassFullName.append (CamelCaseUtils.toUpperCamelCase(schema.getNome()));
			}

			StringBuilder daoFullName = new StringBuilder();
			{
				String packageName = Utils.getPackageName (
						basePackage, genPackage, schema.getModule());
				daoFullName.append (packageName);
				daoFullName.append ('.');
				daoFullName.append (CamelCaseUtils.toUpperCamelCase(schema.getNome()));
				daoFullName.append(daoSuffix);
			}

			klasses.put(klassFullName.toString(), daoFullName.toString());
		}
		return klasses;
	}

}
