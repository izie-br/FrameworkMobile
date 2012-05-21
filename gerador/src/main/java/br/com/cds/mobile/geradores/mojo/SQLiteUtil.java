package br.com.cds.mobile.geradores.mojo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteUtil {

    public static String getSchema(String sql) throws SQLException{
        try {
            @SuppressWarnings("unused")
            Class<?> klass = Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Connection connection = DriverManager.getConnection("jdbc:sqlite:__sample.db");
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
        return sb.toString();
    }

}
