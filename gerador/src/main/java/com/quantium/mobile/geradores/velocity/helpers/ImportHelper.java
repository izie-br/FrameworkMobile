package com.quantium.mobile.geradores.velocity.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.ModelSchema;
import com.quantium.mobile.geradores.util.Constants;
import com.quantium.mobile.geradores.velocity.Utils;

public class ImportHelper {

	private String basePackage;
	private String genPackage;
	private String voPackage;
	private String daoFactory;

	public ImportHelper(String basePackage, String genPackage, String voPackage, String daoFacotory) {
		this.basePackage = basePackage;
		this.genPackage = genPackage;
		this.voPackage = voPackage;
		this.daoFactory = daoFacotory;
	}

	public String getImports (
			JavaBeanSchema schema,
			Collection<JavaBeanSchema> allSchemas,
			Collection<OneToManyAssociationHelper> oneToMany,
			Collection<OneToManyAssociationHelper> manyToOne,
			Collection<ManyToManyAssociationHelper> manyToMany)
	{
		StringBuilder sb = new StringBuilder ();
		String module = schema.getModule ();
		Collection<AssociationHelper> allAssoc =
				new ArrayList<AssociationHelper> ();
		allAssoc.addAll (oneToMany);
		allAssoc.addAll (manyToOne);
		allAssoc.addAll (manyToMany);
		Map<String, AssociationHelper> map =
				new HashMap<String, AssociationHelper> ();
		for (AssociationHelper assoc : allAssoc) {
			//if (!assoc.getModule ().equals (module))
			map.put(module+"."+assoc.getKlass (), assoc);
		}
		for (AssociationHelper assoc : map.values ()) {
			String genPackageName = Utils.getPackageName (
					basePackage, genPackage, assoc.getModule ());
			String voPackageName = Utils.getPackageName (
					basePackage, voPackage, assoc.getModule ());
	
			sb.append ("import ");
			sb.append (voPackageName);
			sb.append ('.');
			sb.append (assoc.getKlass ());
			sb.append (";\n");

			sb.append ("import ");
			sb.append (genPackageName);
			sb.append ('.');
			sb.append (Utils.editableInterface(assoc.getKlass ()));
			sb.append (";\n");
		}
		if (daoFactory != null){
			String packageName = Utils.getPackageName (
					basePackage, genPackage, Constants.DEFAULT_MODULE_NAME);

			sb.append ("import ");
			sb.append (packageName);
			sb.append ('.');
			sb.append (this.daoFactory);
			sb.append (";\n");
		}
		return sb.toString ();
	}

	public JavaBeanSchema getJavaBeanSchemaFromModel (
			ModelSchema modelSchema,
			Collection<JavaBeanSchema> allSchemas)
	{
		for (JavaBeanSchema schema : allSchemas) {
			if (modelSchema.equals(schema.getModelSchema()))
				return schema;
		}
		throw new RuntimeException("JavaBeanSchema nao encontrado");
	}

}
