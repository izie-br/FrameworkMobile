package com.quantium.mobile.geradores.json;

import java.util.Date;
import java.util.Map;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.framework.utils.DateUtil;
import com.quantium.mobile.geradores.dao.CodeModelDaoFactory;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Propriedade;
import com.quantium.mobile.geradores.util.ColumnsUtils;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JVar;

public class CodeModelMapSerializationFactory {

	public static final String TO_MAP_METHOD = "toMap";
	public static final String MAP_TO_OBJECT_METHOD = "mapToObject";

	private static final String DATEUTILS_STRING_TO_DATE_METHOD = "stringToDate";
	private static final String DATEUTILS_TO_STRING_METHOD = "timestampToString";

	JCodeModel jcm;

	public CodeModelMapSerializationFactory(JCodeModel jcm) {
		this.jcm = jcm;
	}

	/**
	 * Gera os metodos de serialização de de-serialização de json
	 * toJson() e jsonToObjectWithPrototype(JSONObject).
	 * 
	 * @param klass
	 * @param javaBeanSchema
	 */
	public void generateMapSerializationMethods(JDefinedClass klass,JavaBeanSchema javaBeanSchema){
		String primaryKeyNome = javaBeanSchema.getPrimaryKey()==null ?
				//
				null :
				//
				javaBeanSchema.getPrimaryKey().getNome();
		generateToMapMethod(klass, javaBeanSchema, primaryKeyNome);
		generateJsonToObjectMethod(klass, javaBeanSchema, primaryKeyNome);
	}


	private void generateToMapMethod(JDefinedClass klass,
			JavaBeanSchema javaBeanSchema, String primaryKeyNome) {
		/*
		 * public Map<String,Object> toMap(Map<String,Object> map) {
		 *     if (id!= 0L) {
		 *         map.put("id", id);
		 *     }
		 *     map.put("campo", campo);
		 *     ...
		 *     map.put("data", DateUtil.dateToString(data));
		 *     ...
		 *     return map;
		 * }
		 */
		// Map<String,Object>
		JClass mapStringObject = jcm.ref(Map.class).narrow(jcm.ref(String.class), jcm.ref(Object.class));

		JMethod toMap = klass.method(JMod.PUBLIC, mapStringObject, TO_MAP_METHOD);
		JVar map = toMap.param(mapStringObject, "map");
		JBlock corpo = toMap.body();

		for(String coluna : ColumnsUtils.orderedColumnsFromJavaBeanSchema(javaBeanSchema)){
			Propriedade propriedade = javaBeanSchema.getPropriedade(coluna);
			JFieldVar campo = klass.fields().get(propriedade.getNome());

			// map.put("campo", ...
			JInvocation setJsonField = map.invoke("put").arg(JExpr.lit(coluna));
			if(propriedade.getNome().equals(primaryKeyNome)){
				/*
				 * if (id!= 0L) {
				 *     map.put("id", id);
				 * }
				 */
				corpo._if(campo.ne(JExpr.lit(CodeModelDaoFactory.ID_PADRAO)))
					._then().add(setJsonField.arg(campo));
			}
			else {
				JExpression value = campo;
				if(propriedade.getType().equals(Date.class)){
					value = jcm.ref(DateUtil.class)
							.staticInvoke(DATEUTILS_TO_STRING_METHOD)
							.arg(value);
				}
				/*
				 * map.put("campo", campo);
				 *  // ou se for Date
				 * map.put("data", DateUtil.dateToString(data));
				 */
				corpo.add(setJsonField.arg(value));
			}
		}
		corpo._return(map);
	}

	private void generateJsonToObjectMethod(JDefinedClass klass,
			JavaBeanSchema javaBeanSchema, String primaryKeyNome) {
		/*
		 * public Customer mapToObject(Map<String,Object> map){
		 *     Customer obj = clone();
		 *     Object temp;
		 *     temp = map.get("id");
		 *     obj.id = (temp!=null) ? (Long)temp: id;
		 *     temp = map.get("campo");
		 *     obj.campo = (temp!=null) ? (String)temp :campo;
		 *     temp = map.get("data");
		 *     obj.data = (temp!=null) ? DateUtil.stringToDate((String)temp): data;
		 *     return obj;
		 * }
		 */
		// Map<String,Object>
		JClass mapStringObject = jcm.ref(Map.class).narrow(jcm.ref(String.class), jcm.ref(Object.class));

		JMethod mapToObject = klass.method(JMod.PUBLIC, klass, MAP_TO_OBJECT_METHOD);
		mapToObject._throws(ClassCastException.class);
		JVar map = mapToObject.param(mapStringObject, "map");
		JBlock body = mapToObject.body();

		JClass mapAnyCamelCaseClass = jcm.ref(CamelCaseUtils.AnyCamelMap.class).narrow(Object.class);
		JVar mapAnyCamelCase = body.decl(
				mapAnyCamelCaseClass,"mapAnyCamelCase",
				JExpr._new(mapAnyCamelCaseClass));
		body.add(mapAnyCamelCase.invoke("putAll").arg(map));
		// Customer obj = clone();
		JVar obj = body.decl(klass, "obj", JExpr.invoke("clone"));

		// Object temp;
		JVar temp = body.decl(jcm.ref(Object.class), "temp");

		for(String coluna : ColumnsUtils.orderedColumnsFromJavaBeanSchema(javaBeanSchema)){
			Propriedade propriedade = javaBeanSchema.getPropriedade(coluna);

			JExpression value = mapAnyCamelCase.invoke("get").arg(coluna);
			JFieldVar fieldVar = klass.fields().get(propriedade.getNome());
			JExpression alt = JExpr._this().ref(fieldVar);

			if(propriedade.getNome().equals(primaryKeyNome)){
				// obj.id = json.optLong("id", 0L);
				alt = JExpr.lit(CodeModelDaoFactory.ID_PADRAO);
			} else if(propriedade.getType().equals(Date.class)){
				/*
				 *     obj.data = DateUtil.stringToDate(
				 *         json.optString("data", DateUtil.dateToString(data))
				 *     );
				 */
				value = jcm.ref(DateUtil.class).staticInvoke(DATEUTILS_STRING_TO_DATE_METHOD)
						.arg(JExpr.cast(jcm.ref(String.class),value));
			}
			body.assign(temp, value);
			if (Number.class.isAssignableFrom(propriedade.getType())){
				body.assign(obj.ref(fieldVar),
						// (temp == null)? (Class)campo: ((Number)campo).longValue();
						JOp.cond(temp.ne(JExpr._null()),
								((JExpression)JExpr.cast(jcm.ref(Number.class),temp))
									.invoke(numberCastoToMethod(propriedade.getType())),
								alt) );
			} else {
				body.assign(obj.ref(fieldVar),
						// (temp == null)? (Class)campo: campo;
						JOp.cond(temp.ne(JExpr._null()),
								JExpr.cast(jcm.ref(propriedade.getType()),temp),
								alt) );
			}
		}
		body._return(obj);
	}

	private static String numberCastoToMethod(Class<?> klass){
		if (klass.getSimpleName().equals("Long"))
			return "longValue";
		if (klass.getSimpleName().equals("Ingeget"))
			return "intValue";
		if (klass.getSimpleName().equals("Short"))
			return "shortValue";
		if (klass.getSimpleName().equals("Byte"))
			return "byteValue";
		if (klass.getSimpleName().equals("Double"))
			return "doubleValue";
		if (klass.getSimpleName().equals("Float"))
			return "floatValue";
		throw new RuntimeException(
				klass.getName() + " nao mapeada em " +
				new Object(){}.getClass().getEnclosingMethod().getName());
	}

}
