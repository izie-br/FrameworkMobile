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
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoManyToMany;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;
import com.quantium.mobile.geradores.util.ColumnsUtils;
import com.quantium.mobile.geradores.util.Constants;
import com.quantium.mobile.geradores.velocity.helpers.ConstraintsHelper;
import com.quantium.mobile.geradores.velocity.helpers.ConstructorArgsHelper;
import com.quantium.mobile.geradores.velocity.helpers.GetterHelper;
import com.quantium.mobile.geradores.velocity.helpers.ImportHelper;
import com.quantium.mobile.geradores.velocity.helpers.ManyToManyAssociationHelper;
import com.quantium.mobile.geradores.velocity.helpers.OneToManyAssociationHelper;
import com.quantium.mobile.geradores.velocity.helpers.ValidateHelper;
import com.quantium.mobile.geradores.velocity.helpers.AssociationHelper.AssociationHolder;

import static com.quantium.mobile.geradores.velocity.Utils.*;
import static com.quantium.mobile.geradores.velocity.helpers.AssociationHelper.*;

public class VelocityVOFactory {

	public enum Type {
		INTERFACE, EDITABLE_INTERFACE , IMPLEMENTATION;

		public String getFilenameFor(JavaBeanSchema schema) {
			String classname = CamelCaseUtils.toUpperCamelCase(
					schema.getNome());
			switch (this) {
			case INTERFACE:
				return classname + "Gen";
			case IMPLEMENTATION:
				return "Abstract" + classname;
			case EDITABLE_INTERFACE:
				return classname + "Editable";
			default:
				throw new RuntimeException();
			}
		}
	}

	private Template template;
	private String basePackage;
	private String genPackage;
	private String voPackage;
	private File targetDirectory;
	private VelocityContext parentCtx;
	private Map<String,String> aliases;

	public VelocityVOFactory(VelocityEngine ve, File targetDirectory,
	                         String basePackage, String genPackage,
	                         String voPackage,
	                         Map<String,String> serializationAliases)
	{
		this.template = ve.getTemplate("VO.java");
		this.basePackage = basePackage;
		this.genPackage = genPackage;
		this.voPackage = voPackage;
		this.targetDirectory = targetDirectory;
		this.parentCtx = new VelocityContext();
		this.parentCtx.put("defaultId", Constants.DEFAULT_ID);
		this.parentCtx.put("basePackage", basePackage);
		this.parentCtx.put("getter", new GetterHelper());
		this.aliases = serializationAliases;
	}

	public void generateVO(
			JavaBeanSchema schema, Collection<JavaBeanSchema> allSchemas,
			VelocityVOFactory.Type type)
			throws IOException
	{
		String classname = CamelCaseUtils.toUpperCamelCase(schema.getNome());
		VelocityContext ctx = new VelocityContext(parentCtx);
		ctx.put("package", Utils.getPackageName (
				basePackage, genPackage, schema.getModule ()
		));

		ctx.put("interface", type == Type.INTERFACE);
		ctx.put("editableInterface", type == Type.EDITABLE_INTERFACE);
		ctx.put("implementation", type == Type.IMPLEMENTATION);

		String filename = type.getFilenameFor(schema);
		ctx.put("Filename", filename);
		String editableInterfaceName = Type.EDITABLE_INTERFACE.getFilenameFor(schema);
		ctx.put("EditableInterface", editableInterfaceName);
		ctx.put("table", schema.getTabela().getName ());
		ctx.put("serialVersionUID", ""+generateSerialUID(schema)+"L");
		ctx.put("Klass", classname);

		AssociationHolder holder = findAssociations(schema, allSchemas);
		ArrayList<OneToManyAssociationHelper> oneToMany = holder.getOneToMany();
		ArrayList<ManyToManyAssociationHelper> manyToMany = holder.getManyToMany();
		ArrayList<OneToManyAssociationHelper> manyToOne = holder.getManyToOne();
		ctx.put("manyToOneAssociations", manyToOne);
		ctx.put("oneToManyAssociations", oneToMany);
		ctx.put("manyToManyAssociations", manyToMany);
		List<Object> toMany = new ArrayList<Object>();
		toMany.addAll(oneToMany);
		toMany.addAll(manyToMany);
		ctx.put("toManyAssociations", toMany);
		List<Property> fields = new ArrayList<Property>();
		for (String col : ColumnsUtils.orderedColumnsFromJavaBeanSchema(schema)){
			Property prop = schema.getPropriedade(col);
			prop = new Property (
					prop.getNome (), prop.getPropertyClass (),
					prop.isGet (), prop.isSet (),
					getAlias(aliases, classname, col),
					prop.getConstraints());
			fields.add(prop);
		}
		ctx.put("fields", fields);
		ctx.put("primaryKey", schema.getPrimaryKey());

		int options = getOptions(schema);
		ctx.put("haveDateField", (options&HAS_DATE_FIELD)!=0);
		ctx.put("hasDatePK", (options&HAS_DATE_PK)!=0);

		Map<Property, OneToManyAssociationHelper> associationsFromFK =
				getAssociationsForFK(fields, manyToOne);

		ConstructorArgsHelper argsHelper = new ConstructorArgsHelper(
				schema, fields, associationsFromFK, oneToMany, manyToMany);

		ImportHelper importHelper =
				new ImportHelper (basePackage, genPackage, voPackage, null);
		String imports = importHelper.getImports (
				schema, allSchemas, oneToMany, manyToOne, manyToMany);
		if (type == Type.EDITABLE_INTERFACE) {
			String packageName = Utils.getPackageName(basePackage, voPackage, schema.getModule());
			imports += String.format("import %s.%s;\n", packageName, classname);
		}
		ctx.put("Imports", imports);

		ctx.put("associationForField", associationsFromFK);
		ctx.put("constructorArgsDecl", argsHelper.getConstructorArgsDecl());

		if (type == VelocityVOFactory.Type.IMPLEMENTATION) {
			ValidateHelper vhelper = new ValidateHelper (schema, fields);
			ctx.put ("NotNull", vhelper.getNotNull ());
			ctx.put ("MaxConstraints", vhelper.getMax());
			ctx.put ("MinConstraints", vhelper.getMin());
			ctx.put ("LenghtConstraints", vhelper.getLength());
		}

		ctx.put ("Constraints", new ConstraintsHelper ());

		File file = new File(
				Utils.getPackageDir (targetDirectory, genPackage, schema.getModule ()),
				filename + ".java");
		Writer w = new OutputStreamWriter(
				new FileOutputStream(file),
				"UTF-8");
		template.merge(ctx, w);
		w.close();
	}

	public static String getAlias(Map<String, String> aliases,
	                               String classname, String field){
		String name = classname + '.' + field;
		if (aliases != null){
			for (String k : aliases.keySet()){
				if ( CamelCaseUtils.camelEquals(name,k)){
					return aliases.get(k);
				}
			}
			String aliasForAllClasses = aliases.get(field);
			if (aliasForAllClasses != null) {
				return aliasForAllClasses;
			}
		}
		return field;
	}


	private long generateSerialUID(JavaBeanSchema schema){
		long result = 1;
		Collection<String> columns = schema.getColunas();
		for(String key : columns){
			Property prop = schema.getPropriedade(key);
			// este e o algoritmo de gerar o numero arbitrario
			// esta linha pode ser alterada com algo que faca sentido
			result += result*prop.getNome().hashCode() +
			         prop.getPropertyClass().getName().hashCode();
		}
		for (Associacao assoc : schema.getAssociacoes()){
			String other = assoc.getTabelaA().getName ();
			boolean hasmany = assoc instanceof AssociacaoManyToMany;
			if (other.equals(schema.getTabela().getName ())){
				other = assoc.getTabelaB().getName ();
				if (assoc instanceof AssociacaoOneToMany)
					hasmany = true;
			}
			// este e o algoritmo de gerar o numero arbitrario
			// esta linha pode ser alterada com algo que faca sentido
			result += other.hashCode() + (hasmany? result : 0);
		}
		return result;
	}

}
