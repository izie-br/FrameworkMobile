package com.quantium.mobile.geradores.velocity;

import java.io.File;
import java.util.Collection;
import java.util.Date;

import com.quantium.mobile.framework.utils.StringUtil;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;
import com.quantium.mobile.geradores.util.Constants;

public class Utils {

	public static final int HAS_NULLABLE_ASSOCIATION = 1;
	public static final int HAS_NOT_NULLABLE_ASSOCIATION = 1 << 1;
	public static final int HAS_DATE_FIELD = 1 << 2;
	public static final int HAS_DATE_PK = 1 << 3;

	public static int getOptions(JavaBeanSchema schema) {
		Collection<Associacao> assocs = schema.getAssociacoes();
		int returnValue = 0;
		if (!(assocs == null)) {
			for (Object obj : assocs) {
				if (!(obj instanceof AssociacaoOneToMany))
					continue;

				AssociacaoOneToMany o2m = (AssociacaoOneToMany) obj;
				if (!(o2m.getTabelaA().equals(schema.getModelSchema ())))
					continue;

				if (o2m.isNullable())
					returnValue |= HAS_NULLABLE_ASSOCIATION;
				else
					returnValue |= HAS_NOT_NULLABLE_ASSOCIATION;
			}
		}
		Collection<String> fieldNames = schema.getColunas();
		if (fieldNames != null) {
			for (String fieldName : fieldNames) {
				Property prop = schema.getPropriedade(fieldName);
				if (prop.getKlass().equals(Date.class.getSimpleName())) {
					returnValue |= HAS_DATE_FIELD;
					if (prop.isPrimaryKey())
						returnValue |= HAS_DATE_PK;
				}
			}
		}
		return returnValue;
	}

	public static String getPackageName (
			String basePackage, String genpackage,
			String module)
	{
		StringBuilder sb = new StringBuilder ();
		if (!StringUtil.isNull (basePackage)){
			sb.append (basePackage);
			sb.append ('.');
		}
		if (!Constants.DEFAULT_MODULE_NAME.equals (module)){
			sb.append (module);
			sb.append ('.');
		}
		sb.append (genpackage);
		return sb.toString ();
	}

	public static File getPackageDir (
			File baseDir, String genpackage,
			String module)
	{
		File f = baseDir;
		if (!Constants.DEFAULT_MODULE_NAME.equals (module)){
			f = new File (f, module);
		}
		if (genpackage == null)
			throw new RuntimeException ();
		f = new File (f, genpackage);
		if (!f.exists ())
			f.mkdirs ();
		return f;
	}

	public static String editableInterface(String targetclass) {
		return targetclass + "Editable";
	}



}
