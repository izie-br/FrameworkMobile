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

public class VelocityCustomClassesFactory {

	public static void generateDAOFactory(
			String templatename,
			VelocityEngine ve, Collection<JavaBeanSchema> schemas,
			String genPackage, File destDir)
			throws IOException
	{
		String classname = templatename;
		Template t = ve.getTemplate(classname);
		File file = new File(destDir, classname);
		List<String> klasses = new ArrayList<String>();
		for (JavaBeanSchema schema : schemas){
			if (schema.isNonEntityTable())
				continue;
			klasses.add(CamelCaseUtils.toUpperCamelCase(schema.getNome()));
		}
		VelocityContext ctx = new VelocityContext();
		ctx.put("package", genPackage);
		ctx.put("Klasses", klasses);
		Writer writer = new OutputStreamWriter(
				new FileOutputStream(file),
				"UTF-8");
		t.merge(ctx, writer);
		writer.close();
	}

}
