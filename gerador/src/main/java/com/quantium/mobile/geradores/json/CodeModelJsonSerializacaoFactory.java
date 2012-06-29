package com.quantium.mobile.geradores.json;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;


import com.quantium.mobile.framework.utils.DateUtil;
import com.quantium.mobile.geradores.dao.CodeModelDaoFactory;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Propriedade;
import com.quantium.mobile.geradores.util.ColunasUtils;
import com.quantium.mobile.geradores.util.SQLiteGeradorUtils;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JVar;
import com.sun.codemodel.JCatchBlock;

public class CodeModelJsonSerializacaoFactory {

	JCodeModel jcm;

	public CodeModelJsonSerializacaoFactory(JCodeModel jcm) {
		this.jcm = jcm;
	}

	public void gerarMetodosDeSerializacaoJson(JDefinedClass klass,JavaBeanSchema javaBeanSchema){

		String primaryKeyNome = javaBeanSchema.getPrimaryKey()==null ?
				null :
				javaBeanSchema.getPrimaryKey().getNome();

		JMethod toJson = klass.method(JMod.PUBLIC, jcm.ref(JSONObject.class), "toJson");
		//toJson._throws(JSONException.class);
		JBlock corpoMetodo = toJson.body();
		JTryBlock tryBlock = corpoMetodo._try();
		JBlock corpo = tryBlock.body();
		JVar jsonObj = corpo.decl(jcm.ref(JSONObject.class), "jsonObj", JExpr._new(jcm.ref(JSONObject.class)));
		for(String coluna : ColunasUtils.colunasOrdenadasDoJavaBeanSchema(javaBeanSchema)){
			Propriedade propriedade = javaBeanSchema.getPropriedade(coluna);
			JFieldVar campo = klass.fields().get(propriedade.getNome());
			JInvocation setJsonField = jsonObj.invoke("put").arg(JExpr.lit(coluna));
			if(propriedade.getNome().equals(primaryKeyNome)){
				corpo._if(campo.ne(JExpr.lit(CodeModelDaoFactory.ID_PADRAO)))
					._then().add(setJsonField.arg(campo));
			}
			else {
			JExpression value = campo;
			if(propriedade.getType().equals(Date.class)){
				value = jcm.ref(DateUtil.class).staticInvoke("dateToString").arg(value);
			}
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

		/*
		 * public static jsonToObject(JsonObject json){
		 * 
		 */
		JMethod jsonToObjectWithPrototype = klass.method(JMod.PUBLIC, klass, "jsonToObjectWithPrototype");
		jsonToObjectWithPrototype._throws(JSONException.class);
		jsonObj = jsonToObjectWithPrototype.param(JSONObject.class, "json");
		corpo = jsonToObjectWithPrototype.body();
		JVar obj = corpo.decl(klass, "obj", JExpr.invoke("clone"));
		for(String coluna : ColunasUtils.colunasOrdenadasDoJavaBeanSchema(javaBeanSchema)){
			Propriedade propriedade = javaBeanSchema.getPropriedade(coluna);
			JInvocation value;
			value = jsonObj.invoke(SQLiteGeradorUtils.metodoGetDoJsonParaClasse(propriedade.getType()))
				.arg(coluna);
			if(propriedade.getNome().equals(primaryKeyNome)){
				value = jsonObj.invoke(SQLiteGeradorUtils.metodoOptDoJsonParaClasse(propriedade.getType()))
					.arg(coluna)
					.arg(JExpr.lit(CodeModelDaoFactory.ID_PADRAO));

			}
			else{
				value = jsonObj.invoke(SQLiteGeradorUtils.metodoGetDoJsonParaClasse(propriedade.getType()))
						.arg(coluna);
			}
			for(Associacao associacao : javaBeanSchema.getAssociacoes()){
				if(associacao instanceof AssociacaoOneToMany){
					AssociacaoOneToMany associacaoOneToMany = (AssociacaoOneToMany)associacao;
					if(associacaoOneToMany.getKeyToA().equals(coluna)){
						value = jsonObj.invoke(SQLiteGeradorUtils.metodoOptDoJsonParaClasse(propriedade.getType()))
								.arg(coluna)
								.arg(JExpr.lit(CodeModelDaoFactory.ID_PADRAO));
					}
				}
			}
			if(propriedade.getType().equals(Date.class)){
				value = jcm.ref(DateUtil.class).staticInvoke("stringToDate").arg(value);
			}
			corpo.assign(
					obj.ref(klass.fields().get(propriedade.getNome())),
					value
			);
		}
		corpo._return(obj);
		
		//Jmethod
	}

}
