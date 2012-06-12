package br.com.cds.mobile.framework.test.query;

import java.util.Date;

import android.test.ActivityInstrumentationTestCase2;
import br.com.cds.mobile.framework.query.Q;
import br.com.cds.mobile.framework.query.Table;
import br.com.cds.mobile.framework.test.TestActivity;
import br.com.cds.mobile.framework.utils.SQLiteUtils;

public class QueryTests  extends ActivityInstrumentationTestCase2<TestActivity> {

	public QueryTests() {
		super("br.com.cds.mobile.framework.test", TestActivity.class);
	}
	
	public void testQString(){
		Table t = new Table("tab");
		Table.Column<String> colStr  = t.addColumn(String.class, "col_str");
		Table.Column<Integer> colInt = t.addColumn(Integer.class, "col_int");
		Table.Column<String> colStr2 = t.addColumn(String.class, "col_str_two");
		Table.Column<Date> colDate = t.addColumn(Date.class, "col_date");
		Q q = colDate.le(new Date()).and( colStr.eq("blah").or(colStr2.lt("blah2")) ).and(colInt.ge(2));
		String qstring = q.getQString();
		String selectString = q.select(colDate, colStr);
		String qstrmatch =
			// parentese de abertura, opcional neste caso
			"\\s*\\(?\\s*" +
				// "datetime(caldate) <= ?"
				SQLiteUtils.dateTimeForColumn(colDate.getName())
					// nao esquecer os escapes dos parenteses
					.replace("(", "\\(\\s*").replace(")", "\\s*\\)") +
				"\\s*\\<=\\s*\\?\\s*" +
				insensitiveRegex("AND") +
				// "( colStr = ? OR colStrTwo < ? )"
				"\\s*\\(\\s*" +
					colStr.getName() + "\\s*=\\s*\\?" +
					"\\s+" + insensitiveRegex("OR") + "\\s+" +
					colStr2.getName() + "\\s*\\<\\s*\\?" +
				"\\s*\\)\\s*" +
			// parentese opcional fechando o primeiro
			"\\)?\\s*" +
			insensitiveRegex("AND") + "\\s+" +
			// "colInt >= ?"
			colInt.getName() +"\\s*\\>=\\s*\\?\\s*";
		assertTrue(qstring.matches(
				qstrmatch
		));
		
		String selectRegex =
			"\\s*" + insensitiveRegex("select") + "\\s+" +
				SQLiteUtils.dateTimeForColumn(colDate.getName())
					// nao esquecer os escapes dos parenteses
					.replace("(", "\\(\\s*").replace(")", "\\s*\\)") +
				"\\s*,\\s*" +
				colStr.getName() +
			"\\s+" +insensitiveRegex("from") +"\\s+" + t.getName() +
			"\\s+"+insensitiveRegex("where") +"\\s+" +
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
		String qstring = q.getQString();
		String qstringRegex =
			// datetime(though_table.date)<=?
			"\\s*" +
			SQLiteUtils.dateTimeForColumn(
					colTab2Date.getTable().getName() + "\\." +
						colTab2Date.getName()
				).replace("(", "\\(\\s*").replace(")", "\\s*\\)") +
			"\\s*\\<=\\s*\\?\\s*";
		assertTrue(qstring.matches(qstringRegex));
//"  FROM tab_1 JOIN though_table AS though_table ON t1.id=though_table.id
		String select = q.select(colTab1Id, colTab2Id);
		String selectRegex =
			// SELECT
			"\\s*" + insensitiveRegex("select") + "\\s+" +
			// id,though_table.id
			//    NOTA: DEVEM estar na mesma ordem
			colTab1Id.getName() + "\\s*,\\s*" +
			colTab2Id.getTable().getName()+ "\\." +
				colTab2Id.getName() +
			// from tab_1
			"\\s+" + insensitiveRegex("from") + "\\s+" +
			colTab1Id.getTable().getName() + "\\s+" +
			// JOIN though_table AS though_table
			insensitiveRegex("join") + "\\s+" +
			colTab2Id.getTable().getName() + "\\s+" +
			insensitiveRegex("as") + "\\s+" +
			colTab2Id.getTable().getName() + "\\s+" +
			// ON (id=though_table.id|though_table.id=id)
			insensitiveRegex("on") + "\\s+(" +
				colTab1Id.getName() + "\\s*=\\s*" +
				colTab2Id.getTable().getName()+ "\\." +
				colTab2Id.getName() +
			"|" +
				colTab2Id.getTable().getName()+ "\\." +
				colTab2Id.getName() + "\\s*=\\s*" +
				colTab1Id.getName() +
			"\\s*)\\s*" +
			// WHERE
			"\\s+" + insensitiveRegex("where") + "\\s+" +
			qstringRegex;
		assertTrue(select.matches(selectRegex));
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

}
