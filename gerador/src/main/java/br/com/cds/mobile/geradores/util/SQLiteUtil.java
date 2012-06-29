package br.com.cds.mobile.geradores.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLiteUtil {

    private static final String DB_FILE = "__sample.db";

	public static String getSchema(String sql) throws SQLException{
        try {
            @SuppressWarnings("unused")
            Class<?> klass = Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+DB_FILE);
//        Statement statement = connection.createStatement();
//        statement.setQueryTimeout(30);
        String[] stms = sql.split(";");
        for(String stm : stms){
            if (stm.matches("[\\s\\n]*"))
                break;
            connection.createStatement().executeUpdate(stm+";");
        }
        ResultSet rs = connection.createStatement().executeQuery("select sql from sqlite_master;");
        StringBuilder sb = new StringBuilder();
        while(rs.next()) {
            String sqlTab = rs.getString(rs.findColumn("sql"));
            if(sqlTab!=null)
                sb.append(sqlTab).append(";");
        }
        new File(DB_FILE).delete();
        return sb.toString();
    }

}
