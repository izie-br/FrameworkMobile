package br.com.cds.mobile.geradores.dao;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import br.com.cds.mobile.framework.config.DB;
import br.com.cds.mobile.gerador.utils.SQLiteUtils;
import br.com.cds.mobile.geradores.javabean.JavaBeanSchema;
import br.com.cds.mobile.geradores.javabean.Propriedade;
import br.com.cds.mobile.geradores.util.CamelCaseUtils;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
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
		for(String nomeCampo : javaBeanSchema.getColunas()){
			Propriedade coluna = javaBeanSchema.getPropriedade(nomeCampo);
			JFieldVar campo = klass.fields().get(coluna.getNome());
			JExpression argumentoValor =
					// if campo instanceof Date
					(campo.type().name().equals("Date")) ?
							jcm.ref(SQLiteUtils.class).staticInvoke("dateToString").arg(campo) :
					// if campo instanceof Boolean
					(campo.type().name().equals("bool") || campo.name().equals("Boolean") ) ?
							JExpr.direct(campo.name()+"==1 ? true : false"):
					// else
							campo;
			JFieldVar constante = klass.fields().get(CamelCaseUtils.camelToUpper(coluna.getNome()));
			if(constante==null)
				throw new RuntimeException(String.format(
						"%s nao encontrada em %s.",
						CamelCaseUtils.camelToUpper(coluna.getNome()),
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


}
