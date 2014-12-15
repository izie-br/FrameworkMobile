package com.quantium.mobile.framework.libandroidtest.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import static org.junit.Assert.*;

import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QSQLProvider;
import com.quantium.mobile.framework.query.QueryParseException;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.utils.SQLiteUtils;

public class QueryTest {

	@Test
	public void testQString(){
		// dados para teste
		Table t = new Table ("tab");
		t.addColumn(String.class, "col_str");
		t.addColumn(Integer.class, "col_int");
		t.addColumn(String.class, "col_str_two");
		t.addColumn(Date.class, "col_date");

		Table.Column<String> colStr  = t.findColumn(String.class, "col_str");
		Table.Column<Integer> colInt = t.findColumn(Integer.class, "col_int");
		Table.Column<String> colStr2 = t.findColumn(String.class, "col_str_two");
		Table.Column<Date> colDate =   t.findColumn(Date.class, "col_date");

		Q q = colDate.le(new Date()).and( colStr.eq("blah").or(colStr2.lt("blah2")) ).and(colInt.ge(2));
		String selectString = new QSQLProvider(q).select(
				Arrays.asList(new Table.Column<?>[] {colDate, colStr}),
				new ArrayList<Object>());

		String qstrmatch =
			// parentese de abertura, opcional neste caso
			"\\s*\\(?\\s*" +
				// "datetime(caldate) <= ?"
				escapeRegex(SQLiteUtils.dateTimeForColumnForWhere(
						colDate.getTable().getName() + "." + colDate.getName()
				)) + "\\s*\\<=\\s*\\?\\s*" +
				insensitiveRegex("AND") +
				// "( colStr = ? OR colStrTwo < ? )"
				"\\s*\\(\\s*" +
					colStr.getTable().getName() + "\\." +
						colStr.getName() + "\\s*=\\s*\\?" +
					"\\s+" + insensitiveRegex("OR") + "\\s+" +
					colStr2.getTable().getName() + "\\." +
						colStr2.getName() + "\\s*\\<\\s*\\?" +
				"\\s*\\)\\s*" +
			// parentese opcional fechando o primeiro
			"\\)?\\s*" +
			insensitiveRegex("AND") + "\\s+" +
			// "colInt >= ?"
			colInt.getTable().getName() + "\\." +
				colInt.getName() +
				"\\s*\\>=\\s*\\?\\s*";

		String selectRegex =
			"\\s*" + insensitiveRegex("select distinct") + "\\s+" +
				escapeRegex(SQLiteUtils.dateTimeForColumnForSelect(
						colDate.getTable().getName() + "." + colDate.getName()
				)) + "\\s*,\\s*" +
				colStr.getTable().getName() + "\\." +colStr.getName() +
			"\\s+" +insensitiveRegex("from") +"\\s+" + t.getName() +
			"\\s+" + insensitiveRegex("as") + "\\s+" + t.getName() + "\\s+" +
			insensitiveRegex("where") +"\\s+" +
				qstrmatch;

		assertTrue("SELECTSTR:: " + selectString, selectString.matches(
				selectRegex
		));
	}

    @Test
    public void testJoinQstring () {

        Table table1 = new Table ("tab_1");
        Table.Column<Long> colTab1Id = table1.addColumn(Long.class, "id");

        Table table2 = new Table ("though_table");
        Table.Column<Long> colTab2Id = table2.addColumn(Long.class, "id");
        Table.Column<Date> colTab2Date = table2.addColumn(Date.class, "date");

        Q q = colTab1Id.eq(colTab2Id).and(colTab2Date.le(new Date()));
        String select = new QSQLProvider(q).select(
                Arrays.asList(new Table.Column<?> []{colTab1Id, colTab2Id}),
                new ArrayList<Object>()
        );

        String qstringRegex =
                // datetime(though_table.date)<=?
                "\\s*" +
                        escapeRegex(SQLiteUtils.dateTimeForColumnForWhere(
                                colTab2Date.getTable().getName() + "." +
                                        colTab2Date.getName()
                        )) + "\\s*\\<=\\s*\\?\\s*";

        String selectRegex =
                // SELECT
                "\\s*" + insensitiveRegex("select distinct") + "\\s+" +
                        // id,though_table.id
                        //    NOTA: DEVEM estar na mesma ordem
                        colTab1Id.getTable().getName() + "\\." +
                        colTab1Id.getName() + "\\s*,\\s*" +
                        colTab2Id.getTable().getName()+ "\\." +
                        colTab2Id.getName() +
                        // from tab_1 AS tab_1
                        "\\s+" + insensitiveRegex("from") + "\\s+" +
                        colTab1Id.getTable().getName() +
                        "\\s+" + insensitiveRegex("as") + "\\s+" +
                        colTab1Id.getTable().getName() + "\\s+" +
                        // JOIN though_table AS though_table
                        insensitiveRegex("join") + "\\s+" +
                        colTab2Id.getTable().getName() + "\\s+" +
                        insensitiveRegex("as") + "\\s+" +
                        colTab2Id.getTable().getName() + "\\s+" +
                        // ON (id=though_table.id|though_table.id=id)
                        insensitiveRegex("on") + "\\s+(" +
                        colTab1Id.getTable().getName() + "\\." +
                        colTab1Id.getName() + "\\s*=\\s*" +
                        colTab2Id.getTable().getName()+ "\\." +
                        colTab2Id.getName() +
                        "|" +
                        colTab2Id.getTable().getName()+ "\\." +
                        colTab2Id.getName() + "\\s*=\\s*" +
                        colTab1Id.getTable().getName() + "\\." +
                        colTab1Id.getName() +
                        "\\s*)\\s*" +
                        // WHERE
                        "\\s+" + insensitiveRegex("where") + "\\s+" +
                        qstringRegex;

        assertTrue(select.matches(selectRegex));
    }


    @Test
    public void testLeftJoinQstring () {

        Table table1 = new Table ("tab_1");
        Table.Column<Long> colTab1Id = table1.addColumn(Long.class, "id");

        Table table2 = new Table ("though_table");
        Table.Column<Long> colTab2Id = table2.addColumn(Long.class, "id");
        Table.Column<Date> colTab2Date = table2.addColumn(Date.class, "date");

        Q q = colTab1Id.eqlj(colTab2Id).and(colTab2Date.le(new Date()));
        String select = new QSQLProvider(q).select(
                Arrays.asList(new Table.Column<?> []{colTab1Id, colTab2Id}),
                new ArrayList<Object>()
        );

        String qstringRegex =
                // datetime(though_table.date)<=?
                "\\s*" +
                        escapeRegex(SQLiteUtils.dateTimeForColumnForWhere(
                                colTab2Date.getTable().getName() + "." +
                                        colTab2Date.getName()
                        )) + "\\s*\\<=\\s*\\?\\s*";

        String selectRegex =
                // SELECT
                "\\s*" + insensitiveRegex("select distinct") + "\\s+" +
                        // id,though_table.id
                        //    NOTA: DEVEM estar na mesma ordem
                        colTab1Id.getTable().getName() + "\\." +
                        colTab1Id.getName() + "\\s*,\\s*" +
                        colTab2Id.getTable().getName()+ "\\." +
                        colTab2Id.getName() +
                        // from tab_1 AS tab_1
                        "\\s+" + insensitiveRegex("from") + "\\s+" +
                        colTab1Id.getTable().getName() +
                        "\\s+" + insensitiveRegex("as") + "\\s+" +
                        colTab1Id.getTable().getName() + "\\s+" +
                        // JOIN though_table AS though_table
                        insensitiveRegex("left join") + "\\s+" +
                        colTab2Id.getTable().getName() + "\\s+" +
                        insensitiveRegex("as") + "\\s+" +
                        colTab2Id.getTable().getName() + "\\s+" +
                        // ON (id=though_table.id|though_table.id=id)
                        insensitiveRegex("on") + "\\s+(" +
                        colTab1Id.getTable().getName() + "\\." +
                        colTab1Id.getName() + "\\s*=\\s*" +
                        colTab2Id.getTable().getName()+ "\\." +
                        colTab2Id.getName() +
                        "|" +
                        colTab2Id.getTable().getName()+ "\\." +
                        colTab2Id.getName() + "\\s*=\\s*" +
                        colTab1Id.getTable().getName() + "\\." +
                        colTab1Id.getName() +
                        "\\s*)\\s*" +
                        // WHERE
                        "\\s+" + insensitiveRegex("where") + "\\s+" +
                        qstringRegex;

        assertTrue(select.matches(selectRegex));
    }


    @Test
	public void testNullArg () {
		Table t = new Table ("tab");
		Table.Column<Integer> colInt = t.addColumn(Integer.class, "col_int");

		Q q = colInt.eq((Integer)null);
		String select = new QSQLProvider(q).select(
				Arrays.asList(new Table.Column []{colInt}),
				new ArrayList<Object>());
		assertTrue(
			select.matches(
				".*"+colInt.getName()+ "\\s+" +
				insensitiveRegex("is") + "\\s*" + insensitiveRegex("null")
			)
		);

		q = colInt.ne((Integer)null);
		select = new QSQLProvider(q).select(
				Arrays.asList(new Table.Column []{colInt}),
				new ArrayList<Object>());
		assertTrue(select.matches(".*" + colInt.getName() + "\\s+" + insensitiveRegex("is") + "\\s*"
				+ insensitiveRegex("not") + "\\s*" + insensitiveRegex("null")
			)
		);

		q = colInt.lt((Integer)null);
		try {
			new QSQLProvider(q).select(
					Arrays.asList(new Table.Column []{colInt}),
					new ArrayList<Object>());
			fail ("A query " +select + " eh absurda");
		} catch (QueryParseException e) {
			/* Aqui deve acontencer esta excecao mesmo. */
			/* A condicao "menor que" NULL eh absurda */
		}
	}

	@Test
	public void testInOperator(){
		Table table1 = new Table ("tab_1");
		Table.Column<Long> colTab1Id = table1.addColumn(Long.class, "id");

		Q q = colTab1Id.in(1L,2L,4L);
		String qstring = new QSQLProvider(q).select(
				Arrays.asList(new Table.Column<?>[]{colTab1Id}),
				new ArrayList<Object>());
		String qstrregex = ".*WHERE\\s+"+
			"\\(?" +
				colTab1Id.getTable().getName() + "\\." +
					colTab1Id.getName() +
				".*\\s+IN\\s*\\(" +
					"\\s*\\?\\s*" + "\\," +
					"\\s*\\?\\s*" + "\\," +
					"\\s*\\?\\s*" +
				"\\)" +
			"\\)?.*";
		com.quantium.mobile.framework.logging.LogPadrao.d("qstring:: '%s'", qstring);
		Pattern pat = Pattern.compile(qstrregex, Pattern.CASE_INSENSITIVE);
		Matcher mobj = pat.matcher(qstring);
		assertTrue(mobj.find());

	}

	@Test
	public void testImmutable(){
		Table table1 = new Table ("tab_1");
		Table.Column<Long> colTab1Id = table1.addColumn(Long.class, "id");

		final Q q1 = colTab1Id.lt(100L);
		final String q1Str = new QSQLProvider(q1).select(
				Arrays.asList(new Table.Column<?>[]{colTab1Id}),
				new ArrayList<Object>());
		{
			Q q2 = q1.and(colTab1Id.gt(10L));
			String q1StrAfterOperation = new QSQLProvider(q1).select(
					Arrays.asList(new Table.Column<?>[]{colTab1Id}),
					new ArrayList<Object>());
			assertEquals(q1Str, q1StrAfterOperation);
			String q2Str = new QSQLProvider(q2).select(
					Arrays.asList(new Table.Column<?>[]{colTab1Id}),
					new ArrayList<Object>());
			assertFalse( q2Str.equals(q1Str));
		}

		{
			Q q3 = Q.not(q1);
			String q1StrAfterNotOperation = new QSQLProvider(q1).select(
					Arrays.asList(new Table.Column<?>[]{colTab1Id}),
					new ArrayList<Object>());
			assertEquals(q1Str, q1StrAfterNotOperation);
			String q3Str = new QSQLProvider(q3).select(
					Arrays.asList(new Table.Column<?>[]{colTab1Id}),
					new ArrayList<Object>());
			assertFalse("String::"+q3Str, q3Str.equals(q1Str));
		}

		{
			Q q4 = q1.or(colTab1Id.eq(11L));
			String q1StrAfterOROperation = new QSQLProvider(q1).select(
					Arrays.asList(new Table.Column<?>[]{colTab1Id}),
					new ArrayList<Object>());
			assertEquals(q1Str, q1StrAfterOROperation);
			String q4Str = new QSQLProvider(q4).select(
					Arrays.asList(new Table.Column<?>[]{colTab1Id}),
					new ArrayList<Object>());
			assertFalse( q4Str.equals(q1Str));
		}
	}

	public String insensitiveRegex(String str) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < str.length(); i++ ) {
			sb.append('[');
			sb.append(Character.toUpperCase(str.charAt(i)));
			sb.append(Character.toLowerCase(str.charAt(i)));
			sb.append(']');
		}
		return sb.toString();
	}

	private String escapeRegex(String str) {
		return str
//			.replaceAll(Pattern.quote("("), "\\(\\s*")
//			.replaceAll(Pattern.quote(")"), "\\s*\\)")
//			.replaceAll(Pattern.quote("."), "\\.");
			.replace("(", "\\(\\s*")
			.replace(")", "\\s*\\)")
			.replace(".", "\\.");
	}

}
