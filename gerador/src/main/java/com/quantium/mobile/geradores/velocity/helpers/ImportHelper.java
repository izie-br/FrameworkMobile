package com.quantium.mobile.geradores.velocity.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.util.Constants;
import com.quantium.mobile.geradores.velocity.Utils;

public class ImportHelper {

	private String basePackage;
	private String genPackage;
	private String daoFactory;

	public ImportHelper(String basePackage, String genPackage, String daoFacotory) {
		this.basePackage = basePackage;
		this.genPackage = genPackage;
		this.daoFactory = daoFacotory;
	}

	public String getImports (
			JavaBeanSchema schema,
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
			if (!assoc.getModule ().equals (module))
				map.put(module+"."+assoc.getKlass (), assoc);
		}
		for (AssociationHelper assoc : map.values ()) {
			String packageName = Utils.getPackageName (
					basePackage, genPackage, assoc.getModule ());
	
			sb.append ("import ");
			sb.append (packageName);
			sb.append ('.');
			sb.append (assoc.getKlass ());
			sb.append (";\n");

			sb.append ("import ");
			sb.append (packageName);
			sb.append ('.');
			sb.append (Utils.editableInterface(assoc.getKlass ()));
			sb.append (";\n");
		}
		if (!module.equals (Constants.DEFAULT_MODULE_NAME) &&
		    daoFactory != null)
		{
			String packageName = Utils.getPackageName (
					basePackage, genPackage, Constants.DEFAULT_MODULE_NAME);

			sb.append ("import ");
			sb.append (packageName);
			sb.append ('.');
			sb.append (daoFactory);
			sb.append (";\n");
		}
		return sb.toString ();
	}

	public String getFactoryImports (String daoSuffix, Collection<JavaBeanSchema> schemas) {
		StringBuilder sb = new StringBuilder ();
		for (JavaBeanSchema schema : schemas) {

			String module = schema.getModule ();
			if (module.equals (Constants.DEFAULT_MODULE_NAME))
				continue;

			String packageName = Utils.getPackageName (
					basePackage, genPackage, module);

			String nome = CamelCaseUtils.toUpperCamelCase (schema.getNome ());

			sb.append ("import ");
			sb.append (packageName);
			sb.append ('.');
			sb.append (nome);
			sb.append (";\n");

			sb.append ("import ");
			sb.append (packageName);
			sb.append ('.');
			sb.append (nome);
			sb.append (daoSuffix);
			sb.append (";\n");
		}
		return sb.toString ();
	}

}
