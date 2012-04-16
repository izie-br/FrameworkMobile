package br.com.cds.mobile.geradores.dao;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import br.com.cds.mobile.flora.db.DB;
import br.com.cds.mobile.geradores.tabelaschema.CamelCaseTabelaDecorator;
import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

public class CodeModelDaoFactory {

	private static final String PACOTE = "br.com.cds.mobile.flora.db";
	private JCodeModel jcm;

	public CodeModelDaoFactory(JCodeModel jcm){
		this.jcm = jcm;
	}

	void gerarAcessoDB(JDefinedClass klass, TabelaSchema schema){
		Map<String,JFieldVar> constantesColunas = new HashMap<String, JFieldVar>();
		// gerar as constantes com nomes das colunas
		for(String nomeCampo : schema.getColunas().keySet()){
			JFieldVar campo = klass.fields().get(nomeCampo);
			JFieldVar constante = klass.field(
				JMod.PUBLIC|JMod.STATIC|JMod.FINAL,
				jcm.ref(String.class),
				camelToUpper(nomeCampo)
			);
			constantesColunas.put(nomeCampo, constante);
		}

		/*********************************************
		 * public boolean save(){
		 *   ContentValues cv = new ContentValues();
		 *   cv.put(ClasseBean.CAMPO,this.campo);
		 *   cv.put(....)
		 *   
		 *   return true;
		 * }
		 *************************/
		JMethod save = klass.method(JMod.PUBLIC, jcm.BOOLEAN, "save");
		JBlock corpo = save.body();
//		JClass dbclass = jcm.ref(DB.class);
//		JVar db = corpo.decl(jcm.ref(SQLiteDatabase.class),"db",dbclass.staticInvoke("getWritableDatabase"));
		JVar contentValues = corpo.decl(
				jcm.ref(ContentValues.class),
				"contentValues",
				JExpr._new(jcm.ref(ContentValues.class))
		);
		for(String nomeCampo : schema.getColunas().keySet()){
			contentValues.invoke("put").arg(constantesColunas.get(nomeCampo)).arg(klass.fields().get(nomeCampo));
		}
	}

	public static String camelToUpper(String input){
		StringBuilder out = new StringBuilder();
		// indice do "iterador"
		int i = 0;
		// remover underscores e espacos iniciais
		while(i<input.length()){
			if(CamelCaseTabelaDecorator.isWhiteSpace(input.charAt(i)))
				i++;
			else
				break;
		}
		// conferir se a string esta vazia
		if(i==input.length()-1)
			throw new RuntimeException(String.format("nome \"%s\" eh vazio",input));
		// adicionar a primeira letra
		out.append(Character.toUpperCase(input.charAt(i)));
		i++;
		// para as seguintes, a cada letra "upper", adicionar um espaco
		while(i<input.length()){
			// 
			if(Character.isUpperCase(input.charAt(i))){
				out.append('_');
			}
			out.append(Character.toUpperCase(input.charAt(i)));
			i++;
		}

		return out.toString();
	}
}
