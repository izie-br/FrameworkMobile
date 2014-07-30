package com.quantium.mobile.framework.libjdbctest.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;

import com.quantium.mobile.framework.logging.LogPadrao;

public class DB {

	public static String DB_NOME = "default.db";
	public static int DB_VERSAO = 5;

	public static Connection getConnection(){
		Properties props = new Properties();
		props.put("user","sa");
		props.put("password", "");
		try {
			Driver driver = new org.h2.Driver();
			Connection connection = driver.connect("jdbc:h2:mem:", props);
			String script = getSqlScriptPorVersao(DB_VERSAO);
			String statments [] = splitSql(script);
			execMultipleSQL(connection, statments);
			return connection;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getSqlScriptPorVersao(int version ){
		StringBuilder sb = new StringBuilder();

		// Cuidado, o operador de parada deve ser menor igual em "i <= version"
		for (int i=0; i <= version; i++) {
			InputStream is = DB.class.getClassLoader ()
					.getResourceAsStream ("migrations/db_versao_" + i +".sql");
			if (is == null) continue;
			try {
				BufferedReader reader = new BufferedReader (new InputStreamReader (is, "UTF-8"));

				String line = reader.readLine ();
				while (line != null) {
					sb.append (line);
					line = reader.readLine ();
				}
				sb.append ('\n');
			} catch (Exception e) {
				throw new RuntimeException (e);
			}
		}
		return sb.toString();
	}

	public static void execMultipleSQL(Connection db, String[] sql) throws SQLException {
		if (sql == null) {
			return;
		}
		for (int i = 0; i < sql.length; i++) {
			if (sql[i].trim().length() > 0) {
				Statement stm = db.createStatement();
				stm.execute(sql[i]);
			}
		}
	}

	//TODO este metodo nao funciona com string com ponto-e-virgula;
	private static String[] splitSql(String sql) {
		String sqlArray [] = sql.toString().split(";");
		ArrayList<String> list = new ArrayList<String>();
		Pattern triggerPat = Pattern.compile("create\\s+trigger",Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
		Pattern triggerEnd = Pattern.compile("end\\s*$",Pattern.CASE_INSENSITIVE);
		for (int i=0; i<sqlArray.length;i++){
			int last = i;
			if (triggerPat.matcher(sqlArray[i]).find()){
				int j = i;
				do{
					j++;
					sqlArray[i] += ";"+ sqlArray[j];
					sqlArray[j] = null;
				}while (j < (sqlArray.length-1) &&
						!triggerEnd.matcher(sqlArray[i]).find() );
				i = j;
			} else {
				list.add(sqlArray[last].replace("AUTOINCREMENT", "AUTO_INCREMENT"));
				LogPadrao.d(sqlArray[last]);
			}
		}
		String out [] = new String[list.size()];
		list.toArray(out);
		return out;
	}


}
