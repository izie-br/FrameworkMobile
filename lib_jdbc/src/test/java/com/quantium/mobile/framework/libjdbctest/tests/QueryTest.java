package com.quantium.mobile.framework.libjdbctest.tests;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import com.quantium.mobile.framework.BaseGenericVO;
import com.quantium.mobile.framework.libjdbctest.vo.DocumentImpl;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import com.quantium.mobile.framework.BaseModelFacade;
import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.DAOFactory;
import com.quantium.mobile.framework.jdbc.QH2DialectProvider;
import com.quantium.mobile.framework.libjdbctest.JdbcPrimaryKeyProvider;
import com.quantium.mobile.framework.libjdbctest.JdbcToSyncProvider;
import com.quantium.mobile.framework.libjdbctest.MemDaoFactory;
import com.quantium.mobile.framework.libjdbctest.util.Utils;
import com.quantium.mobile.framework.libjdbctest.vo.Author;
import com.quantium.mobile.framework.libjdbctest.vo.AuthorImpl;
import com.quantium.mobile.framework.libjdbctest.vo.Document;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.utils.StringUtil;

import javax.management.Query;

public class QueryTest {
	DAOFactory daoFactory = new MemDaoFactory();
    BaseModelFacade facade = new BaseModelFacade(daoFactory, new JdbcPrimaryKeyProvider(), new JdbcToSyncProvider()) {

        @Override
        protected String getLoggedUserId() {
            return null;
        }

        @Override
        public <T extends BaseGenericVO> T refresh(Class<T> clazz, String id) throws Throwable {
            return null;
        }
    };

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
    public void testSelectDistinct() throws IOException {
        DAO<Document> dao = daoFactory.getDaoFor(Document.class);
        Date now = new Date();
        Document document1 = new DocumentImpl();
        document1.setTitle("A name");
        document1.setCreatedAt(now);
        document1.setText("A Text");
        assertTrue(dao.save(document1));
        Document document2 = new DocumentImpl();
        document2.setTitle("Another name");
        document2.setCreatedAt(now);
        document2.setText("A Text");
        assertTrue(dao.save(document2));
        Document document3 = new DocumentImpl();
        document3.setTitle("A name");
        document3.setCreatedAt(now);
        document3.setText("A Text");
        assertTrue(dao.save(document3));
        QuerySet<Document> querySet = dao.query();
        assertEquals(3, querySet.count());
        Set<String> titles = querySet.selectDistinct(Document.TITLE);
        assertEquals(2, titles.size());
        Set<String> texts = querySet.selectDistinct(Document.TEXT);
        assertEquals(1, texts.size());
    }

    //@Test
	public void testLazyInvocation() throws IOException {
		Author author1 = new AuthorImpl();
		author1.setName("lazy");
		facade.save(author1);
		//simular uma sincronia.
		String oldId = author1.getId();
		assertNotNull(oldId);
		facade.updatePrimaryKey(author1, "12345");
		assertNotSame(oldId, author1.getId());
	}


	@Test
	public void testLimitOffset () {
		try {
			Date now = new Date();
			DAO<Author> dao = daoFactory.getDaoFor(Author.class);

			Author author1 = new AuthorImpl(null, now, "author1", true, null, null);
			Author author2 = new AuthorImpl(null, now, "author2", true, null, null);
			Author author3 = new AuthorImpl(null, now, "author3", false, null, null);
			Author author4 = new AuthorImpl(null, now, "author4", true, null, null);

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

		Author author1 = Utils.randomAuthor();
		author1.setCreatedAt(beforeReference);
		Author author2 = Utils.randomAuthor();
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
		DAO<Author> dao = daoFactory.getDaoFor(Author.class);

		Author author1 = Utils.randomAuthor();
		Author author2 = Utils.randomAuthor();
		Author author3 = Utils.randomAuthor();

		Document doc1 = Utils.randomDocument();
		Document doc2 = Utils.randomDocument();
		Document doc3 = Utils.randomDocument();
		try {
			assertTrue(dao.save(author1));
			assertTrue(dao.with(author1).add(doc1));
			assertTrue(dao.save(author2));
			assertTrue(dao.with(author2).add(doc2));
			assertTrue(dao.save(author3));
			assertTrue(dao.with(author3).add(doc3));

			List<Author> authors = dao.query(
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
			QuerySet<Author> qs3 = qs1.orderBy(Author.ID.desc());
			list1 = qs1.all();
			assertFalse(qs1 == qs3);
			List<Author> list3 = qs3.all();
			assertEquals(first, list1.get(0));
			assertFalse( first.equals(list3.get(0)) );
		}
	}

    @Test
    public void testNotOp() {
        Q q = Q.not(Author.ID.eq("1"));
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


    @Test
    public void testListTables() throws IOException {
        List<String> tables = facade.listTempTables();
        assertFalse(tables.isEmpty());
    }

}
