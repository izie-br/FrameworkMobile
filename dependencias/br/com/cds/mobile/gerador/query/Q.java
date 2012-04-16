package br.com.cds.mobile.gerador.query;

import java.util.ArrayList;

public final class Q {

	public static final Op1x1 DIFERENTE_DE = Op1x1.DIFERENTE_DE;
	public static final Op1x1 IGUAL_A = Op1x1.IGUAL_A;
	public static final Op1x1 MENOR_QUE = Op1x1.MENOR_QUE;
	public static final Op1x1 MAIOR_QUE = Op1x1.MAIOR_QUE;
	public static final Op1x1 MENOR_OU_IGUAL_A = Op1x1.MENOR_OU_IGUAL_A;
	public static final Op1x1 MAIOR_OU_IGUAL_A = Op1x1.MAIOR_OU_IGUAL_A;
	public static final Op1x1 LIKE = Op1x1.LIKE;

	public static final Op1x1 IN = Op1x1.IN;

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
	 
	public static byte COUNT = 14;
	public static byte AVG = 15;
	public static byte SUM = 16;
	public static byte MIN = 17;
	public static byte MAX = 18;
	public static byte FIRST = 19;
	public static byte LAST = 20;
	public static byte ABS = 21;
	public static byte ROUND = 22;
*/

	private static final byte TIPO_SIMPLES_1X1 = 1;
	private static final byte TIPO_COMPOSTA_NOT = 3;
	private static final byte TIPO_COMPOSTA_AND = 4;
	private static final byte TIPO_COMPOSTA_OR = 5;

	private byte tipo;
	private Q[] subQs;
	private String[] valores;
	private Object[] argumentos;

	public Q(String coluna, Op1x1 operador, Object argumento){
		tipo = TIPO_SIMPLES_1X1;
		valores = new String[]{ coluna, operador.toString(), "?" };
		argumentos = new Object[]{argumento};
	}


	public Q(Q... subQs){
		tipo = TIPO_COMPOSTA_AND;
		this.subQs = subQs;
	}

	public static Q not(Q... subQs){
		Q q = new Q(subQs);
		q.tipo = TIPO_COMPOSTA_NOT;
		return q;
	}

	public Q ou(Q... subQs){
		Q q = new Q(subQs);
		q.tipo = TIPO_COMPOSTA_OR;
		return q;
	}


	private void listaDeArgumentos(ArrayList<Object> argumentList){
		if(tipo==TIPO_SIMPLES_1X1){
			for(Object obj : argumentos)
				argumentList.add(obj);
			return;
		}
		for(Q subQ : subQs)
			subQ.listaDeArgumentos(argumentList);
	}

	public Object[] getArgumentos(){
		ArrayList<Object> argumentList = new ArrayList<Object>();
		listaDeArgumentos(argumentList);
		return argumentList.toArray();
	}

	private void escreverEsta(StringBuilder out){
		switch (this.tipo) {
		case TIPO_COMPOSTA_NOT:
			out.append(" NOT ");
			// fall through
		case TIPO_COMPOSTA_OR :
			for(String val : valores)
				out.append(val);
		case TIPO_COMPOSTA_AND:
			out.append('(');
			String conexao =
				(tipo == TIPO_COMPOSTA_OR) ?
					" OR " :
					" AND ";
			subQs[0].escreverEsta(out);
			for(int i = 1; i<subQs.length;i++){
				out.append(conexao);
				subQs[i].escreverEsta(out);
			}
			out.append(") ");
			if(tipo!=TIPO_COMPOSTA_OR)
			break;
		case TIPO_SIMPLES_1X1:
			for(String val : valores)
				out.append(val);
		}
	}

	/**
	 * retorna a String "where" desta e todas subqueries
	 */
	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		escreverEsta(sb);
		return sb.toString();
	}


	/********************************
	 * Tipos de operadores e funcoes
	 */

	public static enum OpQxQ {
		AND, OU;
		public String toString() {
			return this == AND ? "AND" : "OR" ;
		};
	}

	public static enum Op1x1 {

		DIFERENTE_DE,IGUAL_A,MENOR_QUE,MAIOR_QUE,MENOR_OU_IGUAL_A,
		MAIOR_OU_IGUAL_A,LIKE,IN;

		public String toString() {
			return
				this == DIFERENTE_DE     ?  "<>"     :
				this == IGUAL_A          ?  "="      :
				this == MENOR_QUE        ?  "<"      :
				this == MAIOR_QUE        ?  ">"      :
				this == MENOR_OU_IGUAL_A ?  "<="     :
				this == MAIOR_OU_IGUAL_A ?  ">="     :
				this == LIKE             ?  " LIKE " :
				/*this == IN             ?*/  " IN " ;
		}
	}


}
