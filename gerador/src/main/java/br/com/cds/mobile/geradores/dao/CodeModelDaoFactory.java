package br.com.cds.mobile.geradores.dao;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import br.com.cds.mobile.framework.query.QuerySet;
import br.com.cds.mobile.framework.utils.CamelCaseUtils;
import br.com.cds.mobile.framework.utils.DateUtil;
import br.com.cds.mobile.framework.utils.SQLiteUtils;
import br.com.cds.mobile.geradores.filters.associacao.Associacao;
import br.com.cds.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import br.com.cds.mobile.geradores.javabean.JavaBeanSchema;
import br.com.cds.mobile.geradores.javabean.Propriedade;
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
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JTypeVar;
import com.sun.codemodel.JVar;

public class CodeModelDaoFactory {

	private static final String REFERENCIA_NAO_ENCONTRADA =
			"Referencia em associacao to many nao encontrada";
	private static final String CONSTANTE_NAO_ENCONTRADA_FORMAT =
			"%s nao encontrada em %s.";

	// o valor 0 nao pode ser uma PK no sqlite
	public static final long ID_PADRAO = 0;

	private JCodeModel jcm;
	private String dbClass;
	private String getDbStaticMethod;

	public CodeModelDaoFactory(JCodeModel jcm, String dbClass,
			String getDbStaticMethod) {
		super();
		this.jcm = jcm;
		this.dbClass = dbClass;
		this.getDbStaticMethod = getDbStaticMethod;
	}

	/**
	 * <p>Gera os metodos de insert/update/delete, no estilo "active record"</p>
	 * <p>Cria tambem metodo de busca, retornando um QuerySet</p>
	 * @param klass
	 * @param javaBeanSchema
	 */
	public void gerarAcessoDB(JDefinedClass klass, JavaBeanSchema javaBeanSchema){

		// inicializando a PK para o valor padrao (NULL, 0, etc...)
		Propriedade primaryKey = javaBeanSchema.getPrimaryKey();
		if(primaryKey!=null){
			JFieldVar pk = klass.fields().get(primaryKey.getNome());
			pk.init(JExpr.lit(ID_PADRAO));
			String pkNome = primaryKey.getNome();
			// remover o setter da PK
			String pkSetter = "set"+Character.toUpperCase(pkNome.charAt(0)) + pkNome.substring(1);
			JMethod pkmetodo = null;
			for(JMethod metodo : klass.methods())
				if(metodo.name().equals(pkSetter))
					pkmetodo = metodo;
			if(pkmetodo!=null)
				klass.methods().remove(pkmetodo);
		}
		else{
			generateConstrutorForCompundPrimaryKey(klass, javaBeanSchema);
		}
		gerarMetodoSave(klass, javaBeanSchema);
		gerarMetodoDelete(klass, javaBeanSchema);
		gerarMetodoObjects(klass, javaBeanSchema);
	}

	public void generateConstrutorForCompundPrimaryKey(
			JDefinedClass klass, JavaBeanSchema javaBeanSchema
	){
		JMethod constructor = klass.constructor(JMod.PUBLIC);

		JBlock corpo = constructor.body();

		//Collection<Associacao> associacoes = javaBeanSchema.getAssociacoes();
		Collection<String> primaryKeys = javaBeanSchema.getPrimaryKeyColumns();
		for(String colunm : primaryKeys){
			Propriedade prop = javaBeanSchema.getPropriedade(colunm);
			JVar param = constructor.param(prop.getType(), prop.getNome());
			JFieldVar campo = klass.fields().get(prop.getNome());
			corpo.assign(JExpr.refthis(campo.name()),param);
			JFieldVar constante = klass.fields().get(
					javaBeanSchema.getConstante(colunm)
			);
		}
		klass.constructor(JMod.PRIVATE);
	}

	/**
	 * Gera metodo "save", para insert/update
	 * @param klass
	 * @param javaBeanSchema
	 */
	public void gerarMetodoSave(JDefinedClass klass, JavaBeanSchema javaBeanSchema){
		/* **********************************************************
		 * public boolean save() throws SQLException {              *
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
		save._throws(SQLException.class);
		JBlock corpo = save.body();

		/* ********************************************
		 *   ContentValues cv = new ContentValues();  *
		 *********************************************/
		JVar contentValues = corpo.decl(
				jcm.ref(ContentValues.class),
				"contentValues",
				JExpr._new(jcm.ref(ContentValues.class))
		);

		Propriedade primaryKey = javaBeanSchema.getPrimaryKey();

		for(String coluna : ColunasUtils.colunasOrdenadasDoJavaBeanSchema(javaBeanSchema)){
			Propriedade propriedade = javaBeanSchema.getPropriedade(coluna);
			if(
					primaryKey!=null &&
					propriedade.getNome().equals(primaryKey.getNome())
			){
				continue;
			}
			JFieldVar campo = klass.fields().get(propriedade.getNome());

			JExpression argumentoValor =
					// if campo instanceof Date
					(campo.type().name().equals("Date")) ?
							// value = DateUtil.timestampToString(date)
							jcm.ref(DateUtil.class).staticInvoke("timestampToString").arg(campo) :
					// if campo instanceof Boolean
					(campo.type().name().equals("boolean") || campo.name().equals("Boolean") ) ?
							// value = (campoBool? 1 : 0)
							JOp.cond(campo,JExpr.lit(1),JExpr.lit(0)):
					// else
							// value = campo
							campo;
			/*
			 * Constante da coluna
			 */
			JFieldVar constante = klass.fields().get(javaBeanSchema.getConstante(coluna));
			if(constante==null)
				throw new RuntimeException(String.format(
						CONSTANTE_NAO_ENCONTRADA_FORMAT,
						javaBeanSchema.getConstante(coluna),
						klass.name()
				));
			else {
				/* *******************************************
				 *   cv.put(Klass.CAMPO, <expressao value>); *
				 ********************************************/
				corpo.add(
					contentValues.invoke("put")
						.arg(constante)
						.arg(argumentoValor)
				);
			}
		}
		JVar db = corpo.decl(jcm.ref(SQLiteDatabase.class),"db",getDbExpr());

		if(primaryKey==null){
			gerarMetodoSaveComPrimaryKeyMultipla(
					klass, javaBeanSchema, corpo, db, contentValues
			);
			return;
		}
		// if(id=ID_PADRAO)   => nova entidade  => insert
		JFieldVar pkVar = klass.fields().get(primaryKey.getNome());
		JFieldVar id = pkVar;
		JConditional ifIdNull = corpo._if(
				id.eq(JExpr.lit(ID_PADRAO)));
		/* **************************************
		 * if(id==ID_PADRAO){                   *
		 *      int value = db.insertOrThrow(   *
		 *          getTabela(),                *
		 *          null,                       *
		 *          contentValues               *
		 *      );                              *
		 *      if( value > 0 ){                *
		 *          id = value;                 *
		 *          return true;                *
		 *      } else {                        *
		 *          return false;               *
		 *      }                               *
		 * }                                    *
		 ***************************************/
		JBlock thenBlock = ifIdNull._then();
		/* **************************************
		 * if(id==ID_PADRAO)                    * 
		 *      int value = db.insertOrThrow(   *
		 *          getTabela(),                *
		 *          null,                       *
		 *          contentValues               *
		 *      );                              *
		 ***************************************/
		JExpression valueExpr = db.invoke("insertOrThrow")
			.arg(klass.fields().get(javaBeanSchema.getConstanteDaTabela()))
			.arg(JExpr._null())
			.arg(contentValues);
		JVar valueId = thenBlock.decl(jcm.LONG, "value",valueExpr);
		/* **************************************
		 *      if( value > 0 ){                *
		 *          id = value;                 *
		 *          return true;                *
		 *      }                               *
		 ***************************************/
		JConditional ifInsertOk = thenBlock._if(valueId.gt(JExpr.lit(0)));
		JBlock ifInsertOkBlock = ifInsertOk._then();
		ifInsertOkBlock.assign(id, valueId);
		ifInsertOkBlock._return(JExpr.lit(true));
		/* **************************************
		 *      } else {                        *
		 *          return false;               *
		 *      }                               *
		 ***************************************/
		ifInsertOk._else()._return(JExpr.lit(false));
		/* ***************************************
		 * else {                                *
		 *     int value = db.update(            *
		 *         getTabela(),                  *
		 *         objetoToContentValue(objeto), *
		 *         getColunaId() + "=?",         *
		 *         new String[] { "" + id }      *
		 *     );                                *
		 *     return (value > 0)                *
		 * }                                     *
		 ****************************************/
		JBlock elseBlock = ifIdNull._else();
//		// TODO refazer com Q
//		JExpression sqlExp = klass.fields().get(
//				javaBeanSchema.getConstante((javaBeanSchema.getPrimaryKey().getNome()))
//		).plus(JExpr.lit("=?"));
//		
//		JArray sqlArgs = JExpr.newArray(jcm.ref(String.class))
//				.add(boxify(pkVar).invoke("toString"));
//		JVar value = elseBlock.decl(jcm.INT, "value", db.invoke("update")
//			.arg(klass.fields().get(javaBeanSchema.getConstanteDaTabela()))
//			.arg(contentValues)
//			.arg(sqlExp)
//			.arg(sqlArgs));
//		elseBlock._return(value.gt(JExpr.lit(0)));
		gerarBlocoUpdate(klass, javaBeanSchema, elseBlock, db, contentValues);
	}

	private void gerarBlocoUpdate(
			JDefinedClass klass,
			JavaBeanSchema javaBeanSchema,
			JBlock block,
			JVar db,
			JVar contentValues
	){
		/* ***********************************
		 *                                   *
		 * int value = db.update(            *
		 *     getTabela(),                  *
		 *     objetoToContentValue(objeto), *
		 *     getColunaId() + "=?",         *
		 *     new String[] { id }           *
		 * );                                *
		 * return (value > 0);               *
		 *                                   *
		 ************************************/
		// TODO refazer com Q
		JVar value = block.decl(jcm.INT, "value", db.invoke("update")
			.arg(klass.fields().get(javaBeanSchema.getConstanteDaTabela()))
			.arg(contentValues)
			.arg(getPrimaryKeysQueryString(klass, javaBeanSchema))
			.arg(getPrimaryKeysArray(klass, javaBeanSchema)));
		block._return(value.gt(JExpr.lit(0)));
	}

	private void gerarMetodoSaveComPrimaryKeyMultipla(
		JDefinedClass klass,
		JavaBeanSchema javaBeanSchema,
		JBlock block,
		JVar db,
		JVar contentValues
	){
		JVar existente =  block.decl(
				klass, "existente",
				klass.staticInvoke("objects")
					.invoke("filter")
						.arg(getPrimaryKeysQueryString(klass, javaBeanSchema))
						.arg(getPrimaryKeysArray(klass, javaBeanSchema))
					.invoke("first")
		);
		JConditional ifExist =  block._if(existente.eq(JExpr._null()));
		JBlock thenBlock = ifExist._then();
		JExpression valueExpr = db.invoke("insertOrThrow")
			.arg(klass.fields().get(javaBeanSchema.getConstanteDaTabela()))
			.arg(JExpr._null())
			.arg(contentValues);
		JVar valueId = thenBlock.decl(jcm.LONG, "value",valueExpr);
		thenBlock._return(valueId.gt(JExpr.lit(0)));

		gerarBlocoUpdate(klass, javaBeanSchema, ifExist._else(), db, contentValues);
	}

	private JExpression getPrimaryKeysQueryString(JDefinedClass klass, JavaBeanSchema javaBeanSchema){
		Iterator<String> primarykeys = javaBeanSchema.getPrimaryKeyColumns().iterator();
		JExpression sqlExp = null;
		while(primarykeys.hasNext()){
			String primarykey = primarykeys.next();
			JExpression expr = klass.fields().get(
					javaBeanSchema.getConstante(primarykey)
				).plus(JExpr.lit("=?"));
			sqlExp = (sqlExp==null) ? expr : sqlExp.plus(JExpr.lit(" AND ")).plus(expr);
		}

		return sqlExp;
	}

	private JExpression getPrimaryKeysArray(JDefinedClass klass, JavaBeanSchema javaBeanSchema){
		Iterator<String> primarykeys = javaBeanSchema.getPrimaryKeyColumns().iterator();
		JArray sqlArgs = JExpr.newArray(jcm.ref(String.class));
		while(primarykeys.hasNext()){
			String primarykey = primarykeys.next();
			sqlArgs = sqlArgs
					.add(boxify(klass.fields().get(
							javaBeanSchema.getPropriedade(primarykey).getNome()
					))
					.invoke("toString"));
		}

		return sqlArgs;

	}

	public void gerarMetodoDelete(JDefinedClass klass, JavaBeanSchema javaBeanSchema){
		/* **********************************************************
		 * public boolean delete(){                                 *
		 *     if(id!=ID_PADRAO) {                                  *
		 *        db.delete(TABELA,ID+"=?",new String[] { id });    *
		 *        return true;                                      *
		 *     }                                                    *
		 *     return false;                                        *
		 * }                                                        *
		 ***********************************************************/
		JMethod delete = klass.method(JMod.PUBLIC, jcm.BOOLEAN, "delete");
		JBlock corpo = delete.body();
		JVar db = corpo.decl(jcm.ref(SQLiteDatabase.class),"db",getDbExpr());
		// if(id!=ID_PADRAO)
		Iterator<String> primarykeys = javaBeanSchema.getPrimaryKeyColumns().iterator();
		JExpression ifexpr = klass.fields().get(
				javaBeanSchema.getPropriedade(primarykeys.next()).getNome()
		).ne(JExpr.lit(ID_PADRAO));
		while(primarykeys.hasNext()){
			ifexpr = ifexpr.cand( 
				klass.fields().get(javaBeanSchema.getPropriedade(primarykeys.next()).getNome())
				.ne(JExpr.lit(ID_PADRAO))
			);
		}
		JConditional ifIdNotNull = corpo._if(ifexpr);
		// getDb().getWritableDatabase().delete(TABELA, ID + "=?", new String[] { "" + id });
		JBlock blocoThen = ifIdNotNull._then();
		blocoThen.invoke(db, "delete")
			.arg(klass.fields().get(javaBeanSchema.getConstanteDaTabela()))
			// ID+"=?"
			.arg(getPrimaryKeysQueryString(klass, javaBeanSchema))
			// new String[]{id}
			.arg(getPrimaryKeysArray(klass, javaBeanSchema));

		blocoThen._return(JExpr.lit(true));

		// se id == null
		corpo._return(JExpr.lit(false));
	}

	public void gerarMetodoObjects(JDefinedClass klass, JavaBeanSchema javaBeanSchema){
		JClass queryset = jcm.ref(QuerySet.class);
		JDefinedClass qsInner = gerarQuerySet(klass,javaBeanSchema,queryset);
		JMethod metodoObjects = klass.method(JMod.PUBLIC|JMod.STATIC,queryset.narrow(klass), "objects");
		metodoObjects.body()._return(JExpr._new(qsInner.narrow(klass)).arg(JExpr._new(klass)));
	}

	private JDefinedClass gerarQuerySet(
			JDefinedClass klass,JavaBeanSchema javaBeanSchema, JClass queryset
	){
		try {
			JDefinedClass querySetInner = klass._class(JMod.PUBLIC|JMod.STATIC, "QuerySetImpl");
			JTypeVar generic = querySetInner.generify("T", klass);
			querySetInner._extends(queryset.narrow(generic));
			gerarQuerySetInnerInternals(klass, javaBeanSchema, querySetInner,generic);
			return querySetInner;
		} catch (JClassAlreadyExistsException e) {
			throw new RuntimeException(e);
		}
	}

	private void gerarQuerySetInnerInternals(
			JDefinedClass klass,
			JavaBeanSchema javaBeanSchema,
			JDefinedClass querySetInner,
			JTypeVar generic
	){

		List<String> colunasEmOrdem = ColunasUtils.colunasOrdenadasDoJavaBeanSchema(javaBeanSchema);
		/*
		 * prototipo
		 */
		JFieldVar prototipo = querySetInner.field(JMod.PRIVATE, generic, "prototipo");
		/*
		 * Construtor
		 */
		JMethod construtor = querySetInner.constructor(JMod.PROTECTED);
		JVar prototipoparam = construtor.param(generic, "prototipo");
		construtor.body().assign(JExpr.refthis(prototipo.name()), prototipoparam);

		/* ***********************
		 * Override              *
		 * protected DB getDb(){ *
		 *   return DB.getDB;    *
		 * }                     *
		 ************************/
		JMethod getDb = querySetInner.method(
				JMod.PROTECTED, jcm.ref(SQLiteDatabase.class), "getDb"
		);
		getDb.annotate(java.lang.Override.class);
		getDb.body()._return(getDbExpr());

		/* *******************************
		 * Override                      *
		 * protected String getTabela(){ *
		 *   return Classe.TABELA;       *
		 * }                             *
		 ********************************/
		JMethod getTabela = querySetInner.method(
				JMod.PROTECTED, jcm.ref(String.class), "getTabela"
		);
		getTabela.annotate(java.lang.Override.class);
		getTabela.body()._return(klass.fields().get(javaBeanSchema.getConstanteDaTabela()));

		/* **********************************
		 * Override                         *
		 * protected String[] getColunas(){ *
		 *   return new String[]{           *
		 *       ID, NOME, ENDERECO,        *
		 *       datetime(DATA_VISITA)      *
		 *   };                             *
		 * }                                *
		 ***********************************/
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

		/* *************************************************
		 * Override                                        *
		 * protected Classe cursorToObject(Cursor cursor){ *
		 *   Classe bean = new Classe();                   *
		 *   bean.id = cursor.getLong(0);                  *
		 *   bean.nome = cursor.                           *
		 * }                                               *
		 **************************************************/
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
				valor = jcm.ref(DateUtil.class)
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
					// variavel para lazy load
					// tansient == nao serializavel
					JFieldVar campo = klassA.field(
							JMod.PRIVATE|JMod.TRANSIENT,
							klassB,
							CamelCaseUtils.tolowerCamelCase(klassB.name())
					);
					/* ***********************************************
					 *   //getter de associacao "to-one"             *
					 *                                               *
					 * public Asscociada getAsscociada(){            *
					 *     if (                                      *
					 *         (associada == null)&&                 *
					 *         (idVendedor!= 0L)                     *
					 *     ) {                                       *
					 *         vendedor = Vendedor.objects().filter( *
					 *             Vendedor.ID +"=?",                *
					 *             idVendedor                        *
					 *         ).first();                            *
					 *     }                                         *
					 *     return vendedor;                          *
					 * }                                             *
					 ************************************************/
					JMethod getKlassB = klassA.method(
							JMod.PUBLIC,
							klassB,
							"get"+javaBeanSchemaB.getNome()
					);
					JBlock corpo = getKlassB.body();
					// if( (associada == null)&&(idVendedor!= 0L) )
					JConditional ifCampoNull = corpo._if(
							campo.eq(
									JExpr._null()
							).cand(
									klassA.fields()
									.get(javaBeanSchemaA.getPropriedade(oneToMany.getKeyToA()).getNome())
									.ne(JExpr.lit(ID_PADRAO))
							)
					);
					/* ***********************************************************
					 *  Associada.objects().filter(Vendedor.ID +"=?",idVendedor) *
					 ************************************************************/
					JInvocation invokeQuery = klassB.staticInvoke("objects")
						// .filter
						.invoke("filter")
						// Vendedor.ID +"=?"
						.arg(
								klassB.staticRef(klassB.fields().get(
										javaBeanSchemaB.getConstante(oneToMany.getReferenciaA())
								))
								.plus(JExpr.lit("=?"))
						)
						// idVendedor
						.arg(
								klassA.fields().get(
										javaBeanSchemaA.getPropriedade(oneToMany.getKeyToA()).getNome()
								)
						);
					// associada = Associada.objects().filter(Vendedor.ID +"=?",idVendedor).first()
					ifCampoNull._then().assign( campo, invokeQuery.invoke("first") );
					// return associada
					corpo._return(campo);
				} else if(
						associacao.getTabelaA().equals(javaBeanSchemaA.getTabela()) &&
						associacao.getTabelaB().equals(javaBeanSchemaB.getTabela())
				){
					String nomePlural = PluralizacaoUtils.pluralizar(javaBeanSchemaB.getNome());
					JClass collectionKlassB = jcm.ref(
							br.com.cds.mobile.framework.query.QuerySet.class
					).narrow(klassB);

//					JFieldVar campo = klassA.field(
//							JMod.PRIVATE|JMod.TRANSIENT,
//							collectionKlassB,
//							CamelCaseUtils.tolowerCamelCase(nomePlural)
//					);

					JMethod getKlassB = klassA.method(
							JMod.PUBLIC,
							collectionKlassB,
							"get"+ nomePlural
					);
					JBlock corpo = getKlassB.body();
//					JConditional ifCampoNull = corpo._if(campo.eq(JExpr._null()));
					JFieldVar referenciaA = klassA.fields().get(
							javaBeanSchemaA.getPropriedade(oneToMany.getReferenciaA()).getNome()
					);
					if(referenciaA == null)
						throw new RuntimeException(REFERENCIA_NAO_ENCONTRADA);
					JInvocation invokeAssociadaObjects = klassB.staticInvoke("objects")
						.invoke("filter").arg(klassB.staticRef(klassB.fields().get(
								javaBeanSchemaB.getConstante(oneToMany.getKeyToA())
						)).plus(JExpr.lit("=?"))).arg(
								referenciaA
						);
//					ifCampoNull._then().assign(
//							campo,
//							invokeAssociadaObjects
//					);
					corpo._return(invokeAssociadaObjects);
				}

			}
		}
	}


	private JExpression getDbExpr(){
		JClass dbclass = jcm.ref(dbClass);
		return dbclass.staticInvoke(getDbStaticMethod);
	}

	private JExpression boxify(JVar var){
		if(var.type().isPrimitive()){
			JPrimitiveType primType= (JPrimitiveType)var.type();
			return JExpr.cast(primType.boxify(), var);
		}
		return var;
	}

}
