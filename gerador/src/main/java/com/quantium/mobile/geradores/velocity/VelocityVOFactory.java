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
import com.quantium.mobile.geradores.GeradorDeBeans;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoManyToMany;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;
import com.quantium.mobile.geradores.util.ColumnsUtils;

public class VelocityVOFactory {

	private Template template;
	private File targetDirectory;
	private VelocityContext parentCtx;
	private Map<String,String> aliases;

	public VelocityVOFactory(VelocityEngine ve, File targetDirectory,
	                         String basePackage, String genPackage,
	                         Map<String,String> serializationAliases)
	{
		this.template = ve.getTemplate("VO.java");
		this.targetDirectory = targetDirectory;
		this.parentCtx = new VelocityContext();
		this.parentCtx.put("defaultId", GeradorDeBeans.DEFAULT_ID);
		this.parentCtx.put("package", genPackage);
		this.parentCtx.put("basePackage", basePackage);
		this.aliases = serializationAliases;
	}

	public void generateVOClass(
			JavaBeanSchema schema, Collection<JavaBeanSchema> allSchemas)
			throws IOException
	{
		String classname = CamelCaseUtils.toUpperCamelCase(schema.getNome());
		String filename = classname + ".java";
		File file = new File(targetDirectory, filename);
		VelocityContext ctx = new VelocityContext(parentCtx);
		ctx.put("table", schema.getTabela().getNome());
		ctx.put("Klass", classname);
		ctx.put("serialVersionUID", ""+generateSerialUID(schema)+"L");
		List<Object> manyToOne = new ArrayList<Object>();
		List<Object> oneToMany = new ArrayList<Object>();
		List<Object> manyToMany = new ArrayList<Object>();
		VelocityDaoFactory.findAssociations(
				schema, allSchemas, manyToOne,
				oneToMany, manyToMany);
		ctx.put("manyToOneAssociations", manyToOne);
		ctx.put("oneToManyAssociations", oneToMany);
		ctx.put("manyToManyAssociations", manyToMany);
		List<Property> fields = new ArrayList<Property>();
		List<Property> pks = new ArrayList<Property>();
		for (String col : ColumnsUtils.orderedColumnsFromJavaBeanSchema(schema)){
			Property prop = schema.getPropriedade(col);
			prop.setAlias(getAlias(aliases, classname, col));
			for (String pk : schema.getPrimaryKeyColumns()){
				if (col.equals(pk))
					pks.add(prop);
			}
			fields.add(prop);
		}
		ctx.put("fields", fields);
		if (pks.size()==1)
			ctx.put("primaryKey", pks.get(0));
		ctx.put("primaryKeys", pks);
		
		Map<Property, Object> associationsFromFK =
				VelocityDaoFactory.getAssociationsForFK(fields, manyToOne);
		ctx.put("associationForField", associationsFromFK);

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
			String other = assoc.getTabelaA().getNome();
			boolean hasmany = assoc instanceof AssociacaoManyToMany;
			if (other.equals(schema.getTabela().getNome())){
				other = assoc.getTabelaB().getNome();
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
