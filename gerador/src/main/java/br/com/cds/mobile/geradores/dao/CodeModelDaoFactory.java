package br.com.cds.mobile.geradores.dao;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.codemodel.JTryBlock;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import br.com.cds.mobile.framework.query.QuerySet;
import br.com.cds.mobile.framework.query.Table;
import br.com.cds.mobile.framework.utils.CamelCaseUtils;
import br.com.cds.mobile.framework.utils.DateUtil;
import br.com.cds.mobile.framework.utils.SQLiteUtils;
import br.com.cds.mobile.geradores.filters.associacao.Associacao;
import br.com.cds.mobile.geradores.filters.associacao.AssociacaoManyToMany;
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

//	private static final String REFERENCIA_NAO_ENCONTRADA =
//			"Referencia em associacao to many nao encontrada";
//	private static final String CONSTANTE_NAO_ENCONTRADA_FORMAT =
//			"%s nao encontrada em %s.";

	// o valor 0 nao pode ser uma PK no sqlite
	public static final long ID_PADRAO = 0;
	// SQL's

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
	 * <p>
	 *   Gera os metodos de insert/update/delete,
	 *   no estilo "active record"
	 * </p>
	 * <p>Cria tambem metodo de busca, retornando um QuerySet</p>
	 * @param klass
	 * @param javaBeanSchema
	 */
	public void gerarAcessoDB(
		JDefinedClass klass,
		JavaBeanSchema javaBeanSchema
	){
		// inicializando a PK para o valor padrao (NULL, 0, etc...)
		Propriedade primaryKey = javaBeanSchema.getPrimaryKey();
		if(primaryKey!=null){
			JFieldVar pk = klass.fields().get(primaryKey.getNome());
			pk.init(JExpr.lit(ID_PADRAO));
			String pkNome = primaryKey.getNome();
			// remover o setter da PK
			String pkSetter =
				"set" +
				Character.toUpperCase(pkNome.charAt(0)) +
				pkNome.substring(1);
			JMethod pkmetodo = null;
			for(JMethod metodo : klass.methods())
				if(metodo.name().equals(pkSetter))
					pkmetodo = metodo;
			if(pkmetodo!=null)
				klass.methods().remove(pkmetodo);
		}
		else{
			generateConstrutorForCompundPrimaryKey(
				klass,
				javaBeanSchema
			);
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

		Propriedade primaryKey = javaBeanSchema.getPrimaryKey();

		Map<String, JFieldVar> fields = new LinkedHashMap<String, JFieldVar>();
		for(String coluna : ColunasUtils.colunasOrdenadasDoJavaBeanSchema(javaBeanSchema)){
			Propriedade propriedade = javaBeanSchema.getPropriedade(coluna);
			if( !(
					primaryKey!=null &&
					propriedade.getNome().equals(primaryKey.getNome())
			) ){
				fields.put(coluna, klass.fields().get(propriedade.getNome()));
			}
		}
		/*
		 * ContentValues cv = new ContentValues();
		 * cv.put(ClasseBean.CAMPO,this.campo);
		 * cv.put(*);
		 */
		JVar contentValues = generateContentValues(klass, fields, corpo);

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
			.arg(klass.fields().get(javaBeanSchema.getConstanteDaTabela()).invoke("getName"))
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
		gerarBlocoUpdate(klass, javaBeanSchema, elseBlock, db, contentValues);
	}

	private JVar generateContentValues(
			JDefinedClass klass,
			Map<String,JFieldVar> fields,
			JBlock body
	) {
		/* ********************************************
		 *   ContentValues cv = new ContentValues();  *
		 *********************************************/
		JVar contentValues = body.decl(
				jcm.ref(ContentValues.class),
				"contentValues",
				JExpr._new(jcm.ref(ContentValues.class))
		);
		for (String column : fields.keySet()) {
			JFieldVar field = fields.get(column);
			JExpression argumentoValor =
					// if campo instanceof Date
					(field.type().name().equals("Date")) ?
							// value = DateUtil.timestampToString(date)
							jcm.ref(DateUtil.class).staticInvoke("timestampToString").arg(field) :
					// if campo instanceof Boolean
					(field.type().name().equals("boolean") || field.name().equals("Boolean") ) ?
							// value = (campoBool? 1 : 0)
							JOp.cond(field, JExpr.lit(1),JExpr.lit(0)):
					// else
							// value = campo
							field;
			/* ****************************************
			 *   cv.put("campo", <expressao value>);  *
			 *****************************************/
			body.add(
				contentValues.invoke("put")
					.arg(JExpr.lit(column))
					.arg(argumentoValor)
			);
		}
		return contentValues;
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
		JVar value = block.decl(jcm.INT, "value", db.invoke("update")
			.arg(klass.fields().get(javaBeanSchema.getConstanteDaTabela()).invoke("getName"))
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
						.arg(getQForPrimaryKeys(klass, javaBeanSchema))
					.invoke("first")
		);
		JConditional ifExist =  block._if(existente.eq(JExpr._null()));
		JBlock thenBlock = ifExist._then();
		JExpression valueExpr = db.invoke("insertOrThrow")
			.arg(klass.fields().get(javaBeanSchema.getConstanteDaTabela()).invoke("getName"))
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
				).invoke("getName").plus(JExpr.lit("=?"));
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

	private JInvocation getQForPrimaryKeys (
		JDefinedClass klass, JavaBeanSchema javaBeanSchema
	) {
		Iterator<String> primarykeys = javaBeanSchema.getPrimaryKeyColumns().iterator();
		JInvocation q = null;
		while(primarykeys.hasNext()){
			String primarykey = primarykeys.next();
			JInvocation pkEqInvocation = 
				klass.fields()
					.get(javaBeanSchema.getConstante(primarykey))
					.invoke("eq")
						.arg(klass.fields().get(javaBeanSchema.getPropriedade(primarykey).getNome()));
			if (q == null )
				q = pkEqInvocation;
			else
				q = q.invoke("and").arg(pkEqInvocation);
		}
		return q;
	}

	public void gerarMetodoDelete(JDefinedClass klass, JavaBeanSchema javaBeanSchema){
		/* **********************************************************
		 * public boolean delete(){                                 *
		 *     super.delete();                                      *
		 *     if(id!=ID_PADRAO) {                                  *
		 *        db.delete(TABELA,ID+"=?",new String[] { id });    *
		 *        for (Associada obj : getAssociadas() {            *
		 *            obj.setPai(null);                             *
		 *        }                                                 *
		 *        return true;                                      *
		 *     }                                                    *
		 *     return false;                                        *
		 * }                                                        *
		 ***********************************************************/
		JMethod delete = klass.method(JMod.PUBLIC, jcm.BOOLEAN, "delete");
		JBlock body = delete.body();
		JVar db = body.decl(jcm.ref(SQLiteDatabase.class),"db",getDbExpr());
		body.directStatement("synchronized (" + db.name() +") {");
		// if(id==ID_PADRAO)
		JTryBlock tryCatch = body._try();
		JBlock tryBody = tryCatch.body();
		tryBody.add(db.invoke("beginTransaction"));

		// alterar/deletar associadas
		Collection<Associacao> associacoes = javaBeanSchema.getAssociacoes();
		if (associacoes != null && associacoes.size() > 0) {
			for (Associacao association : associacoes) {
				generateAssociatedDelete(
					klass, javaBeanSchema,
					association,
					tryBody, db
				);
			}
		}

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
		tryBody._if(ifexpr.not())._then()._return(JExpr.lit(false));
		// getDb().getWritableDatabase().delete(TABELA, ID + "=?", new String[] { "" + id });
		JVar affected = tryBody.decl(
			jcm.INT,
			"affected",
			db.invoke("delete")
				.arg(klass.fields().get(javaBeanSchema.getConstanteDaTabela()).invoke("getName"))
				// ID+"=?"
				.arg(getPrimaryKeysQueryString(klass, javaBeanSchema))
				// new String[]{id}
				.arg(getPrimaryKeysArray(klass, javaBeanSchema))
		);

		tryBody._if(affected.eq(JExpr.lit(0)))
			._then()._return(JExpr.lit(false));
		tryBody.add(db.invoke("setTransactionSuccessful"));

		tryCatch._finally().add(
			db.invoke("endTransaction")
		);

		body.directStatement("}"); // JExpr.direct("synchronized (db) {");

		body._return(JExpr.lit(true));
	}

	private void generateAssociatedDelete (
		JDefinedClass klass, JavaBeanSchema javaBeanSchema,
		Associacao association,
		JBlock body, JVar db
	) {
		if (association instanceof AssociacaoOneToMany) {
			AssociacaoOneToMany one2many = (AssociacaoOneToMany)association;
			if (one2many.getTabelaB().getNome().equals(
				javaBeanSchema.getTabela().getNome()
			)) {
				return;
			}
			if (one2many.isNullable()) {
				JVar contentValues = body.decl(
					jcm.ref(ContentValues.class),
					"contentValues",
					JExpr._new(jcm.ref(ContentValues.class))
				);
				body.add(
					contentValues.invoke("put")
						.arg(JExpr.lit(one2many.getKeyToA()))
						.arg(JExpr._null())
				);
				body.add(
					db.invoke("update")
						.arg(JExpr.lit(one2many.getTabelaB().getNome()))
						.arg(contentValues)
						.arg( JExpr.lit(one2many.getKeyToA() + "=?") )
						.arg(getPrimaryKeysArray(klass, javaBeanSchema))
				);
			} else {
				body.add(
					db.invoke("delete")
						.arg(JExpr.lit(one2many.getTabelaB().getNome()))
						.arg( JExpr.lit(one2many.getKeyToA() + "=?") )
						.arg(getPrimaryKeysArray(klass, javaBeanSchema))
				);
			}
		}
		else if (association instanceof AssociacaoManyToMany) {
			AssociacaoManyToMany many2many = (AssociacaoManyToMany)association;
			String throughTableKey =
				//if
				(many2many.getTabelaA().getNome().equals(javaBeanSchema.getTabela().getNome())) ?
					// keyToA
					many2many.getKeyToA() :
					// keyToB
					many2many.getKeyToB();
				throughTableKey = many2many.getKeyToA();
			body.add(
				db.invoke("delete")
					.arg(JExpr.lit(many2many.getTabelaJuncao().getNome()))
					.arg( JExpr.lit(throughTableKey+ "=?") )
					.arg(getPrimaryKeysArray(klass, javaBeanSchema))
			);
		}
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

		/* ********************************
		 * Override                       *
		 * protected Table getTabela () { *
		 *   return Classe.TABELA;        *
		 * }                              *
		 *********************************/
		JMethod getTabela = querySetInner.method(
				JMod.PROTECTED, jcm.ref(Table.class), "getTabela"
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
		JClass columnClass = jcm.ref(Table.Column.class).narrow(jcm.wildcard());
		JMethod getColunas = querySetInner.method(
				JMod.PROTECTED, columnClass.array(), "getColunas"
		);
		getColunas.annotate(java.lang.Override.class);
		JArray colunasArray = JExpr.newArray(columnClass);
		for(String coluna : colunasEmOrdem){
			JExpression elementoArray = klass.fields().get(
					javaBeanSchema.getConstante(coluna)
			);
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
			if(associacao instanceof AssociacaoManyToMany){
				AssociacaoManyToMany m2m = (AssociacaoManyToMany) associacao;
				if(
						m2m.getTabelaA ().equals (javaBeanSchemaA.getTabela ()) &&
						m2m.getTabelaB ().equals (javaBeanSchemaB.getTabela ())
				) {
					JFieldVar throughTableA = klassA.field(
						JMod.PUBLIC|JMod.STATIC|JMod.FINAL,
						Table.class,
						"TABLE_" + CamelCaseUtils.camelToUpper (CamelCaseUtils.tolowerCamelCase (m2m.getTabelaJuncao ().getNome ())),
						JExpr._new (jcm.ref (Table.class)).arg (m2m.getTabelaJuncao ().getNome ())
					);
					JFieldVar throughTableB = klassB.field(
						JMod.PUBLIC|JMod.STATIC|JMod.FINAL,
						Table.class,
						"TABLE_" + CamelCaseUtils.camelToUpper (CamelCaseUtils.tolowerCamelCase (m2m.getTabelaJuncao ().getNome ())),
						klassA.staticRef(throughTableA)
					);

					JFieldVar throughTableKeyToA = klassA.field(
						JMod.PUBLIC|JMod.STATIC|JMod.FINAL,
						Table.Column.class,
						CamelCaseUtils.camelToUpper (CamelCaseUtils.tolowerCamelCase (
							m2m.getTabelaJuncao ().getNome () +
							"_" + (m2m.getKeyToA ()))),
						throughTableA.invoke ("addColumn")
							.arg (JExpr.dotclass (jcm.ref (javaBeanSchemaA.getPropriedade (m2m.getReferenciaA ()).getType ())))
							.arg (m2m.getKeyToA ())
					);
					JFieldVar throughTableKeyToB = klassB.field(
						JMod.PUBLIC|JMod.STATIC|JMod.FINAL,
						Table.Column.class,
						CamelCaseUtils.camelToUpper (CamelCaseUtils.tolowerCamelCase(
							m2m.getTabelaJuncao ().getNome () +
							"_" + (m2m.getKeyToB ()))),
						throughTableB.invoke ("addColumn")
							.arg (JExpr.dotclass (jcm.ref (javaBeanSchemaB.getPropriedade (m2m.getReferenciaB ()).getType ())))
							.arg (m2m.getKeyToB ())
					);

					JFieldVar referenceA = klassA.fields ().get (
							javaBeanSchemaA.getPropriedade (m2m.getReferenciaA ()).getNome ()
					);
					JFieldVar referenceB = klassB.fields ().get (
							javaBeanSchemaB.getPropriedade (m2m.getReferenciaB ()).getNome ()
					);
					JFieldVar columnRefA = klassA.fields ().get (
							javaBeanSchemaA.getConstante (m2m.getReferenciaA ())
					);
					JFieldVar columnRefB = klassB.fields ().get (
							javaBeanSchemaB.getConstante (m2m.getReferenciaB ())
					);

					generateToManyAssociation (
						klassA, javaBeanSchemaA,
						referenceA, throughTableKeyToA,
						klassB, javaBeanSchemaB,
						columnRefB, throughTableKeyToB
					);
					generateToManyAssociation (
						klassB, javaBeanSchemaB,
						referenceB, throughTableKeyToB,
						klassA, javaBeanSchemaA,
						columnRefA, throughTableKeyToA
					);

					generateManyToManyPersistence (
						klassA, javaBeanSchemaA,
						referenceA, throughTableKeyToA,
						klassB, javaBeanSchemaB,
						m2m.getReferenciaB(), throughTableKeyToB,
						throughTableA
					);
					generateManyToManyPersistence (
						klassB, javaBeanSchemaB,
						referenceB, throughTableKeyToB,
						klassA, javaBeanSchemaA,
						m2m.getReferenciaA(), throughTableKeyToA,
						throughTableB
					);
				}
			}

			else if(associacao instanceof AssociacaoOneToMany){
				AssociacaoOneToMany oneToMany = (AssociacaoOneToMany)associacao;
				if(
						associacao.getTabelaB().equals(javaBeanSchemaA.getTabela()) &&
						associacao.getTabelaA().equals(javaBeanSchemaB.getTabela())
				){
					generateToOneAssociation(klassA, javaBeanSchemaA, klassB,
							javaBeanSchemaB, oneToMany);
				} else if(
						associacao.getTabelaA().equals(javaBeanSchemaA.getTabela()) &&
						associacao.getTabelaB().equals(javaBeanSchemaB.getTabela())
				){
					JFieldVar referenceA = klassA.fields().get(
							javaBeanSchemaA.getPropriedade(oneToMany.getReferenciaA()).getNome()
					);
					JFieldVar columnRefB = klassB.fields().get(
							javaBeanSchemaB.getConstante(oneToMany.getKeyToA())
					);
					generateToManyAssociation(
							klassA, javaBeanSchemaA,
							referenceA, null,
							klassB,javaBeanSchemaB,
							columnRefB, null
					);
				}

			}
		}
	}

	private void generateToOneAssociation(JDefinedClass klassA,
			JavaBeanSchema javaBeanSchemaA, JDefinedClass klassB,
			JavaBeanSchema javaBeanSchemaB, AssociacaoOneToMany oneToMany) {
		JFieldVar keyToB = klassA.fields().get(
			javaBeanSchemaA.getPropriedade(oneToMany.getKeyToA()).getNome()
		);
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
					)).invoke("eq").arg(keyToB)
			);
		// associada = Associada.objects().filter(Vendedor.ID +"=?",idVendedor).first()
		ifCampoNull._then().assign( campo, invokeQuery.invoke("first") );
		// return associada
		corpo._return(campo);
		/*
		 *  // setter para onetomany
		 *
		 * public void setAssociada (Associada obj) {
		 *     if (obj.getId () == ID_PADRAO)
		 *         throw new RuntimeException (ASSOCIADA_SEM_ID);
		 *     this.idAssociada = obj.getId ();
		 *     this.associada = obj;
		 * }
		 */
		JMethod methodSetAssociated = klassA.method(
			JMod.PUBLIC,
			jcm.VOID,
			"set" + javaBeanSchemaB.getNome()
		);
		JVar obj = methodSetAssociated.param(klassB, "obj");
		corpo = methodSetAssociated.body();
		String getterRefB = "get" +
			Character.toUpperCase(oneToMany.getReferenciaA().charAt(0)) +
			oneToMany.getReferenciaA().substring(1);

		// Caso o objeto (da classe associada) nao tenha sido inserido
		// no banco ainda
		// if (obj.getId () == ID_PADRAO)
		//     throw new RuntimeException (ASSOCIADA_SEM_ID);
		corpo._if(obj.invoke(getterRefB).eq(JExpr.lit(ID_PADRAO)))
			._then()._throw(
				JExpr._new(jcm.ref(RuntimeException.class))
					.arg(JExpr.lit(getterRefB+" retornou null"))
			);
		corpo.assign(keyToB, obj.invoke(getterRefB));
		corpo.assign(campo, obj);
	}

	private void generateToManyAssociation(
			JDefinedClass klassA, JavaBeanSchema javaBeanSchemaA,
			JFieldVar referenceA, JFieldVar columnThroughTableToA,
			JDefinedClass klassB, JavaBeanSchema javaBeanSchemaB,
			JFieldVar columnRefB, JFieldVar columnThroughTableToB
	) {
		String nomePlural = PluralizacaoUtils.pluralizar(javaBeanSchemaB.getNome());
		JClass collectionKlassB = jcm.ref(
				br.com.cds.mobile.framework.query.QuerySet.class
		).narrow(klassB);

		/*
		 * public Collection<Associada> getAssociadas () {
		 *     return Associada.objects().filter(Associada.PAI_ID.eq(id));
		 * }
		 */
		JMethod getKlassB = klassA.method(
				JMod.PUBLIC,
				collectionKlassB,
				"get"+ nomePlural
		);
		JBlock body = getKlassB.body();
		JInvocation invokeAssociadaObjects = klassB.staticInvoke("objects")
			.invoke("filter");
		if (columnThroughTableToA == null || columnThroughTableToB == null) {
			/*
			 * Caso OneToMany
			 * Sem tabela "though table"
			 * 
			 * Exemplo de codigo gerado:
			 * 
			 *     Associada.objects().filter(Associada.PAI_ID.eq(id));
			 */
			invokeAssociadaObjects = invokeAssociadaObjects.arg(
					klassB.staticRef(columnRefB).invoke("eq")
						.arg(referenceA)
				);
		} else {
			invokeAssociadaObjects = invokeAssociadaObjects.arg(
			/*
			 * Caso ManyToMany
			 * Com "though table"
			 * 
			 * Exemplo de codigo gerado:
			 * 
			 *     Associada.objects().filter(
			 *         Associada.PAI_ID.eq(Associada.THROUGH_TABLE_ASSOCIADA_ID)
			 *             .and(Associada.THROUGH_TABLE_CLASSE_ID.eq(id)
			 *     );
			 */
				klassB.staticRef(columnRefB).invoke("eq")
						.arg(klassB.staticRef(columnThroughTableToB))
					.invoke("and").arg(columnThroughTableToA.invoke("eq").arg(referenceA)));
		}
		body._return(invokeAssociadaObjects);
	}

	private void generateManyToManyPersistence (
			JDefinedClass klassA, JavaBeanSchema javaBeanSchemaA,
			JFieldVar referenceA, JFieldVar columnThroughTableToA,
			JDefinedClass klassB, JavaBeanSchema javaBeanSchemaB,
			String columnRefB, JFieldVar columnThroughTableToB,
			JFieldVar throughTable
	) {
		String getterReferenceB = "get" +
			Character.toUpperCase( columnRefB.charAt (0)) +
			columnRefB.substring (1);
		/*
		 * public boolean addAssociada(Associada obj) {
		 *     if (id == 0)
		 *         return false;
		 *     ContentValues contentValues = new ContentValues();
		 *     contentValues.put(Classe.THROUGH_TABLE_CLASSE_ID.getName(), id);
		 *     contentValues.put(Associada.THROUGH_TABLE_ASSOCIADA_ID.getName(), obj.getId());
		 *     SQLiteDatabase db = DB.getDb();
		 *     long value = db.insertOrThrow(Classe.THROUGH_TABLE_CLASSE_ID.getName(), null, contentValues);
		 *     return (value > 0);
		 */
		JMethod addMethod = klassA.method(
			JMod.PUBLIC,
			jcm.BOOLEAN,
			"add" + javaBeanSchemaB.getNome()
		);
		JVar obj = addMethod.param(klassB, "obj");
		JBlock body = addMethod.body();
		String pkName = javaBeanSchemaA.getPrimaryKey().getNome();
		if (pkName != null) {
			JFieldVar pk = klassA.fields().get(pkName);
			body._if (pk.eq (JExpr.lit (ID_PADRAO)))
				._then()._return (JExpr.lit(false));
		}
		/* ********************************************
		 *   ContentValues cv = new ContentValues();  *
		 *********************************************/
		JVar contentValues = body.decl(
				jcm.ref(ContentValues.class),
				"contentValues",
				JExpr._new(jcm.ref(ContentValues.class))
		);

		// contentValues.put(Classe.THROUGH_TABLE_CLASSE_ID.getName(), id);
		body.add (contentValues.invoke ("put")
			.arg (klassA.staticRef (columnThroughTableToA).invoke ("getName"))
			.arg (referenceA)
		);

		// contentValues.put(Associada.THROUGH_TABLE_ASSOCIADA_ID.getName(), obj.getId());
		body.add (contentValues.invoke ("put")
			.arg (klassB.staticRef (columnThroughTableToB).invoke ("getName"))
			.arg (obj.invoke (getterReferenceB))
		);

		// SQLiteDatabase db = DB.getDb();
		JVar db = body.decl(jcm.ref(SQLiteDatabase.class),"db",getDbExpr());

		// db.insertOrThrow(Classe.THROUGH_TABLE_CLASSE_ID.getName(), null, contentValues);
		JExpression valueExpr = db.invoke("insertOrThrow")
			.arg(klassA.staticRef(throughTable).invoke("getName"))
			.arg(JExpr._null())
			.arg(contentValues);

		// long value = db.insertOrThrow(Customer.TABLE_TB_CUSTOMER_JOIN_DOCUMENT.getName(), null, contentValues);
		JVar valueId = body.decl(jcm.LONG, "value",valueExpr);

		// return (value > 0);
		body._return (valueId.gt(JExpr.lit(0)));

		/*
		 * public boolean removeDocument(Document obj) {
		 *     if (id == 0L) {
		 *         return false;
		 *     }
		 *     SQLiteDatabase db = br.com.cds.mobile.framework.test.db.DB.getDb();
		 *     Cursor cursor = db.query(
		 *         THROUGH_TABLE.getName(),
		 *         (new String[]{"rowid"}),
		 *         Classe.THROUGH_TABLE_CLASSE_ID.getName()+"=? AND " +
		 *             Associada.THROUGH_TABLE_ASSOCIADA_ID.getName())+"=?",
		 *         new String[] {((Long) id).toString(), ((Long) obj.getId()).toString()},
		 *         null,
		 *         null,
		 *         null,
		 *         "1" // limit
		 *     );
		 *     if (!cursor.moveToNext()) {
		 *         return false;
		 *     }
		 *     long rowid = cursor.getLong(0);
		 *     cursor.close();
		 *     if (rowid<= 0) {
		 *         return false;
		 *     }
		 *     long affected = db.delete(
		 *         Classe.THROUGH_TABLE.getName(),
		 *         "rowid=?",
		 *         new String[] {((Long) rowid).toString()}
		 *     );
		 *     return (affected == 1);
		 * }
		 */
		JMethod removeMethod = klassA.method(
			JMod.PUBLIC,
			jcm.BOOLEAN,
			"remove" + javaBeanSchemaB.getNome()
		);
		obj = removeMethod.param(klassB, "obj");
		body = removeMethod.body();
		if (pkName != null) {
			// if (id == 0L) 
			//     return false;
			JFieldVar pk = klassA.fields().get(pkName);
			body._if (pk.eq (JExpr.lit (ID_PADRAO)))
				._then()._return (JExpr.lit(false));
		}

		// SQLiteDatabase db = DB.getDb();
		db = body.decl(jcm.ref(SQLiteDatabase.class),"db",getDbExpr());

		// Cursor cursor = db.query( ... )
		JVar cursor = body.decl(
			jcm.ref(Cursor.class),
			"cursor",
			db.invoke("query")
				.arg(throughTable.invoke("getName"))
				.arg(JExpr.direct("new String[]{\"rowid\"}"))
				.arg(klassA.staticRef(
					columnThroughTableToA).invoke("getName")
					.plus(JExpr.lit("=? AND "))
					.plus(klassB.staticRef(columnThroughTableToB).invoke("getName"))
					.plus(JExpr.lit("=?"))
				)
				.arg(JExpr.newArray(jcm.ref(String.class))
					.add(boxify(referenceA).invoke("toString"))
					.add(((JExpression)JExpr.cast(
						jcm.ref(javaBeanSchemaB.getPropriedade(columnRefB).getType()),
						obj.invoke(getterReferenceB)
					)).invoke("toString"))
				)
				.arg(JExpr._null())
				.arg(JExpr._null())
				.arg(JExpr._null())
				.arg(JExpr.lit("1"))
		);

		// if (!cursor.moveToNext())
		//     return false;
		body._if(cursor.invoke("moveToNext").not())
			._then()._return(JExpr.lit(false));
		Class<?> referenceType;
		try {
			referenceType = Class.forName(
				referenceA.type().boxify()._package().name() +
				"." + referenceA.type().boxify().name()
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// long rowid = cursor.getLong(0);
		JVar rowid = body.decl(
			referenceA.type(),
			"rowid",
			cursor.invoke(SQLiteGeradorUtils.metodoGetDoCursorParaClasse(
				referenceType
			)).arg(JExpr.lit(0))
		);

		// db.close();
		body.add(cursor.invoke("close"));

		// if (rowid<= 0)
		//     return false;
		body._if(rowid.lte(JExpr.lit(0)))
			._then()._return(JExpr.lit(false));

		// long affected = db.delete(
		//     Classe.THROUGH_TABLE.getName(),
		//     "rowid=?",
		//     new String[] {((Long) rowid).toString()}
		// );
		JVar affeted = body.decl(
			jcm.LONG,
			"affected",
			db.invoke("delete")
				.arg(klassA.staticRef(throughTable).invoke("getName"))
				.arg(JExpr.lit("rowid=?"))
				.arg(JExpr.newArray(jcm.ref(String.class))
					.add(boxify(rowid).invoke("toString"))
				)
		);

		// return (affected == 1);
		body._return(affeted.eq(JExpr.lit(1)));

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
