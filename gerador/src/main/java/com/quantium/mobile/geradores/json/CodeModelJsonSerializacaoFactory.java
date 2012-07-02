package com.quantium.mobile.geradores.json;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;


import com.quantium.mobile.framework.utils.DateUtil;
import com.quantium.mobile.geradores.dao.CodeModelDaoFactory;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Propriedade;
import com.quantium.mobile.geradores.util.ColumnsUtils;
import com.quantium.mobile.geradores.util.SQLiteGeradorUtils;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JVar;
import com.sun.codemodel.JCatchBlock;

public class CodeModelJsonSerializacaoFactory {

	JCodeModel jcm;

	public CodeModelJsonSerializacaoFactory(JCodeModel jcm) {
		this.jcm = jcm;
	}

	/**
	 * Gera os metodos de serialização de de-serialização de json
	 * toJson() e jsonToObjectWithPrototype(JSONObject).
	 * 
	 * @param klass
	 * @param javaBeanSchema
	 */
	public void gerarMetodosDeSerializacaoJson(JDefinedClass klass,JavaBeanSchema javaBeanSchema){
		String primaryKeyNome = javaBeanSchema.getPrimaryKey()==null ?
				//
				null :
				//
				javaBeanSchema.getPrimaryKey().getNome();
		generateToJsonMethod(klass, javaBeanSchema, primaryKeyNome);
		generateJsonToObjectMethod(klass, javaBeanSchema, primaryKeyNome);
	}


	private void generateToJsonMethod(JDefinedClass klass,
			JavaBeanSchema javaBeanSchema, String primaryKeyNome) {
		/*
		 * public JSONObject toJson() {
		 *     try {
		 *         JSONObject jsonObj = new JSONObject();
		 *         if (id!= 0L) {
		 *             jsonObj.put("id", id);
		 *         }
		 *         jsonObj.put("campo", campo);
		 *         ...
		 *         jsonObj.put("data", DateUtil.dateToString(data));
		 *         ...
		 *         return jsonObj;
		 *     } catch (JSONException e) {
		 *         throw new RuntimeException(e);
		 *     }
		 * }
		 */
		JMethod toJson = klass.method(JMod.PUBLIC, jcm.ref(JSONObject.class), "toJson");
		JTryBlock tryBlock = toJson.body()._try();
		JBlock corpo = tryBlock.body();

		// JSONObject jsonObj = new JSONObject();
		JVar jsonObj = corpo.decl(jcm.ref(JSONObject.class), "jsonObj", JExpr._new(jcm.ref(JSONObject.class)));

		for(String coluna : ColumnsUtils.orderedColumnsFromJavaBeanSchema(javaBeanSchema)){
			Propriedade propriedade = javaBeanSchema.getPropriedade(coluna);
			JFieldVar campo = klass.fields().get(propriedade.getNome());

			// jsonObj.put("campo", ...
			JInvocation setJsonField = jsonObj.invoke("put").arg(JExpr.lit(coluna));
			if(propriedade.getNome().equals(primaryKeyNome)){
				/*
				 * if (id!= 0L) {
				 *     jsonObj.put("id", id);
				 * }
				 */
				corpo._if(campo.ne(JExpr.lit(CodeModelDaoFactory.ID_PADRAO)))
					._then().add(setJsonField.arg(campo));
			}
			else {
				JExpression value = campo;
				if(propriedade.getType().equals(Date.class)){
					value = jcm.ref(DateUtil.class).staticInvoke("dateToString").arg(value);
				}
				/*
				 * jsonObj.put("campo", campo);
				 *  // ou se for Date
				 * jsonObj.put("data", DateUtil.dateToString(data));
				 */
				corpo.add(setJsonField.arg(value));
			}
		}
		corpo._return(jsonObj);
		/* ************************************************
		 * o erro jsonexception nao deve ocorrer, a menos *
		 * que haja algo fundamentamentalmente errado     *
		 *                                                *
		 *  catch(JsonException e){                       *
		 *      throw new runtimeException(e);            *
		 *  }                                             *
		 *************************************************/
		JCatchBlock catchBlock = tryBlock._catch(jcm.ref(JSONException.class));
		JVar except = catchBlock.param("e");
		catchBlock.body()
			._throw(JExpr._new(jcm.ref(RuntimeException.class)).arg(except));
	}

	private void generateJsonToObjectMethod(JDefinedClass klass,
			JavaBeanSchema javaBeanSchema, String primaryKeyNome) {
		/*
		 * public Customer jsonToObjectWithPrototype(JSONObject json)
		 *     throws JSONException
		 * {
		 *     Customer obj = clone();
		 *     obj.id = json.optLong("id", 0L);
		 *     obj.campo = json.optString("campo",campo);
		 *     obj.data = DateUtil.stringToDate(
		 *         json.optString("data", DateUtil.dateToString(data))
		 *     );
		 *     return obj;
		 * }
		 */
		JMethod jsonToObjectWithPrototype = klass.method(JMod.PUBLIC, klass, "jsonToObjectWithPrototype");
		jsonToObjectWithPrototype._throws(JSONException.class);
		JVar jsonObj = jsonToObjectWithPrototype.param(JSONObject.class, "json");
		JBlock corpo = jsonToObjectWithPrototype.body();

		// Customer obj = clone();
		JVar obj = corpo.decl(klass, "obj", JExpr.invoke("clone"));

		for(String coluna : ColumnsUtils.orderedColumnsFromJavaBeanSchema(javaBeanSchema)){
			Propriedade propriedade = javaBeanSchema.getPropriedade(coluna);
			// obj.campo = json.optString("campo",campo);
			JInvocation value = jsonObj.invoke(
					SQLiteGeradorUtils.metodoOptDoJsonParaClasse(
						propriedade.getType()
					)
				).arg(coluna);
			if(propriedade.getNome().equals(primaryKeyNome)){
				// obj.id = json.optLong("id", 0L);
				value = value.arg(JExpr.lit(CodeModelDaoFactory.ID_PADRAO));
			} else if(propriedade.getType().equals(Date.class)){
				/*
				 *     obj.data = DateUtil.stringToDate(
				 *         json.optString("data", DateUtil.dateToString(data))
				 *     );
				 */
				value = jcm.ref(DateUtil.class).staticInvoke("stringToDate")
					.arg(
						value.arg(
							jcm.ref(DateUtil.class).staticInvoke("dateToString")
							.arg(klass.fields().get(propriedade.getNome()))
						)
					);
			} else {
				value = value.arg(klass.fields().get(propriedade.getNome()));
			}
			corpo.assign(
					obj.ref(klass.fields().get(propriedade.getNome())),
					value
			);
		}
		corpo._return(obj);
	}

}
