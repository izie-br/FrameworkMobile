package com.quantium.mobile.framework.libjdbctest.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.shared.util.XMLUtil;

public class DB {

	public static String DB_NOME = "default.db";
	public static int DB_VERSAO = 3;

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
		InputStream is = DB.class.getClassLoader()
		                 .getResourceAsStream("sql.xml");
		List<String> nodes = XMLUtil.xpath(is, "//string["
				+ "contains(@name,\"db_versao_\") and "
				+ "number(substring(@name,11)) < " + (++version) + "]//text()");
		StringBuilder sb = new StringBuilder();
		for (String node : nodes)
			sb.append(node);
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
		Pattern triggerEnd = Pattern.compile("\\send\\s*$",Pattern.CASE_INSENSITIVE);
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
