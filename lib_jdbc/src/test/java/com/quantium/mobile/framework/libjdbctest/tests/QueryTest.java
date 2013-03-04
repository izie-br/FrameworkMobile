package com.quantium.mobile.framework.libjdbctest.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import static org.junit.Assert.*;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.DAOFactory;
import com.quantium.mobile.framework.jdbc.QH2DialectProvider;
import com.quantium.mobile.framework.libjdbctest.MemDaoFactory;
import com.quantium.mobile.framework.libjdbctest.vo.Author;
import com.quantium.mobile.framework.libjdbctest.vo.AuthorImpl;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.utils.StringUtil;

public class QueryTest {

	DAOFactory daoFactory = new MemDaoFactory();

	@SuppressWarnings("deprecation")
	@Test
	public void testLikeAndGlob(){
		DAO<Author> dao = daoFactory.getDaoFor(Author.class);
		Author author1 = new AuthorImpl();
		author1.setName("um nome");
		Date now = new Date();
		now = new Date(now.getYear(), now.getMonth(), now.getDate(),
				now.getHours(), now.getMinutes());
		author1.setCreatedAt(now);
		Author author2 = new AuthorImpl();
		author2.setName("outro nome");
		author2.setCreatedAt(now);
		Author author3 = new AuthorImpl();
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

	@Test
	public void testImmutableQuerySet() throws Exception{
		DAO<Author> dao = daoFactory.getDaoFor(Author.class);
		for (int i=0; i< 10; i++){
			Author author1 = new AuthorImpl();
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

	@Test
	public void testNotOp() {
		Q q = Q.not(Author.ID.eq(1L));
		QH2DialectProvider provider = new QH2DialectProvider(q);
		ArrayList<Object> args = new ArrayList<Object>();
		String str = provider.select(Arrays.asList((Object)Author.ID), args);
		//removendo parenteses, que podem aparecer arbitrariamente
		str = str.replaceAll("\\(", " ").replaceAll("\\)", " ");
		Pattern pattern = Pattern.compile(
				".*" +                     /* SELECT <colunas> FROM <table> */
				"where\\s+not\\s+" +       /* WHERE NOT */
				"[\\w\\._]+\\s*=\\s*\\?",  /* <tabela>.<coluna> = ? */
				Pattern.CASE_INSENSITIVE); /* case arbitrario */
		assertTrue(str, pattern.matcher(str).find());
	}

}
