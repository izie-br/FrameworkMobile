package br.com.cds.mobile.framework.config;

import java.lang.ref.SoftReference;

import br.com.cds.mobile.framework.LogPadrao;
import br.com.cds.mobile.framework.utils.AndroidUtils;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper{

	private static String DB_NOME = "default.db";
	private static int DB_VERSAO_INICIAL = 0;
	private static int DB_VERSAO = 0;
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
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		Context ctx = Aplicacao.getContext();
		for(int i=DB_VERSAO_INICIAL; i<=DB_VERSAO;i++){
			executaScript(getSqlScriptPorVersao(ctx, i), db);
		}
	}

	public static SQLiteDatabase getDb(){
		DB db = null;
		if(instancia==null)
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

	public void execMultipleSQL(SQLiteDatabase db, String[] sql, boolean log) {
		if (sql == null) {
			return;
		}
		for (int i = 0; i < sql.length; i++) {
			try {
				if (log) {
					LogPadrao.d("opera‹o banco:" + sql[i]);
				}
				if (sql[i].trim().length() > 0) {
					db.execSQL(sql[i]);
				}
			} catch (Throwable e) {
				LogPadrao.d(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void executaScript(String sql, SQLiteDatabase db) {
		String[] sqlArray = sql.toString().split(";");
		db.beginTransaction();
		try {
			execMultipleSQL(db, sqlArray, false);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			LogPadrao.d("Erro ao criar o banco " + DB_NOME+ e.toString());
		} finally {
			db.endTransaction();
		}
		sqlArray = null;
		sql = null;
	}


}
