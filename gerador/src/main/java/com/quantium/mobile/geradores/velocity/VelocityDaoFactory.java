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
import com.quantium.mobile.geradores.velocity.helpers.ConstructorArgsHelper;
import com.quantium.mobile.geradores.velocity.helpers.GetterHelper;

import static com.quantium.mobile.geradores.velocity.Utils.*;

public class VelocityDaoFactory {

	public enum Type {

		ANDROID, JDBC;

		public String getTemplateName() {
			switch (this) {
			case ANDROID:
				return "DAO.java";
			case JDBC:
				return "JdbcDao.java";
			default:
				throw new RuntimeException();
			}
		}

		public String getSuffix() {
			switch (this) {
			case ANDROID:
				return "DAOSQLite";
			case JDBC:
				return "JdbcDAO";
			default:
				throw new RuntimeException();
			}
		}

	}

	private Type type;
	private File targetDirectory;
	private Template template;
	private VelocityContext parentCtx;
	private Map<String,String> aliases;

	public VelocityDaoFactory(
			VelocityEngine ve, File targetDirectory,
			Type type,
			String genPackage, Map<String,String> serializationAliases){
		//this.ve = ve;
		this.type = type;
		this.targetDirectory = targetDirectory;
		template = ve.getTemplate(type.getTemplateName());
		parentCtx = new VelocityContext();
		parentCtx.put("defaultId", Constants.DEFAULT_ID);
		parentCtx.put("package", genPackage);
		this.aliases = serializationAliases;
		//parentCtx.put("basePackage", basePackage);
	}

	public void generate(
			JavaBeanSchema schema,
			Collection<JavaBeanSchema> allSchemas)
			throws IOException
	{
		if (schema.isNonEntityTable())
			return;
		String targetclass = CamelCaseUtils.toUpperCamelCase(schema.getNome());
		String classname = targetclass + this.type.getSuffix();
		String filename = classname + ".java";
		File file = new File(targetDirectory, filename);
		VelocityContext ctx = new VelocityContext(parentCtx);
		ctx.put("Klass", classname);
		ctx.put("EditableInterface", targetclass + "Editable");
		ctx.put("KlassImpl", targetclass + "Impl");
		ctx.put("Target", targetclass);
		ctx.put("table", schema.getTabela().getNome());
		List<Property> fields = new ArrayList<Property>();
		Property primaryKey = null;
		for (String col : ColumnsUtils.orderedColumnsFromJavaBeanSchema(schema)){
			Property f = schema.getPropriedade(col);
			f.setAlias(VelocityVOFactory.getAlias(aliases, targetclass, col));
			for (String pk : schema.getPrimaryKeyColumns()){
				if (col.equals(pk)){
					if (primaryKey != null){
						throw new RuntimeException(
								schema.getNome() +
								" tem mais de uma primaryKey");
					}
					primaryKey = f;
				}
			}
			fields.add(f);
		}
		ctx.put("fields", fields);
		ctx.put("primaryKey", primaryKey);

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

		ConstructorArgsHelper argsHelper = new ConstructorArgsHelper(
				schema, fields, associationsFromFK, toMany);

		ctx.put("associationForField", associationsFromFK);
		ctx.put("constructorArgs", argsHelper.getConstructorArguments());

		GetterHelper getterHelper = new GetterHelper();
		ctx.put("getter", getterHelper);

		Writer w = new OutputStreamWriter(
				new FileOutputStream(file),
				"UTF-8");
		template.merge(ctx, w);
		w.close();
	}


}
