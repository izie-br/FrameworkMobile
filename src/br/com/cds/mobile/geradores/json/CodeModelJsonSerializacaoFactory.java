package br.com.cds.mobile.geradores.json;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.cds.mobile.framework.utils.SQLiteUtils;
import br.com.cds.mobile.geradores.javabean.JavaBeanSchema;
import br.com.cds.mobile.geradores.javabean.Propriedade;
import br.com.cds.mobile.geradores.util.ColunasUtils;
import br.com.cds.mobile.geradores.util.SQLiteGeradorUtils;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

public class CodeModelJsonSerializacaoFactory {

	JCodeModel jcm;

	public CodeModelJsonSerializacaoFactory(JCodeModel jcm) {
		this.jcm = jcm;
	}

	public void gerarMetodosDeSerializacaoJson(JDefinedClass klass,JavaBeanSchema javaBeanSchema){
		JMethod toJson = klass.method(JMod.PUBLIC, jcm.ref(JSONObject.class), "toJson");
		toJson._throws(JSONException.class);
		JBlock corpo = toJson.body();
		JVar jsonObj = corpo.decl(jcm.ref(JSONObject.class), "jsonObj", JExpr._new(jcm.ref(JSONObject.class)));
		for(String coluna : ColunasUtils.colunasOrdenadasDoJavaBeanSchema(javaBeanSchema)){
			Propriedade propriedade = javaBeanSchema.getPropriedade(coluna);
			JExpression value = klass.fields().get(propriedade.getNome());
			if(propriedade.getType().equals(Date.class))
				value = jcm.ref(SQLiteUtils.class).staticInvoke("dateToString").arg(value);
			corpo.invoke(jsonObj, "put")
				.arg(JExpr.lit(coluna))
				.arg(value);
		}
		corpo._return(jsonObj);
		
		JMethod jsonToObject = klass.method(JMod.PUBLIC|JMod.STATIC, klass, "jsonToObject");
		jsonToObject._throws(JSONException.class);
		jsonObj = jsonToObject.param(JSONObject.class, "json");
		corpo = jsonToObject.body();
		JVar obj = corpo.decl(klass, "obj", JExpr._new(klass));
		for(String coluna : ColunasUtils.colunasOrdenadasDoJavaBeanSchema(javaBeanSchema)){
			Propriedade propriedade = javaBeanSchema.getPropriedade(coluna);
			JInvocation value = jsonObj.invoke(SQLiteGeradorUtils.metodoGetDoJsonParaClasse(propriedade.getType()))
				.arg(coluna);
			if(propriedade.getType().equals(Date.class))
				value = jcm.ref(SQLiteUtils.class).staticInvoke("stringToDate").arg(value);
			corpo.assign(
					obj.ref(klass.fields().get(propriedade.getNome())),
					value
			);
		}
		corpo._return(obj);
	}

}
