package com.quantium.mobile.framework.test.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import android.test.ActivityInstrumentationTestCase2;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QueryParseException;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.test.TestActivity;
import com.quantium.mobile.framework.utils.SQLiteUtils;

public class QueryTests  extends ActivityInstrumentationTestCase2<TestActivity> {

	public QueryTests() {
		super("com.quantium.mobile.framework.test", TestActivity.class);
	}
	
	public void testQString(){
		// dados para teste
		Table t = new Table("tab");
		Table.Column<String> colStr  = t.addColumn(String.class, "col_str");
		Table.Column<Integer> colInt = t.addColumn(Integer.class, "col_int");
		Table.Column<String> colStr2 = t.addColumn(String.class, "col_str_two");
		Table.Column<Date> colDate = t.addColumn(Date.class, "col_date");

		Q q = colDate.le(new Date()).and( colStr.eq("blah").or(colStr2.lt("blah2")) ).and(colInt.ge(2));
		String selectString = q.select(new Table.Column<?>[] {colDate, colStr},new ArrayList<String>());

		String qstrmatch =
			// parentese de abertura, opcional neste caso
			"\\s*\\(?\\s*" +
				// "datetime(caldate) <= ?"
				escapeRegex(SQLiteUtils.dateTimeForColumn(
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
			"\\s*" + insensitiveRegex("select") + "\\s+" +
				escapeRegex(SQLiteUtils.dateTimeForColumn(
						colDate.getTable().getName() + "." + colDate.getName()
				)) + "\\s*,\\s*" +
				colStr.getTable().getName() + "\\." +colStr.getName() +
			"\\s+" +insensitiveRegex("from") +"\\s+" + t.getName() +
			"\\s+" + insensitiveRegex("as") + "\\s+" + t.getName() + "\\s+" +
			insensitiveRegex("where") +"\\s+" +
				qstrmatch;

		assertTrue(selectString.matches(
				selectRegex
		));
	}

	public void testJoinQstring () {

		Table table1 = new Table("tab_1");
		Table.Column<Long> colTab1Id = table1.addColumn(Long.class, "id");
		Table table2 = new Table("though_table");
		Table.Column<Long> colTab2Id = table2.addColumn(Long.class, "id");
		Table.Column<Date> colTab2Date = table2.addColumn(Date.class, "date");

		Q q = colTab1Id.eq(colTab2Id).and(colTab2Date.le(new Date()));
		String select = q.select(
				new Table.Column<?> []{colTab1Id, colTab2Id},
				new ArrayList<String>()
		);

		String qstringRegex =
			// datetime(though_table.date)<=?
			"\\s*" +
			escapeRegex(SQLiteUtils.dateTimeForColumn(
					colTab2Date.getTable().getName() + "." +
					colTab2Date.getName()
			)) + "\\s*\\<=\\s*\\?\\s*";

		String selectRegex =
			// SELECT
			"\\s*" + insensitiveRegex("select") + "\\s+" +
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

	public void testNullArg () {
		Table t = new Table("tab");
		Table.Column<Integer> colInt = t.addColumn(Integer.class, "col_int");

		Q q = colInt.eq((Integer)null);
		String select = q.select(new Table.Column []{colInt}, new ArrayList<String>());
		assertTrue(
			select.matches(
				".*"+colInt.getName()+ "\\s+" +
				insensitiveRegex("is") + "\\s*" + insensitiveRegex("null")
			)
		);

		q = colInt.ne((Integer)null);
		select = q.select(new Table.Column []{colInt}, new ArrayList<String>());
		assertTrue(
			select.matches(
				".*"+colInt.getName()+ "\\s+" +
				insensitiveRegex("not") + "\\s*" + insensitiveRegex("null")
			)
		);

		q = colInt.lt((Integer)null);
		try {
			q.select(new Table.Column []{colInt}, new ArrayList<String>());
			fail ("A query " +select + " eh absurda");
		} catch (QueryParseException e) {
			/* Aqui deve acontencer esta excecao mesmo. */
			/* A condicao "menor que" NULL eh absurda */
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