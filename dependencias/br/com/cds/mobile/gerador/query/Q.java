package br.com.cds.mobile.gerador.query;

public class Q {

	public static byte NOT = 1;
	public static byte IGUAL_A = 2;
	public static byte MENOR_QUE = 3;
	public static byte MAIOR_QUE = 4;
	public static byte MENOR_OU_IGUAL_A = 5;
	public static byte MAIOR_OU_IGUAL_A = 6;

	public static byte LIKE = 7;
	public static byte ESCAPE = 8;
	public static byte BETWEEN = 9;
	public static byte IN = 10;

	/*
SQL avg()
SQL count()
SQL first()
SQL last()
SQL max()
SQL min()
SQL sum()
SQL Group By
SQL Having
SQL ucase()
SQL lcase()
SQL mid()
SQL len()
SQL round()
SQL now()
SQL format()
	 */
	public static byte COUNT = 14;
	public static byte AVG = 15;
	public static byte SUM = 16;
	public static byte MIN = 17;
	public static byte MAX = 18;
	public static byte FIRST = 19;
	public static byte LAST = 20;
	public static byte ABS = 21;
	public static byte ROUND = 22;

	private Object data;

	public Q(String coluna, byte operador, Object... args){
		
	}

	public static Q select(String tabela,Q inner){
		return new Q(null,Q.NOT,null);
	}

	private String operadorToString(byte op){
		return
				op == NOT              ?  "!"    :
				op == IGUAL_A          ?  "="    :
				op == MENOR_QUE        ?  "<"    :
				op == MAIOR_QUE        ?  ">"    :
				op == MENOR_OU_IGUAL_A ?  "<="   :
				op == MAIOR_OU_IGUAL_A ?  ">="   :
				op == LIKE             ?  "LIKE" :
				op == ESCAPE           ? "ESCAPE" :
				op == BETWEEN          ? "BETWEEN" :
				op == IN               ? "IN"    :
				op == COUNT            ? "COUNT" :
				op == AVG              ? "AVG"   :
				op == SUM              ? "SUM"   :
				op == MIN              ? "MIN"   :
				op == MAX              ? "MAX"   :
				op == FIRST            ? "FIRST" :
				op == ABS              ? "ABS"   :
				op == ROUND            ? "ROUND" :
				null;
	}

}
