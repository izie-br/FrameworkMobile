package com.quantium.mobile.geradores.velocity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;
import com.quantium.mobile.geradores.util.ColumnsUtils;
import com.quantium.mobile.geradores.util.Constants;

import static com.quantium.mobile.geradores.velocity.Utils.*;

public class VelocityDaoFactory {

	//private VelocityEngine ve;
	private File targetDirectory;
	private Template template;
	private VelocityContext parentCtx;
	private Map<String,String> aliases;

	public VelocityDaoFactory(
			String templateName,
			VelocityEngine ve, File targetDirectory,
			String genPackage, Map<String,String> serializationAliases){
		//this.ve = ve;
		this.targetDirectory = targetDirectory;
		template = ve.getTemplate(templateName);
		parentCtx = new VelocityContext();
		parentCtx.put("defaultId", Constants.DEFAULT_ID);
		parentCtx.put("package", genPackage);
		this.parentCtx.put("getter", new GetterFactory());
		this.aliases = serializationAliases;
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

	public void generate(JavaBeanSchema schema, String suffix,
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
		ctx.put("EditableInterface", targetclass + "Editable");
		ctx.put("KlassImpl", targetclass + "Impl");
		ctx.put("BaseClass", base);
		ctx.put("Target", targetclass);
		ctx.put("table", schema.getTabela().getNome());
		List<Property> fields = new ArrayList<Property>();
		List<Property> pks = new ArrayList<Property>();
		for (String col : ColumnsUtils.orderedColumnsFromJavaBeanSchema(schema)){
			Property f = schema.getPropriedade(col);
			f.setAlias(VelocityVOFactory.getAlias(aliases, targetclass, col));
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
		ctx.put("compoundPk", (pks.size() > 1) );

		ArrayList<Object> oneToMany = new ArrayList<Object>();
		ArrayList<Object> manyToMany = new ArrayList<Object>();
		ArrayList<Object> manyToOne = new ArrayList<Object>();
		findAssociations(schema, allSchemas, manyToOne, oneToMany, manyToMany);
		ctx.put("oneToManyAssociations", oneToMany);
		ctx.put("manyToManyAssociations", manyToMany);
		ctx.put("manyToOneAssociations", manyToOne);
		List<Object> toMany = new ArrayList<Object>();
		toMany.addAll(oneToMany);
		toMany.addAll(manyToMany);
		ctx.put("toManyAssociations", toMany);
		//ctx.put("ForeignKeys", getForeignKeys(schema));

		int options = getOptions(schema);
		ctx.put("hasNullableAssociation",
		        (options & HAS_NULLABLE_ASSOCIATION) != 0 );
		ctx.put("hasNotNullableAssociation",
		        (options & HAS_NOT_NULLABLE_ASSOCIATION) != 0 );
		ctx.put("hasDateField", 
				(options & HAS_DATE_FIELD) != 0 );

		Map<Property, Object> associationsFromFK =
				getAssociationsForFK(fields,manyToOne);
		ctx.put("associationForField", associationsFromFK);
		ctx.put("constructorArgs", ArgsFactory.getConstructorArguments(
				schema, fields, associationsFromFK, toMany));

		Writer w = new OutputStreamWriter(
				new FileOutputStream(file),
				"UTF-8");
		template.merge(ctx, w);
		w.close();
	}


}
