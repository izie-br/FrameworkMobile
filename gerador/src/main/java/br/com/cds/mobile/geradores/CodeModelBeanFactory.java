package br.com.cds.mobile.geradores;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import br.com.cds.mobile.geradores.javabean.JavaBeanSchema;
import br.com.cds.mobile.geradores.javabean.Propriedade;
import br.com.cds.mobile.geradores.util.ColunasUtils;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;


public class CodeModelBeanFactory {

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
				(campo.type().name().equals("boolean") || campo.type().name().equals("Boolean") ) ?
						// metodo = "isCampo"
						klass.method(JMod.PUBLIC, campo.type(), "is"+nomeCampoCaptalizado):
				// else
						// metodo = "getCampo"
						klass.method(JMod.PUBLIC, campo.type(), "get"+nomeCampoCaptalizado);
		JBlock corpo = getter.body();
		corpo._return(campo);
	}

	public void gerarHashCodeAndEquals(JDefinedClass klass,JavaBeanSchema javaBeanSchema){
		Collection<JFieldVar> camposUsados = new ArrayList<JFieldVar>();

		for(String campoNome : ColunasUtils.colunasOrdenadasDoJavaBeanSchema(javaBeanSchema)) {
			JFieldVar campo = klass.fields().get(javaBeanSchema.getPropriedade(campoNome).getNome());
			if(
					campo!=null &&
					( (campo.mods().getValue()&JMod.TRANSIENT)==0 ) &&
					( (campo.mods().getValue()&JMod.STATIC)==0 ) &&
					( (campo.mods().getValue()&JMod.FINAL)==0 )
			)
				camposUsados.add(campo);
		}

		JMethod hashcode = klass.method(JMod.PUBLIC, jcm.INT, "hashCode");
		hashcode.annotate(Override.class);
		JBlock hashcodeBody = hashcode.body();
		JVar hashAccumulator = hashcodeBody.decl(jcm.INT, "value",JExpr.lit(1));

		for(JFieldVar campoUsado : camposUsados){
			if(campoUsado.type().equals(jcm.BOOLEAN)){
				hashcodeBody.assign(
						hashAccumulator,
						hashAccumulator.plus(JExpr.direct(campoUsado.name()+"?1:0"))
				);
			}
			else if(campoUsado.type().isPrimitive()){
				hashcodeBody.assign(
						hashAccumulator,
						hashAccumulator.plus(JExpr.cast(jcm.INT, campoUsado))
				);
			} else {
				hashcodeBody.assign(
						hashAccumulator,
						hashAccumulator.mul(campoUsado.invoke("hashCode"))
				);
			}
		}
		hashcodeBody._return(hashAccumulator);


		JMethod equals = klass.method(JMod.PUBLIC, jcm.BOOLEAN, "equals");
		JVar other = equals.param(Object.class, "obj");
		equals.annotate(Override.class);
		JBlock equalsBody = equals.body();

		equalsBody._if(
				other.eq(JExpr._null())
		)._then()._return(JExpr.lit(false));
		equalsBody._if(
				other._instanceof(klass).not()
		)._then()._return(JExpr.lit(false));
		other = equalsBody.decl(klass, "other",JExpr.cast(klass, other));

		for(JFieldVar campoUsado : camposUsados){
			if(campoUsado.type().isPrimitive()){
				equalsBody._if(
						other.ref(campoUsado).ne(campoUsado)
				)._then()._return(JExpr.lit(false));
			} else {
				equalsBody._if(
						JOp.cond(
								campoUsado.eq(JExpr._null()),
								other.ref(campoUsado).ne(JExpr._null()),
								campoUsado.invoke("equals").arg(other.ref(campoUsado)).not()
						)
				)._then()._return(JExpr.lit(false));
			}
		}
		equalsBody._return(JExpr.lit(true));
	}

	public void gerarSerialVersionUID(JDefinedClass klass){
		Map<String, JFieldVar> fields = klass.fields();
		long result = 1;
		for(String key : fields.keySet()){
			JFieldVar field = fields.get(key);
			if( (field.mods().getValue()&JMod.TRANSIENT)!=0)
				result *= field.name().hashCode() + field.type().fullName().hashCode();
		}
		klass.field(
				JMod.PRIVATE|JMod.STATIC|JMod.FINAL,
				jcm.LONG,
				"serialVersionUID",
				JExpr.lit(result)
		);
	}

	public JDefinedClass gerarPropriedades(JDefinedClass classeBean, Collection<Propriedade> propriedades){
		for(Propriedade propriedade: propriedades){
			gerarPropriedade(classeBean, propriedade);
		}
		return classeBean;
	}

	public void gerarPropriedade(JDefinedClass classeBean, Propriedade propriedade) {
		JType tipo = getTipo(propriedade.getType());
		// campos privados
		classeBean.field(JMod.PRIVATE, tipo, propriedade.getNome());
		// getters e setters
		// mas se for o id, gerar apenas o getter
		if(propriedade.isSet())
			gerarSetter(classeBean, propriedade.getNome());
		if(propriedade.isGet())
			gerarGetter(classeBean, propriedade.getNome());
	}

	public JDefinedClass gerarClasse(String fullyqualifiedName)
			throws JClassAlreadyExistsException {
		JDefinedClass classeBean = jcm._class(
				JMod.PUBLIC,
				fullyqualifiedName,
				ClassType.CLASS
		);
		classeBean._implements(Serializable.class);
		classeBean._implements(Cloneable.class);
		return classeBean;
	}

	public void gerarConstantes(JDefinedClass klass, JavaBeanSchema javaBeanSchema) {
		klass.field(
				JMod.PUBLIC|JMod.STATIC|JMod.FINAL,
				jcm.ref(String.class),
				javaBeanSchema.getConstanteDaTabela(),
				JExpr.lit(javaBeanSchema.getTabela().getNome())
			);
		for(String coluna : ColunasUtils.colunasOrdenadasDoJavaBeanSchema(javaBeanSchema)){
			if(coluna==null)
				continue;
			klass.field(
				JMod.PUBLIC|JMod.STATIC|JMod.FINAL,
				jcm.ref(String.class),
				javaBeanSchema.getConstante(coluna),
				JExpr.lit(coluna)
			);
		}
	}


	public void gerarMetodoClone(JDefinedClass klass, JavaBeanSchema javaBeanSchema){
		JMethod clone = klass.method(JMod.PUBLIC, klass, "clone");
		JBlock corpo = clone.body();
		JVar newClone = corpo.decl(klass, "obj");
		JTryBlock tryCast = corpo._try();
		tryCast.body().assign(
				newClone,
				JExpr.cast(klass, JExpr._super().invoke("clone"))
		);
		tryCast
			._catch(jcm.ref(CloneNotSupportedException.class))
			.body()
			._return(JExpr._null());
		for(String coluna : ColunasUtils.colunasOrdenadasDoJavaBeanSchema(javaBeanSchema)){
			Propriedade propriedade = javaBeanSchema.getPropriedade(coluna);
			JFieldVar campo = klass.fields().get(propriedade.getNome());
			// se for necessario "zerar"o id
			//if(propriedade.equals(javaBeanSchema.getPrimaryKey()))
			//	corpo.assign(
			//			newClone.ref(campo),
			//			JExpr.direct(CodeModelDaoFactory.ID_PADRAO)
			//	);

			// para usar o clone binario da classe object, nada eh feito
			if(campo.type().isPrimitive()||campo.type().equals(getTipo(String.class))) {
				continue;
			}

			else if(propriedade.getType().equals(Date.class)){
				corpo.assign(
						newClone.ref(campo),
						JExpr._new(jcm.ref(Date.class)).arg(campo.invoke("getTime"))
				);
			}

			else
				corpo.assign(newClone.ref(campo), campo);
		}
		for(String key : klass.fields().keySet()){
			JFieldVar campo = klass.fields().get(key);
			if( (campo.mods().getValue()&JMod.TRANSIENT)!=0 ){
				corpo.assign(newClone.ref(campo), JExpr._null());
			}
		}

		corpo._return(newClone);
	}

//	public void gerarAssociacaoToOne(JDefinedClass klass, JDefinedClass estrangeira, String idEstrangeira){
//		// private IdClass idEstrangeira;
//		// JFieldVar idCampo = klass.fields().get(idEstrangeira);
//		// private ClasseEstrangeira estrangeira;
//		JFieldVar campo = klass.field(
//				JMod.PRIVATE,
//				estrangeira,
//				estrangeira.name(),
//				JExpr._null()
//		);
//		// getters e setters
//		gerarGetterAndSetter(klass, campo.name());
//	}
//
//	public void gerarAssociacaoToMany(JDefinedClass klass, JDefinedClass estrangeira, String idEstrangeira){
//		// private ClasseEstrangeira estrangeira;
//		JFieldVar campo = klass.field(
//				JMod.PRIVATE,
//				jcm.ref(Collection.class).narrow(estrangeira),
//				// TODO pluralizar
//				estrangeira.name(),
//				JExpr._null()
//		);
//		// getters e setters
//		gerarGetterAndSetter(klass, campo.name());
//	}

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
