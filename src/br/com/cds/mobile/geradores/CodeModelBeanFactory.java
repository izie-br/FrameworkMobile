package br.com.cds.mobile.geradores;

import java.util.Map;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

public class CodeModelBeanFactory {

	public CodeModelBeanFactory(JCodeModel jcm){
		this.jcm = jcm;
	}

	private JCodeModel jcm;

	public void gerarGettersSetters(JDefinedClass klass){
		Map<String,JFieldVar> campos = klass.fields();
		for(String nomeCampo : campos.keySet()){
			JFieldVar campo = campos.get(nomeCampo);
			// nomeDoCampo -> NomeDoCampo
			String nomeCampoCaptalizado = ""+Character.toUpperCase(nomeCampo.charAt(0))+nomeCampo.substring(1);
			/*****************************
			 *        GETTER             *
			 *                           *
			 * public Campo getCampo(){  *
			 *     return campo;         *
			 * }                         *
			 ****************************/
			JMethod getter = klass.method(JMod.PUBLIC, campo.type(), "get"+nomeCampoCaptalizado);
			JBlock corpo = getter.body();
			corpo._return(campo);
			/**************************************
			 *             SETTER                 *
			 *                                    *
			 * public void setCampo(Campo campo){ *
			 *     this.campo = campo;            *
			 * }                                  *
			 *************************************/
			JMethod setter = klass.method(JMod.PUBLIC, jcm.VOID, "set"+nomeCampoCaptalizado);
			JVar parametroSetter = setter.param(campo.type(), nomeCampo);
			corpo = setter.body();
			// JExpr.refthis(campo.name()) == this.campo
			corpo.assign(JExpr.refthis(campo.name()), parametroSetter);
		}
	}

	public JDefinedClass gerarJavaBean(String fullyqualifiedName, Map<String,Class<?>> campos){
		try {
			JDefinedClass classeBean = jcm._class(
					JMod.PUBLIC,
					fullyqualifiedName,
					ClassType.CLASS
			);
			// campos privados
			for(String nomeCampo: campos.keySet()){
				JType tipo = getTipo(campos.get(nomeCampo));
				classeBean.field(JMod.PRIVATE, tipo, nomeCampo);
			}
			// getters e setters
			gerarGettersSetters( classeBean);
			return classeBean;
		} catch (JClassAlreadyExistsException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public JType getTipo(Class<?> klass){
		if(klass==null)
			throw new RuntimeException("classe null passada para o metodo "+
					getClass().getName()+"::"+
						new Object(){}.getClass().getEnclosingMethod().getName());
		if(klass.equals(Byte.class))
			return jcm.BYTE;
		if(klass.equals(Short.class))
			return jcm.SHORT;
		if(klass.equals(Integer.class))
			return jcm.INT;
		if(klass.equals(Long.class))
			return jcm.LONG;
		if(klass.equals(Character.class))
			return jcm.CHAR;
		if(klass.equals(Float.class))
			return jcm.FLOAT;
		if(klass.equals(Double.class))
			return jcm.DOUBLE;
		if(klass.equals(Boolean.class))
			return jcm.BOOLEAN;
		return jcm._ref(klass);
	}

}
