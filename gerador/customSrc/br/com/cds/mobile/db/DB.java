package br.com.cds.mobile.db;

import java.lang.ref.SoftReference;

import br.com.cds.mobile.framework.config.Aplicacao;
import br.com.cds.mobile.framework.utils.AndroidUtils;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper{

	private static String DB_NOME = "default.db";
	private static int DB_VERSAO_INICIAL = 0;
	public static int DB_VERSAO = 0;
	private static String DB_VERSOES_RESOURCES_PREFIXO = "db_versao_";

	public static SoftReference<DB> instancia;

	public DB(){
		super(Aplicacao.getContext(), DB_NOME, null, DB_VERSAO);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		onUpgrade(db, DB_VERSAO_INICIAL, DB_VERSAO);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Context ctx = Aplicacao.getContext();
		for(int i=oldVersion; i<=newVersion;i++){
			executaScript(getSqlScriptPorVersao(ctx, i), db);
		}
	}

	public static SQLiteDatabase getDb(){
		DB db = null;
		if(instancia!=null)
			db = instancia.get();
		if(db==null){
			db = new DB();
			instancia = new SoftReference<DB>(db);
		}
		return db.getWritableDatabase();
	}

	private String getSqlScriptPorVersao(Context ctx, int versao ){
		int id = AndroidUtils.getResourceByName(ctx, DB_VERSOES_RESOURCES_PREFIXO + versao );
//		if(id!=0)
		return ctx.getString(id);
	}

	public void execMultipleSQL(SQLiteDatabase db, String[] sql) throws SQLException {
		if (sql == null) {
			return;
		}
		for (int i = 0; i < sql.length; i++) {
			if (sql[i].trim().length() > 0) {
				db.execSQL(sql[i]);
			}
		}
	}

	public void executaScript(String sql, SQLiteDatabase db) throws SQLException{
		String[] sqlArray = sql.toString().split(";");
		db.beginTransaction();
		try {
			execMultipleSQL(db, sqlArray);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		sqlArray = null;
		sql = null;
	}


}
