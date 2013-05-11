package com.quantium.mobile.framework.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.quantium.mobile.framework.BaseApplication;
import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.utils.AndroidUtils;

public class BaseDB extends SQLiteOpenHelper {

	private static String DB_VERSOES_RESOURCES_PREFIXO = "db_versao_";

	public static BaseDB instancia;
	private int dbVersaoInicial;
	private int dbVersao;
	private String dbNome;

	public BaseDB(String name, int version, int dbVersaoInicial) {
		super(BaseApplication.getContext(), name, null, version);
		this.dbVersaoInicial = dbVersaoInicial;
		this.dbVersao = version;
		this.dbNome = name;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		onUpgrade(db, dbVersaoInicial, dbVersao);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Context ctx = BaseApplication.getContext();
		int i = oldVersion;
		do {
			executaScript(getSqlScriptPorVersao(ctx, ++i), db);
		} while (i < newVersion);
	}

	public static SQLiteDatabase getDb() {
		// if (instancia == null || !instancia.isOpen())
		// instancia = new DB().getWritableDatabase();
		return instancia.getWritableDatabase();
	}

	// @Override
	// protected void finalize() throws Throwable {
	// this.close();
	// super.finalize();
	// }

	private String getSqlScriptPorVersao(Context ctx, int versao) {
		int id = AndroidUtils.getResourceByName(ctx, "string/"
				+ DB_VERSOES_RESOURCES_PREFIXO + versao);
		if (id != 0)
			return ctx.getString(id);
		return "";
	}

	public void execMultipleSQL(SQLiteDatabase db, String[] sql)
			throws SQLException {
		if (sql == null) {
			return;
		}
		for (int i = 0; i < sql.length; i++) {
			if (sql[i].trim().length() > 0) {
				db.execSQL(sql[i]);
			}
		}
	}

	public void executaScript(String sql, SQLiteDatabase db)
			throws SQLException {
		String[] sqlArray = splitSql(sql);
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

	// TODO este metodo nao funciona com string com ponto-e-virgula;
	private String[] splitSql(String sql) {
		String sqlArray[] = sql.toString().split(";");
		ArrayList<String> list = new ArrayList<String>();
		Pattern triggerPat = Pattern.compile("create\\s+trigger",
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Pattern triggerEnd = Pattern.compile("\\send\\s*$",
				Pattern.CASE_INSENSITIVE);
		for (int i = 0; i < sqlArray.length; i++) {
			int last = i;
			if (triggerPat.matcher(sqlArray[i]).find()) {
				int j = i;
				do {
					j++;
					sqlArray[i] += ";" + sqlArray[j];
					sqlArray[j] = null;
				} while (j < (sqlArray.length - 1)
						&& !triggerEnd.matcher(sqlArray[i]).find());
				i = j;
			}
			list.add(sqlArray[last]);
			LogPadrao.d(sqlArray[last]);
		}
		String out[] = new String[list.size()];
		list.toArray(out);
		return out;
	}

	public static void executeBatch(InputStream assetStream) {
		SQLiteDatabase db = BaseDB.getDb();
		try {
			System.out.println("Iniciando batch:" + new Date());
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					assetStream));
			db.beginTransaction();
			String mLine = reader.readLine();
			while (mLine != null) {
				instancia.execMultipleSQL(db,
						new String[] { mLine.replace(";", "") });
				mLine = reader.readLine();
			}
			System.out.println("Finalizando batch:" + new Date());
			db.setTransactionSuccessful();
			reader.close();
		} catch (IOException e) {
			LogPadrao.e(e);
		} finally {
			db.endTransaction();
		}
	}

}
