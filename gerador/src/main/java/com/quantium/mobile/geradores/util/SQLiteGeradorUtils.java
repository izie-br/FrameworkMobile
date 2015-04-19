package com.quantium.mobile.geradores.util;

import java.io.File;
import java.math.BigInteger;
import java.sql.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLiteGeradorUtils {

    public static final String FUNCAO_DATE = "datetime";

    private static final String ERRO_DATA_TYPE_SEM_CLASSE_JAVA_FORMAT =
            "DataType \"%s\" sem uma classe java correspodente.\n ---FIXME---@%s::%s";
    private static final String ERRO_METODO_PARA_CLASSE_JAVA_FORMAT =
            "Classe \"%s\" sem um metodo do cursor correspodente.\n ---FIXME---@%s::%s";


    private static final String DB_FILE_FMT = ".sample%s.db";

    /**
     * Escreve o sql em um arquivo sqlite temporario, retira um DUMP, ja com
     * todos os ALTER e DROP aplicados.
     * <p/>
     * IMPORTANTE: uma tabela sqlite_sequence vai ser criada, se houver algum
     * coluna PRIMARYKEY AUTOINCREMENT
     *
     * @param sql script inicial
     * @return script DUMP resultante
     */
    public static String getSchema(String sql) throws SQLException {
        StringBuilder sb = new StringBuilder();
        File dbFile = new File(String.format(DB_FILE_FMT, ""));
        int i = 0;

        while (dbFile.exists()) {
            LoggerUtil.getLog().error(dbFile.getAbsolutePath() + " ja existe");
            i++;
            dbFile = new File(String.format(DB_FILE_FMT, "_" + i));
        }

        try {
            // jdbc pode precisar disso
            @SuppressWarnings("unused")
            Class<?> klass = Class.forName("org.sqlite.JDBC");

            Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + dbFile.getAbsolutePath()
            );
            // Statement statement = connection.createStatement();
            // statement.setQueryTimeout(30);
            Pattern pat = Pattern.compile("CREATE\\s+TRIGGER\\s+.*?END\\s*;",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            sql = sql.replace("\\'", "'");
            sql = sql.replace("\\\"", "\"");
            Matcher matcher = pat.matcher(sql);
            StringBuffer sqlsb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sqlsb, "");
            }
            matcher.appendTail(sqlsb);
            sql = sqlsb.toString();
            String[] stms = sql.split(";");
            for (String stm : stms) {
                if (stm.matches("[\\s\\n]*"))
                    break;
                PreparedStatement stmt = connection.prepareStatement(stm + ";");
                stmt.executeUpdate();
            }
            ResultSet rs = connection.createStatement().executeQuery(
                    "select sql from sqlite_master;");
            while (rs.next()) {
                String sqlTab = rs.getString(rs.findColumn("sql"));
                if (sqlTab != null)
                    sb.append(sqlTab).append(";");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            dbFile.delete();
        }
        return sb.toString();
    }


    public static Class<?> classeJavaEquivalenteAoTipoSql(String sqlType) {
        /********************
         * INT              *
         * INTEGER          *
         * TINYINT          *
         * SMALLINT         *
         * MEDIUMINT        *
         * INT2             *
         * INT8             *
         *******************/
        if (
                sqlType.equalsIgnoreCase("int") ||
                        sqlType.equalsIgnoreCase("integer") ||
                        sqlType.equalsIgnoreCase("tinyint") ||
                        sqlType.equalsIgnoreCase("smallint") ||
                        sqlType.equalsIgnoreCase("mediumint") ||
                        sqlType.equalsIgnoreCase("int2") ||
                        sqlType.equalsIgnoreCase("int8")
                )
            return Long.class;
        /********************
         * BIGINT           *
         * UNSIGNED BIG INT *
         *******************/

        if (
                sqlType.equalsIgnoreCase("bigint") ||
                        sqlType.equalsIgnoreCase("unsiged big int")
                )
            return BigInteger.class;
        /**************************
         * CHARACTER(20)          *
         * VARCHAR(255)           *
         * VARYING CHARACTER(255) *
         * NCHAR(55)              *
         * NATIVE CHARACTER(70)   *
         * NVARCHAR(100)          *
         * TEXT                   *
         * CLOB                   *
         *************************/
        if (
                sqlType.equalsIgnoreCase("text") ||
                        sqlType.equalsIgnoreCase("clob") ||
                        sqlType.matches("[\\s\\w]*[cC][hH][aA][rR].*")
                )
            return String.class;
        /********************
         * REAL             *
         * DOUBLE           *
         * DOUBLE PRECISION *
         * FLOAT            *
         *******************/
        if (
                sqlType.equalsIgnoreCase("real") ||
                        sqlType.equalsIgnoreCase("double") ||
                        sqlType.equalsIgnoreCase("double precision") ||
                        sqlType.equalsIgnoreCase("float")
                )
            return Double.class;
        /*****************
         * NUMERIC       *
         * DECIMAL(10,5) *
         ****************/
//		if(
//				sqlType.equalsIgnoreCase("numeric") ||
//				sqlType.equalsIgnoreCase("decimal") ||
//				sqlType.matches("[\\w]*[d][D][e][E][c][C][i][I][m][M][a][A][l][L].*")
//		)
//			return BigDecimal.class;
        /************
         * DATE     *
         * DATETIME *
         ***********/
        if (
                sqlType.equalsIgnoreCase("date") ||
                        sqlType.equalsIgnoreCase("datetime") ||
                        sqlType.equalsIgnoreCase("timestamp")
                )
            return Date.class;
        /***********
         * BOOLEAN *
         **********/
        if (sqlType.equalsIgnoreCase("boolean"))
            return Boolean.class;
        /**********
         * BLOB   *
         * outros *
         *********/
        throw new RuntimeException(String.format(
                ERRO_DATA_TYPE_SEM_CLASSE_JAVA_FORMAT,
                sqlType,
                SQLiteGeradorUtils.class.getName(),
                new Object() {
                }.getClass().getEnclosingMethod().getName()
        ));
    }

    public static String getSqlTypeFromClass(Class<?> klass) {
        if (klass.equals(Long.class))
            return "INTEGER";
        if (klass.equals(Date.class))
            return "TIMESTAMP";
        if (klass.equals(String.class))
            return "TEXT";
        if (klass.equals(Double.class))
            return "DOUBLE";
        if (klass.equals(Boolean.class))
            return "BOOLEAN";
        throw new RuntimeException(String.format(
                "Classe %s nao mapeada em %s.%s",
                klass.getName(),
                SQLiteGeradorUtils.class.getName(),
                new Object() {
                }.getClass().getEnclosingMethod().getName()
        ));
    }

    public static String metodoGetDoCursorParaClasse(Class<?> klass) {
        if (klass.equals(Long.class))
            return "getLong";
        if (klass.equals(String.class))
            return "getString";
        if (klass.equals(Double.class))
            return "getDouble";
        if (klass.equals(Boolean.class))
            return "getShort";
        if (klass.equals(Date.class))
            return "getString";
        throw new RuntimeException(String.format(
                ERRO_METODO_PARA_CLASSE_JAVA_FORMAT,
                klass.getSimpleName(),
                SQLiteGeradorUtils.class.getName(),
                new Object() {
                }.getClass().getEnclosingMethod().getName()
        ));
    }

    public static String metodoGetDoJsonParaClasse(Class<?> klass) {
        if (klass.equals(Long.class))
            return "getLong";
        if (klass.equals(String.class))
            return "getString";
        if (klass.equals(Double.class))
            return "getDouble";
        if (klass.equals(Boolean.class))
            return "getBoolean";
        if (klass.equals(Date.class))
            return "getString";
        throw new RuntimeException(String.format(
                ERRO_METODO_PARA_CLASSE_JAVA_FORMAT,
                klass.getSimpleName(),
                SQLiteGeradorUtils.class.getName(),
                new Object() {
                }.getClass().getEnclosingMethod().getName()
        ));
    }

    public static String metodoOptDoJsonParaClasse(Class<?> klass) {
        return "opt" + metodoGetDoJsonParaClasse(klass).substring(3);
    }

}
