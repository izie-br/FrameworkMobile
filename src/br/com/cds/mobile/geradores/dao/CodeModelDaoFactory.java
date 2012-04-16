package br.com.cds.mobile.geradores.dao;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import br.com.cds.mobile.framework.config.DB;
import br.com.cds.mobile.gerador.utils.SQLiteUtils;
import br.com.cds.mobile.geradores.CodeModelBeanFactory;
import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;
import br.com.cds.mobile.geradores.util.CamelCaseUtils;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

public class CodeModelDaoFactory {

	// private static final String PACOTE = "br.com.cds.mobile.flora.db";
	private JCodeModel jcm;

	public CodeModelDaoFactory(JCodeModel jcm){
		this.jcm = jcm;
	}

	public void gerarAcessoDB(JDefinedClass klass, TabelaSchema schema, String constanteColunaId, String campoId){
		gerarMetodoSave(klass, schema, constanteColunaId,campoId);
		gerarMetodoDelete(klass, schema, constanteColunaId, campoId);
	}

	public void gerarMetodoSave(JDefinedClass klass, TabelaSchema schema, String constanteColunaId, String campoId){
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
		for(String nomeCampo : schema.getPropriedades().keySet()){
			TabelaSchema.Coluna coluna = schema.getPropriedades().get(nomeCampo);
			JFieldVar campo = klass.fields().get(nomeCampo);
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
		JConditional ifIdNull = corpo._if(klass.fields().get(campoId)
				.eq(JExpr.direct(CodeModelBeanFactory.ID_PADRAO)));
		// db.insertOrThrow(getTabela(), null, contentValues);
		ifIdNull._then().invoke(db, "insertOrThrow")
			.arg(JExpr.lit(schema.getTabela()))
			.arg(JExpr._null())
			.arg(contentValues);
		// else
		// db.update(getTabela(), objetoToContentValue(objeto), getColunaId() + "=?", new String[] { "" + id });
		ifIdNull._else().invoke(db,"update")
			.arg(JExpr.lit(schema.getTabela()))
			.arg(contentValues)
			.arg(klass.fields().get(CamelCaseUtils.toUpperCamelCase(constanteColunaId)).plus(JExpr.lit("=?")))
			.arg(JExpr.newArray(jcm.ref(String.class)).add(JExpr.lit("").plus(klass.fields().get(campoId))));

		corpo._return(JExpr.lit(true));
	}

	public void gerarMetodoDelete(JDefinedClass klass, TabelaSchema schema, String constanteColunaId, String campoId){
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
		JConditional ifIdNotNull = corpo._if(klass.fields().get(campoId)
				.ne(JExpr.direct(CodeModelBeanFactory.ID_PADRAO)));
		// getDb().getWritableDatabase().delete(getTabela(), ID + "=?", new String[] { "" + id });

		JBlock blocoThen = ifIdNotNull._then();
		blocoThen.invoke(db, "delete")
			.arg(JExpr.lit(schema.getTabela()))
			.arg(klass.fields().get(CamelCaseUtils.toUpperCamelCase(constanteColunaId)).plus(JExpr.lit("=?")))
			.arg(JExpr.newArray(jcm.ref(String.class)).add(JExpr.lit("").plus(klass.fields().get(campoId))));
		blocoThen._return(JExpr.lit(true));
		corpo._return(JExpr.lit(false));
	}

	public Map<String, JFieldVar> gerarConstantesComNomesDasColunas(JDefinedClass klass, TabelaSchema schema) {
		Map<String,JFieldVar> constantesColunas = new HashMap<String, JFieldVar>();
		// gerar as constantes com nomes das colunas
		for(TabelaSchema.Coluna coluna : schema.getColunas()){
			String nomeConstanteUpperCase = CamelCaseUtils.camelToUpper(coluna.getNome());
			JFieldVar constante = klass.fields().get(nomeConstanteUpperCase);
			if(constante==null)
				constante = klass.field(
					JMod.PUBLIC|JMod.STATIC|JMod.FINAL,
					jcm.ref(String.class),
					nomeConstanteUpperCase,
					JExpr.lit(coluna.getNome())
				);
			constantesColunas.put(coluna.getNome(), constante);
		}
		return constantesColunas;
	}

}
