package br.com.cds.mobile.geradores.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public class SQLiteGeradorUtils {

	private static final String ERRO_DATA_TYPE_SEM_CLASSE_JAVA_FORMAT =
		"DataType \"%s\" sem uma classe java correspodente.\n ---FIXME---@%s::%s";
	private static final String ERRO_METODO_PARA_CLASSE_JAVA_FORMAT =
		"Classe \"%s\" sem um metodo do cursor correspodente.\n ---FIXME---@%s::%s";


	public static Class<?> classeJavaEquivalenteAoTipoSql(String sqlType){
		/********************
		 * INT              *
		 * INTEGER          *
		 * TINYINT          *
		 * SMALLINT         *
		 * MEDIUMINT        *
		 * INT2             *
		 * INT8             *
		 *******************/
		if(
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

		if(
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
		if(
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
		if(
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
		if(
				sqlType.equalsIgnoreCase("numeric") ||
				sqlType.matches("[\\w]*[d][D][e][E][c][C][i][I][m][M][a][A][l][L].*")
		)
			return BigDecimal.class;
		/************
		 * DATE     *
		 * DATETIME *
		 ***********/
		if(
				sqlType.equalsIgnoreCase("date") ||
				sqlType.equalsIgnoreCase("datetime")
		)
			return Date.class;
		/***********
		 * BOOLEAN *
		 **********/
		if(sqlType.equalsIgnoreCase("boolean"))
			return Boolean.class;
		/**********
		 * BLOB   *
		 * outros *
		 *********/
		throw new RuntimeException(String.format(
				ERRO_DATA_TYPE_SEM_CLASSE_JAVA_FORMAT,
				sqlType,
				SQLiteGeradorUtils.class.getName(),
				new Object(){}.getClass().getEnclosingMethod().getName()
		));
	}

	public static String cursorGetColunaParaClasse(Class<?> klass){
		if(klass.equals(Long.class))
			return "getLong";
		if(klass.equals(String.class))
			return "getString";
		if(klass.equals(Double.class))
			return "getDouble";
		if(klass.equals(Boolean.class))
			return "getShort";
		if(klass.equals(Date.class))
			return "getString";
		throw new RuntimeException(String.format(
				ERRO_METODO_PARA_CLASSE_JAVA_FORMAT,
				klass.getSimpleName(),
				SQLiteGeradorUtils.class.getName(),
				new Object(){}.getClass().getEnclosingMethod().getName()
		));
	}


}
