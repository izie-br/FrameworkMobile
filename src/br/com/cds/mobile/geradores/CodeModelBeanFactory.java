package br.com.cds.mobile.geradores;

import java.util.Collection;
import java.util.HashSet;
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

//TODO hashcode e equals


public class CodeModelBeanFactory {

	public static final String ID_PADRAO = "-1l";
	private static final String ERRO_MSG_ARGUMENTO_NULL = "argumento null passado para o metodo %s::%s";
	private JCodeModel jcm;

	public CodeModelBeanFactory(JCodeModel jcm){
		this.jcm = jcm;
	}

	public void gerarGetterAndSetter(JDefinedClass klass, String nomeCampo) {
		gerarGetter(klass, nomeCampo);
		gerarSetter(klass, nomeCampo);
	}

	public void gerarSetter(JDefinedClass klass, String nomeCampo) {
		JFieldVar campo = klass.fields().get(nomeCampo);
		// nomeDoCampo -> NomeDoCampo
		String nomeCampoCaptalizado = ""+Character.toUpperCase(campo.name().charAt(0))+campo.name().substring(1);
		/**************************************
		 *             SETTER                 *
		 *                                    *
		 * public void setCampo(Campo campo){ *
		 *     this.campo = campo;            *
		 * }                                  *
		 *************************************/
		JMethod setter = klass.method(JMod.PUBLIC, jcm.VOID, "set"+nomeCampoCaptalizado);
		JVar parametroSetter = setter.param(campo.type(), campo.name());
		JBlock corpo = setter.body();
		// JExpr.refthis(campo.name()) == this.campo
		corpo.assign(JExpr.refthis(campo.name()), parametroSetter);
	}

	public void gerarGetter(JDefinedClass klass, String nomeCampo) {
		JFieldVar campo = klass.fields().get(nomeCampo);
		// nomeDoCampo -> NomeDoCampo
		String nomeCampoCaptalizado = ""+Character.toUpperCase(campo.name().charAt(0))+campo.name().substring(1);
		/*****************************
		 *        GETTER             *
		 *                           *
		 * public Campo getCampo(){  *
		 *     return campo;         *
		 * }                         *
		 ****************************/
		JMethod getter =
				// if(campo.type()==Boolean)
				(campo.type().name().equals("bool") || campo.type().name().equals("bool") ) ?
						// metodo = "isCampo"
						klass.method(JMod.PUBLIC, campo.type(), "is"+nomeCampoCaptalizado):
				// else
						// metodo = "getCampo"
						klass.method(JMod.PUBLIC, campo.type(), "get"+nomeCampoCaptalizado);
		JBlock corpo = getter.body();
		corpo._return(campo);
	}

	public void gerarHashCodeAndEquals(JDefinedClass klass, String regexDoNomeDoCampo){
		Collection<JFieldVar> camposUsados = new HashSet<JFieldVar>();
		for(JFieldVar campo : klass.fields().values())
			if(campo.name().matches(regexDoNomeDoCampo))
				camposUsados.add(campo);
		//TODO escrever hascode e equals
	}

	public JDefinedClass gerarPropriedades(JDefinedClass classeBean, Map<String,Class<?>> campos){
		for(String nomeCampo: campos.keySet()){
			JType tipo = getTipo(campos.get(nomeCampo));
			// campos privados
			JFieldVar campo = classeBean.field(JMod.PRIVATE, tipo, nomeCampo);
			// getters e setters
			// mas se for o id, gerar apenas o getter
			if(nomeCampo.equals("id")){
				gerarGetter(classeBean, nomeCampo);
				campo.init(JExpr.direct(ID_PADRAO));
			}
			else
				gerarGetterAndSetter(classeBean, nomeCampo);
		}
		return classeBean;
	}

	public JDefinedClass gerarClasse(String fullyqualifiedName)
			throws JClassAlreadyExistsException {
		JDefinedClass classeBean = jcm._class(
				JMod.PUBLIC,
				fullyqualifiedName,
				ClassType.CLASS
		);
		return classeBean;
	}

	public void gerarAssociacaoToOne(JDefinedClass klass, JDefinedClass estrangeira, String idEstrangeira){
		// private IdClass idEstrangeira;
		//JFieldVar idCampo = 
		klass.field(
				JMod.PRIVATE,
				estrangeira.fields().get(idEstrangeira).type(),
				idEstrangeira+estrangeira.name()
		);
		// private ClasseEstrangeira estrangeira;
		JFieldVar campo = klass.field(
				JMod.PRIVATE,
				estrangeira,
				estrangeira.name(),
				JExpr._null()
		);
		// getters e setters
		gerarGetterAndSetter(klass, campo.name());
	}

	public void gerarAssociacaoToMany(JDefinedClass klass, JDefinedClass estrangeira, String idEstrangeira){
		// private ClasseEstrangeira estrangeira;
		JFieldVar campo = klass.field(
				JMod.PRIVATE,
				jcm.ref(Collection.class).narrow(estrangeira),
				// TODO pluralizar
				estrangeira.name(),
				JExpr._null()
		);
		// getters e setters
		gerarGetterAndSetter(klass, campo.name());
	}

	public JType getTipo(Class<?> klass){
		if(klass==null)
			throw new RuntimeException(String.format(
					ERRO_MSG_ARGUMENTO_NULL,
					getClass().getName(),
					new Object(){}.getClass().getEnclosingMethod().getName()
			));
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
