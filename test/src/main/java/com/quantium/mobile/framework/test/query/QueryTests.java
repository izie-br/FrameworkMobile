package com.quantium.mobile.framework.test.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.test.ActivityInstrumentationTestCase2;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QSQLProvider;
import com.quantium.mobile.framework.query.QueryParseException;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.query.SQLiteQuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.test.SessionFacade;
import com.quantium.mobile.framework.test.TestActivity;
import com.quantium.mobile.framework.utils.SQLiteUtils;
import com.quantium.mobile.framework.utils.StringUtil;
import com.quantium.mobile.framework.test.gen.Author;

public class QueryTests  extends ActivityInstrumentationTestCase2<TestActivity> {

	SessionFacade facade = new SessionFacade();
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
		String selectString = new QSQLProvider(q).select(new Table.Column<?>[] {colDate, colStr},new ArrayList<String>());

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

		assertTrue("SELECTSTR:: " + selectString, selectString.matches(
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
		String select = new QSQLProvider(q).select(
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
		String select = new QSQLProvider(q).select(new Table.Column []{colInt}, new ArrayList<String>());
		assertTrue(
			select.matches(
				".*"+colInt.getName()+ "\\s+" +
				insensitiveRegex("is") + "\\s*" + insensitiveRegex("null")
			)
		);

		q = colInt.ne((Integer)null);
		select = new QSQLProvider(q).select(new Table.Column []{colInt}, new ArrayList<String>());
		assertTrue(
			select.matches(
				".*"+colInt.getName()+ "\\s+" +
				insensitiveRegex("not") + "\\s*" + insensitiveRegex("null")
			)
		);

		q = colInt.lt((Integer)null);
		try {
			new QSQLProvider(q).select(new Table.Column []{colInt}, new ArrayList<String>());
			fail ("A query " +select + " eh absurda");
		} catch (QueryParseException e) {
			/* Aqui deve acontencer esta excecao mesmo. */
			/* A condicao "menor que" NULL eh absurda */
		}
	}

	public void testLikeAndGlob(){
		DAO<Author> dao = facade.getDAOFactory().getDaoFor(Author.class);
		Author author1 = new Author();
		author1.setName("um nome");
		Date now = new Date();
		now = new Date(now.getYear(), now.getMonth(), now.getDate(),
				now.getHours(), now.getMinutes());
		author1.setCreatedAt(now);
		Author author2 = new Author();
		author2.setName("outro nome");
		author2.setCreatedAt(now);
		Author author3 = new Author();
		author3.setName("outro");
		author3.setCreatedAt(now);
		try {
			assertTrue(dao.save(author1));
			assertTrue(dao.save(author2));
			assertTrue(dao.save(author3));
		} catch (IOException e) {
			fail(StringUtil.getStackTrace(e));
		}
		// buscas com LIKE
		Collection<Author> authors = dao.query(
				Q.like(Author.NAME,"%no_e"))
			.all();
		assertNotNull(authors);
		assertEquals(2, authors.size());
		assertTrue(authors.contains(author1));
		assertTrue(authors.contains(author2));
		// buscas com GLOB
		authors = dao.query(
				Q.glob(Author.NAME,"*[nm]?[mw]e"))
			.all();
		assertNotNull(authors);
		assertEquals(2, authors.size());
		assertTrue(authors.contains(author1));
		assertTrue(authors.contains(author2));
	}

	public void testInOperator(){
		Table table1 = new Table("tab_1");
		Table.Column<Long> colTab1Id = table1.addColumn(Long.class, "id");

		Q q = colTab1Id.in(1L,2L,4L);
		String qstring = new QSQLProvider(q).select(
				new Table.Column<?>[]{colTab1Id},
				new ArrayList<String>());
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

	public void testImmutable(){
		Table table1 = new Table("tab_1");
		Table.Column<Long> colTab1Id = table1.addColumn(Long.class, "id");

		final Q q1 = colTab1Id.lt(100L);
		final String q1Str = new QSQLProvider(q1).select(
				new Table.Column<?>[]{colTab1Id},
				new ArrayList<String>());
		{
			Q q2 = q1.and(colTab1Id.gt(10L));
			String q1StrAfterOperation = new QSQLProvider(q1).select(
					new Table.Column<?>[]{colTab1Id},
					new ArrayList<String>());
			assertEquals(q1Str, q1StrAfterOperation);
			String q2Str = new QSQLProvider(q2).select(
					new Table.Column<?>[]{colTab1Id},
					new ArrayList<String>());
			assertFalse( q2Str.equals(q1Str));
		}

		{
			Q q3 = Q.not(q1);
			String q1StrAfterNotOperation = new QSQLProvider(q1).select(
					new Table.Column<?>[]{colTab1Id},
					new ArrayList<String>());
			assertEquals(q1Str, q1StrAfterNotOperation);
			String q3Str = new QSQLProvider(q3).select(
					new Table.Column<?>[]{colTab1Id},
					new ArrayList<String>());
			assertFalse("String::"+q3Str, q3Str.equals(q1Str));
		}

		{
			Q q4 = q1.or(colTab1Id.eq(11L));
			String q1StrAfterOROperation = new QSQLProvider(q1).select(
					new Table.Column<?>[]{colTab1Id},
					new ArrayList<String>());
			assertEquals(q1Str, q1StrAfterOROperation);
			String q4Str = new QSQLProvider(q4).select(
					new Table.Column<?>[]{colTab1Id},
					new ArrayList<String>());
			assertFalse( q4Str.equals(q1Str));
		}
	}

	public void testImmutableQuerySet() throws Exception{
		DAO<Author> dao = facade.getDAOFactory().getDaoFor(Author.class);
		for (int i=0; i< 10; i++){
			Author author1 = new Author();
			author1.setName("nome["+i+"]");
			author1.setCreatedAt(new Date());
			try {
				dao.save(author1);
			} catch (IOException e) {
				fail(StringUtil.getStackTrace(e));
			}
		}
		final QuerySet<Author> qs1 = dao.query();
		List<Author> list1 = qs1.all();
		int qty = list1.size();
		Author first = qs1.first();

		{
			QuerySet<Author> qs2 = qs1.filter(Author.NAME.eq("nome["+9+"]"));
			List<Author> list2 = qs2.all();
			list1 = qs1.all();
			assertFalse(qs1 == qs2);
			assertEquals(qty, list1.size());
			assertTrue(list1.size() > list2.size() );
		}

		{
			QuerySet<Author> qs3 = qs1.limit(3);
			List<Author> list2 = qs3.all();
			list1 = qs1.all();
			assertFalse(qs1 == qs3);
			assertEquals(qty, list1.size());
			assertTrue( list1.size() > list2.size() );
		}

		{
			QuerySet<Author> qs3 = qs1.orderBy(Author.ID, Q.DESC);
			list1 = qs1.all();
			assertFalse(qs1 == qs3);
			List<Author> list3 = qs3.all();
			assertEquals(first, list1.get(0));
			assertFalse( first.equals(list3.get(0)) );
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
