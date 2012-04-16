package br.com.cds.mobile.geradores.dao;

import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import br.com.cds.mobile.framework.config.DB;
import br.com.cds.mobile.framework.utils.SQLiteUtils;
import br.com.cds.mobile.gerador.query.QuerySet;
import br.com.cds.mobile.geradores.filters.associacao.Associacao;
import br.com.cds.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import br.com.cds.mobile.geradores.javabean.JavaBeanSchema;
import br.com.cds.mobile.geradores.javabean.Propriedade;
import br.com.cds.mobile.geradores.util.CamelCaseUtils;
import br.com.cds.mobile.geradores.util.ColunasUtils;
import br.com.cds.mobile.geradores.util.PluralizacaoUtils;
import br.com.cds.mobile.geradores.util.SQLiteGeradorUtils;

import com.sun.codemodel.JArray;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTypeVar;
import com.sun.codemodel.JVar;

public class CodeModelDaoFactory {

	// private static final String PACOTE = "br.com.cds.mobile.flora.db";
	// o valor 0 nao pode ser uma PK no sqlite
	public static final String ID_PADRAO = "0";
	private JCodeModel jcm;

	public CodeModelDaoFactory(JCodeModel jcm){
		this.jcm = jcm;
	}

	public void gerarAcessoDB(JDefinedClass klass, JavaBeanSchema javaBeanSchema){
		// inicializando a PK para o valor padrao (NULL, 0, etc...)
		String pkNome = javaBeanSchema.getPrimaryKey().getNome();
		JFieldVar pk = klass.fields().get(pkNome);
		pk.init(JExpr.direct(ID_PADRAO));
		// remover o setter da PK
		String pkSetter = "set"+Character.toUpperCase(pkNome.charAt(0)) + pkNome.substring(1);
		JMethod pkmetodo = null;
		for(JMethod metodo : klass.methods())
			if(metodo.name().equals(pkSetter))
				pkmetodo = metodo;
		if(pkmetodo!=null)
			klass.methods().remove(pkmetodo);
		gerarMetodoSave(klass, javaBeanSchema);
		gerarMetodoDelete(klass, javaBeanSchema);
		gerarMetodoObjects(klass, javaBeanSchema);
	}

	public void gerarMetodoSave(JDefinedClass klass, JavaBeanSchema javaBeanSchema){
		/************************************************************
		 * public boolean save(){                                   *
		 *   ContentValues cv = new ContentValues();                *
		 *   cv.put(ClasseBean.CAMPO,this.campo);                   *
		 *   cv.put(*);                                             *
		 *   if(id==-1)                                             *
		 *     db.insertOrThrow(getTabela(), null, contentValues);  *
		 *   else                                                   *
		 *     db.update(getTabela(), contentValues,                *
		 *         getColunaId() + "=?", new String[] { "" + id }); *
		 *   return true;                                           *
		 * }                                                        *
		 ***********************************************************/
		JMethod save = klass.method(JMod.PUBLIC, jcm.BOOLEAN, "save");
		JBlock corpo = save.body();
		JVar contentValues = corpo.decl(
				jcm.ref(ContentValues.class),
				"contentValues",
				JExpr._new(jcm.ref(ContentValues.class))
		);
		for(String coluna : ColunasUtils.colunasOrdenadasDoJavaBeanSchema(javaBeanSchema)){
			Propriedade propriedade = javaBeanSchema.getPropriedade(coluna);
			JFieldVar campo = klass.fields().get(propriedade.getNome());
			JExpression argumentoValor =
					// if campo instanceof Date
					(campo.type().name().equals("Date")) ?
							jcm.ref(SQLiteUtils.class).staticInvoke("dateToString").arg(campo) :
					// if campo instanceof Boolean
					(campo.type().name().equals("boolean") || campo.name().equals("Boolean") ) ?
							JExpr.direct(campo.name()+"? 1 : 0"):
					// else
							campo;
			JFieldVar constante = klass.fields().get(javaBeanSchema.getConstante(coluna));
			if(constante==null)
				throw new RuntimeException(String.format(
						"%s nao encontrada em %s.",
						javaBeanSchema.getConstante(coluna),
						klass.name()
				));
			else
				corpo.add(
				contentValues.invoke("put")
					.arg(constante)
					.arg(argumentoValor)
				);
		}
		JClass dbclass = jcm.ref(DB.class);
		JVar db = corpo.decl(jcm.ref(SQLiteDatabase.class),"db",dbclass.staticInvoke("getWritableDatabase"));
		// if(id=-1)
		JFieldVar id = klass.fields().get( javaBeanSchema.getPrimaryKey().getNome() );
		JConditional ifIdNull = corpo._if(
				id.eq(JExpr.direct(ID_PADRAO)));
		// db.insertOrThrow(getTabela(), null, contentValues);
		ifIdNull._then().invoke(db, "insertOrThrow")
			.arg(klass.fields().get(javaBeanSchema.getConstanteDaTabela()))
			.arg(JExpr._null())
			.arg(contentValues);
		// else
		// db.update(getTabela(), objetoToContentValue(objeto), getColunaId() + "=?", new String[] { "" + id });
		ifIdNull._else().invoke(db,"update")
			.arg(klass.fields().get(javaBeanSchema.getConstanteDaTabela()))
			.arg(contentValues)
			.arg(
					klass.fields().get(javaBeanSchema.getConstante((javaBeanSchema.getTabela().getPrimaryKey().getNome()))).plus(JExpr.lit("=?")))
			.arg(JExpr.newArray(jcm.ref(String.class)).add(JExpr.lit("").plus(klass.fields().get(javaBeanSchema.getPrimaryKey().getNome()))));

		corpo._return(JExpr.lit(true));
	}

	public void gerarMetodoDelete(JDefinedClass klass, JavaBeanSchema javaBeanSchema){
		/************************************************************
		 * public boolean save(){                                   *
		 *   ContentValues cv = new ContentValues();                *
		 *   cv.put(ClasseBean.CAMPO,this.campo);                   *
		 *   cv.put(*);                                             *
		 *   if(id==-1)                                             *
		 *     db.insertOrThrow(getTabela(), null, contentValues);  *
		 *   else                                                   *
		 *     db.update(getTabela(), objetoToContentValue(objeto), *
		 *         getColunaId() + "=?", new String[] { "" + id }); *
		 *   return true;                                           *
		 * }                                                        *
		 ***********************************************************/
		JMethod delete = klass.method(JMod.PUBLIC, jcm.BOOLEAN, "delete");
		JBlock corpo = delete.body();
		JClass dbclass = jcm.ref(DB.class);
		JVar db = corpo.decl(jcm.ref(SQLiteDatabase.class),"db",dbclass.staticInvoke("getWritableDatabase"));
		// if(id!=-1)
		JConditional ifIdNotNull = corpo._if(klass.fields().get(javaBeanSchema.getPrimaryKey().getNome())
				.ne(JExpr.direct(ID_PADRAO)));
		// getDb().getWritableDatabase().delete(TABELA, ID + "=?", new String[] { "" + id });

		// if(id!=null)
		JBlock blocoThen = ifIdNotNull._then();
		blocoThen.invoke(db, "delete")
			.arg(klass.fields().get(javaBeanSchema.getConstanteDaTabela()))
			// ID+"=?"
			.arg(klass.fields()
				.get(
					javaBeanSchema.getConstante(
							javaBeanSchema.getTabela().getPrimaryKey().getNome()
					)
				).plus(JExpr.lit("=?"))
			)
			// new String[]{""+id}
			.arg(
				JExpr.newArray(jcm.ref(String.class)).add(
					JExpr.lit("")
					.plus(klass.fields().get(javaBeanSchema.getPrimaryKey().getNome()))
				)
			);

		blocoThen._return(JExpr.lit(true));

		// se id == null
		corpo._return(JExpr.lit(false));
	}

	public void gerarMetodoObjects(JDefinedClass klass, JavaBeanSchema javaBeanSchema){
		JClass queryset = jcm.ref(QuerySet.class).narrow(klass);
		JDefinedClass qsInner = gerarQuerySet(klass,javaBeanSchema,queryset);
		JMethod metodoObjects = klass.method(JMod.PUBLIC|JMod.STATIC,queryset, "objects");
		metodoObjects.body()._return(JExpr._new(qsInner.narrow(klass)).arg(JExpr._new(klass)));
	}

	private JDefinedClass gerarQuerySet(
			JDefinedClass klass,JavaBeanSchema javaBeanSchema, JClass queryset
	){
		try {
			JDefinedClass querySetInner = klass._class(JMod.PUBLIC|JMod.STATIC, "QuerySet");
			querySetInner._extends(queryset);
			gerarQuerySetInnerInternals(klass, javaBeanSchema, querySetInner);
			return querySetInner;
		} catch (JClassAlreadyExistsException e) {
			throw new RuntimeException(e);
		}
	}

	private void gerarQuerySetInnerInternals(
			JDefinedClass klass,
			JavaBeanSchema javaBeanSchema,
			JDefinedClass querySetInner
	){

		List<String> colunasEmOrdem = ColunasUtils.colunasOrdenadasDoJavaBeanSchema(javaBeanSchema);

		JTypeVar generic = querySetInner.generify("T", klass);

		/**
		 * prototipo
		 */
		JFieldVar prototipo = querySetInner.field(JMod.PRIVATE, generic, "prototipo");

		/**
		 * Construtor
		 */
		JMethod construtor = querySetInner.constructor(JMod.PROTECTED);
		JVar prototipoparam = construtor.param(generic, "prototipo");
		construtor.body().assign(JExpr.refthis(prototipo.name()), prototipoparam);

		/*****************************
		 * Override
		 * protected String getTabela(){
		 *   return Classe.TABELA;
		 * }
		 ******************************/
		JMethod getTabela = querySetInner.method(
				JMod.PROTECTED, jcm.ref(String.class), "getTabela"
		);
		getTabela.annotate(java.lang.Override.class);
		getTabela.body()._return(klass.fields().get(javaBeanSchema.getConstanteDaTabela()));

		/**
		 * Override
		 * protected String[] getColunas(){
		 *   return new String[]{ ID, NOME, ENDERECO, datetime(DATA_VISITA) };
		 * }
		 */
		JMethod getColunas = querySetInner.method(
				JMod.PROTECTED, jcm.ref(String[].class), "getColunas"
		);
		getColunas.annotate(java.lang.Override.class);
		JArray colunasArray = JExpr.newArray(jcm.ref(String.class));
		for(String coluna : colunasEmOrdem){
			Propriedade propriedade = javaBeanSchema.getPropriedade(coluna);
			JExpression elementoArray = klass.fields().get(
					javaBeanSchema.getConstante(coluna)
			);
			if(propriedade.getType().equals(Date.class))
				elementoArray = JExpr.lit(SQLiteGeradorUtils.FUNCAO_DATE+"(" ).plus(elementoArray).plus(JExpr.lit(")"));
			colunasArray.add(elementoArray);
		}
		getColunas.body()._return(colunasArray);

		/**
		 * Override
		 * protected Classe cursorToObject(Cursor cursor){
		 *   Classe bean = new Classe();
		 *   bean.id = cursor.getLong(0);
		 *   bean.nome = cursor.
		 * }
		 */
		JMethod cursorToObject = querySetInner.method(
				JMod.PROTECTED, generic, "cursorToObject");
		cursorToObject.annotate(java.lang.Override.class);
		JVar cursor = cursorToObject.param(android.database.Cursor.class, "cursor");
		JBlock corpo = cursorToObject.body();
		JVar bean = corpo.decl(generic, "objeto", JExpr.cast(generic,prototipo.invoke("clone")));
		bean.annotate(java.lang.SuppressWarnings.class).param("value","unchecked");
		for(String coluna : colunasEmOrdem){
			Propriedade propriedade = javaBeanSchema.getPropriedade(coluna);

			JInvocation valor = cursor.invoke(
					SQLiteGeradorUtils
					.metodoGetDoCursorParaClasse(propriedade.getType())
			).arg(JExpr.lit(colunasEmOrdem.indexOf(coluna)));
			if(propriedade.getType().equals(Boolean.class))
				valor = jcm.ref(SQLiteUtils.class)
					.staticInvoke("integerToBoolean").arg(valor);
			else if(propriedade.getType().equals(Date.class))
				valor = jcm.ref(SQLiteUtils.class)
					.staticInvoke("stringToDate").arg(valor);

			corpo.assign(
					// bean.campo = 
					bean.ref(klass.fields().get(propriedade.getNome())),
					// cursor.get****(indice)
					valor
			);
		}
		corpo._return(bean);

	}

	public void gerarRelacoes(
			JDefinedClass klassA, JavaBeanSchema javaBeanSchemaA,
			JDefinedClass klassB, JavaBeanSchema javaBeanSchemaB
	){

		for(Associacao associacao : javaBeanSchemaA.getAssociacoes()){
			if(associacao instanceof AssociacaoOneToMany){
				AssociacaoOneToMany oneToMany = (AssociacaoOneToMany)associacao;
				if(
						associacao.getTabelaB().equals(javaBeanSchemaA.getTabela()) &&
						associacao.getTabelaA().equals(javaBeanSchemaB.getTabela())
				){
					JFieldVar campo = klassA.field(
							JMod.PRIVATE|JMod.TRANSIENT,
							klassB,
							CamelCaseUtils.tolowerCamelCase(klassB.name())
					);
					JMethod getKlassB = klassA.method(
							JMod.PUBLIC,
							klassB,
							"get"+javaBeanSchemaB.getNome()
					);
					JBlock corpo = getKlassB.body();
					JConditional ifCampoNull = corpo._if(
							campo.eq(
									JExpr._null()
							).cand(
									klassA.fields()
									.get(javaBeanSchemaA.getPropriedade(oneToMany.getKeyToA()).getNome())
									.ne(JExpr.direct(ID_PADRAO))
							)
					);
					ifCampoNull._then().assign(campo,
							klassB.staticInvoke("objects")
							.invoke("filter").arg(klassB.staticRef(klassB.fields().get(
									javaBeanSchemaB.getConstante(oneToMany.getReferenciaA())
							)).plus(JExpr.lit("=?"))).arg(
									klassB.fields().get(
											javaBeanSchemaB.getPropriedade(oneToMany.getReferenciaA()).getNome()
									)
							)
							.invoke("first")
					);
					corpo._return(campo);
				} else if(
						associacao.getTabelaA().equals(javaBeanSchemaA.getTabela()) &&
						associacao.getTabelaB().equals(javaBeanSchemaB.getTabela())
				){
					String nomePlural = PluralizacaoUtils.pluralizar(javaBeanSchemaB.getNome());
					JFieldVar campo = klassA.field(
							JMod.PRIVATE|JMod.TRANSIENT,
							klassB,
							CamelCaseUtils.tolowerCamelCase(nomePlural)
					);
					JMethod getKlassB = klassA.method(
							JMod.PUBLIC,
							klassB,
							"get"+ nomePlural
					);
					JBlock corpo = getKlassB.body();
					JConditional ifCampoNull = corpo._if(campo.eq(JExpr._null()));
					ifCampoNull._then()._return(
							klassB.staticInvoke("objects")
							.invoke("filter").arg(klassB.staticRef(klassB.fields().get(
									javaBeanSchemaB.getConstante(oneToMany.getKeyToA())
							)).plus(JExpr.lit("=?"))).arg(
									klassB.fields().get(
											javaBeanSchemaA.getPropriedade(oneToMany.getReferenciaA()).getNome()
									)
							)
							.invoke("first")
					);
					corpo._return(campo);
				}

			}
		}
	}



}
