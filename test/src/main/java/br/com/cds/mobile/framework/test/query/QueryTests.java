package br.com.cds.mobile.framework.test.query;

import java.util.Date;

import android.test.ActivityInstrumentationTestCase2;
import br.com.cds.mobile.framework.query.Q;
import br.com.cds.mobile.framework.query.Table;
import br.com.cds.mobile.framework.test.TestActivity;

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
		Q q = colDate.le(new Date()).and( new Q( colStr.eq("blah").and(colStr2.le("blah2")) ) ).and(colInt.eq(2));
		String qstring = q.getQstring();
		String selectString = q.getSelectStm(colStr);
		String qstrmatch = "\\s*" + colStr.getName() + "\\s*=\\s*\\?\\s*";
		assertTrue(qstring.matches(
				qstrmatch
		));
		assertTrue(selectString.matches(
				"select\\s+" +
				colStr.getName() + "\\s*,\\s*" + colInt.getName() +
				"\\s*,\\s*count\\(\\*\\)" +
				"\\s+[fF][rR][oO][mM]\\s+" + t.getName() +
				"\\s+[wW][hH][eE][rR][eE]\\s+" +
				qstrmatch
		));
	}

}
