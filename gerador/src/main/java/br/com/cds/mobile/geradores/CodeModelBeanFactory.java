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

	private static final String ERROR_CLONE_NOT_DEEP_COPY_WARNING = "Metodo clone parece nao fazer deep copy de %s da classe %s";
	private static final String ERRO_MSG_ARGUMENTO_NULL = "argumento null passado para o metodo %s::%s";
	private JCodeModel jcm;

	public CodeModelBeanFactory(JCodeModel jcm){
		this.jcm = jcm;
	}

	public void gerarSetter(JDefinedClass klass, String nomeCampo) {
		JFieldVar campo = klass.fields().get(nomeCampo);
		// nomeDoCampo -> NomeDoCampo
		String nomeCampoCaptalizado = ""+Character.toUpperCase(campo.name().charAt(0))+campo.name().substring(1);
		/* ************************************
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
		/* ***************************
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

	/**
	 * <p>Sobrescreve os metodos hashCode e equals.</p>
	 * <p>Sempre que dois objetos forem iguais (objA.equals(objB)==true),
	 *    seus hascodes obriatoriamente sao iguais</p>
	 * <p>Deve-se notar que o metodo equals eh sempre mais restritivo</p>
	 * @param klass
	 * @param javaBeanSchema
	 */
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

		gerarMetodoHashCode(klass, camposUsados);
		gerarMetodoEquals(klass, camposUsados);
	}

	/**
	 * Gera metodo hashcode
	 * @param klass
	 * @param camposUsados
	 */
	private void gerarMetodoHashCode(JDefinedClass klass,
			Collection<JFieldVar> camposUsados) {
		String nome = "hashCode";
		int valInicial = 1;
		/* ****************************************
		 *                hashCode                *
		 *                                        *
		 * Override                               *
		 * public int hashCode(){                 *
		 *     int value = 1;                     *
		 *     value += ( campoBoolean ? 1 : 0 ); *
		 *     value += (int) campoNumeric;       *
		 *     value *= campoObject.hashCode();   *
		 *     return value;                      *
		 * }                                      *
		 *****************************************/
		JMethod hashcode = klass.method(JMod.PUBLIC, jcm.INT, nome);
		hashcode.annotate(Override.class);
		JBlock hashcodeBody = hashcode.body();
		JVar hashAccumulator = hashcodeBody.decl(jcm.INT, "value",JExpr.lit(valInicial));

		/* ************************************
		 *            Campo booleano          *
		 *                                    *
		 * value += ( campoBoolean ? 1 : 0 ); *
		 *************************************/
		for(JFieldVar campoUsado : camposUsados){
			if(campoUsado.type().equals(jcm.BOOLEAN)){
				hashcodeBody.assign(
						hashAccumulator,
						hashAccumulator.plus(JOp.cond(
								campoUsado,
								JExpr.lit(1),
								JExpr.lit(0)
						))
				);
			}

			/* ************************************
			 *  Outros primitivos sao numericos   *
			 *                                    *
			 * value += (int) campoBoolean;       *
			 *************************************/
			else if(campoUsado.type().isPrimitive()){
				hashcodeBody.assign(
						hashAccumulator,
						hashAccumulator.plus(JExpr.cast(jcm.INT, campoUsado))
				);
			}

			/* ************************************
			 *               objects              *
			 *                                    *
			 * value += campoObject.hashCode();   *
			 *************************************/
			else {
				hashcodeBody.assign(
						hashAccumulator,
						hashAccumulator.mul(campoUsado.invoke(nome))
				);
			}
		}

		// retorna o valor no "acumulador"
		hashcodeBody._return(hashAccumulator);
	}

	/**
	 * Sobrescreve metodo equals
	 * @param klass
	 * @param camposUsados
	 */
	private void gerarMetodoEquals(JDefinedClass klass,
			Collection<JFieldVar> camposUsados) {

		/* ***************************************************
		 *                     equals                        *
		 *                                                   *
		 * Overide                                           *
		 * public boolean equals(Object obj){                *
		 *    if (obj == null) {                             *
		 *         return false;                             *
		 *    }                                              *
		 *    if (!(obj instanceof klass)) {                 *
		 *        return false;                              *
		 *    }                                              *
		 *    Klass other = ((Klass) obj);                   *
		 *    if (other.campoPrimitivo!= campoPrimitivo) {   *
		 *        return false;                              *
		 *    }                                              *
		 *    if (                                           *
		 *        (campoObjeto == null) ?                    *
		 *            other.campoObjeto!= null :             *
		 *            !campoObjeto.equals(other.campoObjeto) *
		 *     ) {                                           *
		 *        return false;                              *
		 *    }                                              *
		 *    return true;                                   *
		 * }                                                 *
		 ****************************************************/
		JMethod equals = klass.method(JMod.PUBLIC, jcm.BOOLEAN, "equals");
		JVar other = equals.param(Object.class, "obj");
		equals.annotate(Override.class);
		JBlock equalsBody = equals.body();

		/* ************************
		 *    if (obj == null) {  *
		 *         return false;  *
		 *    }                   *
		 *************************/
		equalsBody._if(
				other.eq(JExpr._null())
		)._then()._return(JExpr.lit(false));

		/* ***********************************
		 *    if (!(obj instanceof klass)) { *
		 *        return false;              *
		 *    }                              *
		 *    Klass other = ((Klass) obj);   *
		 ************************************/
		equalsBody._if(
				other._instanceof(klass).not()
		)._then()._return(JExpr.lit(false));
		other = equalsBody.decl(klass, "other",JExpr.cast(klass, other));

		
		for(JFieldVar campoUsado : camposUsados){
			/* ***************************************************
			 *    if (other.campoPrimitivo!= campoPrimitivo) {   *
			 *        return false;                              *
			 *    }                                              *
			 ****************************************************/
			if(campoUsado.type().isPrimitive()){
				equalsBody._if(
						other.ref(campoUsado).ne(campoUsado)
				)._then()._return(JExpr.lit(false));
			}

			/* **********************************************************
			 *    if (                                                  *
			 *        (campoObjeto == null) ?                           *
			 *            other.campoObjeto!= null :                    *
			 *            !campoObjeto.equals(other.campoObjeto)        *
			 *     ) {                                                  *
			 *        return false;                                     *
			 *    }                                                     *
			 ***********************************************************/
			else {
				equalsBody._if(
						JOp.cond(
								campoUsado.eq(JExpr._null()),
								other.ref(campoUsado).ne(JExpr._null()),
								campoUsado.invoke("equals").arg(other.ref(campoUsado)).not()
						)
				)._then()._return(JExpr.lit(false));
			}
		}

		// se nada retornou false, retorna true
		equalsBody._return(JExpr.lit(true));
	}

	/**
	 * gera serialVersionUID para a classe, baseado nos campos
	 * que nao sao transient, static ou final
	 * @param klass
	 */
	public void gerarSerialVersionUID(JDefinedClass klass){
		Map<String, JFieldVar> fields = klass.fields();
		long result = 1;
		for(String key : fields.keySet()){
			JFieldVar field = fields.get(key);
			if(
					((field.mods().getValue()&JMod.TRANSIENT)!=0) &&
					((field.mods().getValue()&JMod.STATIC)!=0) &&
					((field.mods().getValue()&JMod.FINAL)!=0)
			){
				result = result*field.name().hashCode() + field.type().fullName().hashCode();
			}
		}
		klass.field(
				JMod.PRIVATE|JMod.STATIC|JMod.FINAL,
				jcm.LONG,
				"serialVersionUID",
				JExpr.lit(result)
		);
	}

	/**
	 * Gera propriedades, campos com getter e/ou setter
	 * @param classeBean
	 * @param propriedades
	 * @return classeBean com propriedades econtradas
	 */
	public JDefinedClass gerarPropriedades(JDefinedClass classeBean, Collection<Propriedade> propriedades){
		for(Propriedade propriedade: propriedades){
			gerarPropriedade(classeBean, propriedade);
		}
		return classeBean;
	}

	/**
	 * Gera uma propriedade, campo com getter e/ou setter
	 * @param classeBean
	 * @param propriedade
	 */
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

	/**
	 * Gera uma JDefinedClass a partir de seu nome completo
	 * @param fullyqualifiedName
	 * @return classe gerada
	 * @throws JClassAlreadyExistsException
	 */
	public JDefinedClass gerarClasse(String fullyqualifiedName)
			throws JClassAlreadyExistsException {
		/* *********************************************************
		 * package nome.completo.do.pacote;                        *
		 *                                                         *
		 * public class Klass implements Serializable, Cloneable { *
		 *                                                         *
		 * }                                                       *
		 **********************************************************/
		JDefinedClass classeBean = jcm._class(
				JMod.PUBLIC,
				fullyqualifiedName,
				ClassType.CLASS
		);
		classeBean._implements(Serializable.class);
		classeBean._implements(Cloneable.class);
		return classeBean;
	}

	/**
	 * Gera as constantes com nomes das colunas e da tabela
	 * @param klass
	 * @param javaBeanSchema
	 */
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

	/**
	 * Sobrescreve metodo clone, da interface Cloneable
	 * @param klass
	 * @param javaBeanSchema
	 */
	public void gerarMetodoClone(JDefinedClass klass, JavaBeanSchema javaBeanSchema){
		/* **********************************************
		 * public Klass clone() {                       *
		 *     klass obj;                               *
		 *     try {                                    *
		 *         obj = ((Klass) super.clone());       *
		 *     } catch (CloneNotSupportedException e) { *
		 *         return null;                         *
		 *     }                                        *
		 *     obj.data = new Date(data);               *
		 *     obj.campoAssociado = null;               *
		 *     return obj;                              *
		 * }                                            *
		 ***********************************************/
		JMethod clone = klass.method(JMod.PUBLIC, klass, "clone");
		JBlock corpo = clone.body();

		/* **********************************************
		 *     klass obj;                               *
		 *     try {                                    *
		 *         obj = ((Klass) super.clone());       *
		 *     } catch (CloneNotSupportedException e) { *
		 *         return null;                         *
		 *     }                                        *
		 ***********************************************/
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

			// usar o valor obtido com o clone binario da interface cloneable
			if(campo.type().isPrimitive()||campo.type().equals(getTipo(String.class))) {
				continue;
			}

			/* **********************************************
			 * //reescreve a datas, criando novos objetos   *
			 *     obj.data = new Date(data);               *
			 ***********************************************/
			else if(propriedade.getType().equals(Date.class)){
				corpo.assign(
						newClone.ref(campo),
						JExpr._new(jcm.ref(Date.class)).arg(campo.invoke("getTime"))
				);
			}

			// Se chegar aqui, deu erro
			else{
				System.err.printf(
						ERROR_CLONE_NOT_DEEP_COPY_WARNING,
						campo.name(),
						klass.name()
				);
			};
		}

		/* **********************************************
		 * //estes campos devem ser buscados novamente  *
		 * // no banco                                  *
		 *     obj.campoAssociado = null;               *
		 ***********************************************/
		for(String key : klass.fields().keySet()){
			JFieldVar campo = klass.fields().get(key);
			if( (campo.mods().getValue()&JMod.TRANSIENT)!=0 ){
				corpo.assign(newClone.ref(campo), JExpr._null());
			}
		}

		// retorna o novo clone
		corpo._return(newClone);
	}


	/**
	 * Retorna o tipo do CodeModel para a classe
	 * @param klass
	 * @return
	 */
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
