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
import com.quantium.mobile.framework.libjdbctest.vo.Document;
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
		Author author4 = new AuthorImpl();
		author4.setName("com caracteres *[?.^$+-(){}]");
		author4.setCreatedAt(now);
		try {
			assertTrue(dao.save(author1));
			assertTrue(dao.save(author2));
			assertTrue(dao.save(author3));
			assertTrue(dao.save(author4));
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

		authors = dao.query(Q.glob(
					Author.NAME,
					// os caracteres *, [ e ? devem estar entre chaves []
					// o caractere ] deve estar sozinho, sem uma [ associada
					// nenhum desses outros caracteres deve ser tratado
					// de forma especial
					"*[*[?].^$+-(){}]")
				).all();
		assertEquals(1, authors.size());
		assertEquals(author4, authors.iterator().next());
	}

	@Test
	public void testLimitOffset () {
		try {
			Date now = new Date();
			DAO<Author> dao = daoFactory.getDaoFor(Author.class);

			Author author1 = new AuthorImpl(0, now, "author1", true, null, null);
			Author author2 = new AuthorImpl(0, now, "author2", true, null, null);
			Author author3 = new AuthorImpl(0, now, "author3", false, null, null);
			Author author4 = new AuthorImpl(0, now, "author4", true, null, null);

			assertTrue(dao.save(author1));
			assertTrue(dao.save(author2));
			assertTrue(dao.save(author3));
			assertTrue(dao.save(author4));

			List<Author> authors = dao.query().limit(1).all();
			assertEquals(1, authors.size());

			authors = dao.query().offset(2).all();
			assertEquals(2, authors.size());

			authors = dao.query(Author.ACTIVE.eq(true))
					.limit(1).offset(1)
					.orderBy(Author.NAME)
					.all();
			assertEquals(1, authors.size());
			assertEquals(author2, authors.get(0));
		} catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	@Test
	public void testQueryByDate() {
		Date referenceDate = new Date();
		Date beforeReference = new Date(referenceDate.getTime() - 2*60*1000);
		Date afterReference = new Date(referenceDate.getTime() + 2*60*1000);

		DAO<Author> dao = daoFactory.getDaoFor(Author.class);

		Author author1 = GeradorTest.randomAuthor();
		author1.setCreatedAt(beforeReference);
		Author author2 = GeradorTest.randomAuthor();
		author2.setCreatedAt(afterReference);

		try{
			assertTrue(dao.save(author1));
			assertTrue(dao.save(author2));

			List<Author> authorsCreatedBefore = dao
					.query(Author.CREATED_AT.lt(referenceDate))
					.all();
			assertEquals(1, authorsCreatedBefore.size());
			assertEquals(author1, authorsCreatedBefore.get(0));

			List<Author> authorsCreatedAfter = dao
					.query(Author.CREATED_AT.gt(referenceDate))
					.all();
			assertEquals(1, authorsCreatedAfter.size());
			assertEquals(author2, authorsCreatedAfter.get(0));
		} catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	@Test
	public void testJoinQuery(){
		DAO<Author> authorDao = daoFactory.getDaoFor(Author.class);
		DAO<Document> documentDao = daoFactory.getDaoFor(Document.class);

		Author author1 = GeradorTest.randomAuthor();
		Author author2 = GeradorTest.randomAuthor();
		Author author3 = GeradorTest.randomAuthor();

		Document doc1 = GeradorTest.randomDocument();
		doc1.setAuthor(author1);
		Document doc2 = GeradorTest.randomDocument();
		doc2.setAuthor(author2);
		Document doc3 = GeradorTest.randomDocument();
		doc3.setAuthor(author3);

		try {
			assertTrue(authorDao.save(author1));
			assertTrue(authorDao.save(author2));
			assertTrue(authorDao.save(author3));
			assertTrue(documentDao.save(doc1));
			assertTrue(documentDao.save(doc2));
			assertTrue(documentDao.save(doc3));

			List<Author> authors = authorDao.query(
					Author.ID.eq(Document.ID_AUTHOR)
					.and(Document.ID.eq(doc2.getId()))
			).all();
			assertEquals(1, authors.size());
			assertEquals(author2, authors.get(0));
		} catch (IOException e) {
			fail(StringUtil.getStackTrace(e));
		}
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
