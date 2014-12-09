package com.quantium.mobile.framework.test.query;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipInputStream;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.test.ActivityInstrumentationTestCase2;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.query.Q;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.query.SQLiteQuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.test.SessionFacade;
import com.quantium.mobile.framework.test.TestActivity;
import com.quantium.mobile.framework.test.db.DB;
import com.quantium.mobile.framework.test.document.vo.DocumentImpl;
import com.quantium.mobile.framework.utils.StringUtil;
import com.quantium.mobile.framework.test.document.vo.Document;
import com.quantium.mobile.framework.test.utils.Utils;
import com.quantium.mobile.framework.test.vo.Author;
import com.quantium.mobile.framework.test.vo.AuthorImpl;
import com.quantium.mobile.framework.test.vo.Customer;
import com.quantium.mobile.framework.test.vo.Score;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SQLiteTest extends ActivityInstrumentationTestCase2<TestActivity> {

    SessionFacade facade = new SessionFacade();

    public SQLiteTest() {
        super("com.quantium.mobile.framework.test", TestActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //Limpar o banco
        {
            List<Score> objects = facade.query(Score.class).all();
            for (Score obj : objects) {
                facade.delete(obj, true);
            }
        }
        {
            List<Customer> objects = facade.query(Customer.class).all();
            for (Customer obj : objects) {
                facade.delete(obj, true);
            }
        }
        {
            List<Document> objects = facade.query(Document.class).all();
            for (Document obj : objects) {
                facade.delete(obj, true);
            }
        }
        {
            List<Author> objects = facade.query(Author.class).all();
            for (Author obj : objects) {
                facade.delete(obj, true);
            }
        }
    }

    public void testPrimaryKey() throws IOException {
        Date now = new Date();
        String id = "123456";
        Document document1 = new DocumentImpl();
        document1.setId(id);
        document1.setTitle("A primary key");
        document1.setCreatedAt(now);
        document1.setText("A primary key");
        assertTrue(facade.save(document1));
        assertEquals(1, facade.query(Document.class, Document.ID.eq(id)).count());
        document1 = new DocumentImpl();
        document1.setId(id);
        document1.setTitle("A primary key");
        document1.setCreatedAt(now);
        document1.setText("A primary key");
        assertTrue(facade.save(document1));
        assertEquals(1, facade.query(Document.class, Document.ID.eq(id)).count());
    }

    public void testPrimaryKeyDb() throws IOException {
        Date now = new Date();
        String id = "123456";
        Document document1 = new DocumentImpl();
        document1.setId(id);
        document1.setTitle("A primary key");
        document1.setCreatedAt(now);
        document1.setText("A primary key");
        assertTrue(facade.save(document1));
        QuerySet<Document> query = facade.query(Document.class, Document.ID.eq(id));
        assertEquals(1, query.count());
        Cursor tempDatabaseCursor = ((SQLiteQuerySet) query).getCursor(Arrays.asList("*"));
        tempDatabaseCursor.moveToNext();
        ContentValues contentValues = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(tempDatabaseCursor, contentValues);
        tempDatabaseCursor.close();
        try {
            DB.getDb().insertOrThrow(Document._TABLE.getName(), null, contentValues);
            assertEquals(1, query.count());
            fail("should have thrown constraint exception");
        } catch (SQLException sql) {
        }
    }

    @Test
    public void testGroupBy() throws IOException {
        Score score1 = Utils.randomScore();
        score1.setScore(42);
        Score score2 = Utils.randomScore();
        score2.setScore(32);
        Score score3 = Utils.randomScore();
        score3.setScore(22);
        Author author1 = Utils.randomAuthor();
        assertTrue(facade.save(author1));
        Document document1 = Utils.randomDocument();
        assertTrue(facade.save(document1));
        Document document2 = Utils.randomDocument();
        assertTrue(facade.save(document2));
        score1.setAuthor(author1);
        score2.setAuthor(author1);
        score3.setAuthor(author1);
        score1.setDocument(document1);
        score2.setDocument(document2);
        score3.setDocument(document1);
        assertTrue(facade.save(score1));
        assertTrue(facade.save(score2));
        assertTrue(facade.save(score3));
        assertEquals(3, facade.query(Score.class).count());
        assertEquals(1, facade.query(Score.class).groupBy(Score.SCORE.sum(), Score.ID_AUTHOR).size());
        assertEquals(96, facade.query(Score.class).groupBy(Score.SCORE.sum(), Score.ID_AUTHOR).get(0).getScore());
        assertEquals(3, facade.query(Score.class).groupBy(Score.SCORE.count(), Score.SCORE).size());
        List<Score> scoresCount = facade.query(Score.class).groupBy(Score.SCORE.count(), Score.ID_DOCUMENT);
        assertEquals(2, scoresCount.get(0).getScore());
        assertEquals(1, scoresCount.get(1).getScore());
        List<Score> scoreSum = facade.query(Score.class).groupBy(Score.SCORE.sum(), Score.ID_DOCUMENT);
        assertEquals(64, scoreSum.get(0).getScore());
        assertEquals(32, scoreSum.get(1).getScore());
        List<Score> scoreAvg = facade.query(Score.class).groupBy(Score.SCORE.avg(), Score.ID_DOCUMENT);
        assertEquals(32, scoreAvg.get(0).getScore());
        assertEquals(32, scoreAvg.get(1).getScore());
        List<Score> scoreMin = facade.query(Score.class).groupBy(Score.SCORE.min(), Score.ID_DOCUMENT);
        assertEquals(22, scoreMin.get(0).getScore());
        assertEquals(32, scoreMin.get(1).getScore());
        List<Score> scoreMax = facade.query(Score.class).groupBy(Score.SCORE.max(), Score.ID_DOCUMENT);
        assertEquals(42, scoreMax.get(0).getScore());
        assertEquals(32, scoreMax.get(1).getScore());
        assertEquals(2, facade.query(Score.class).orderBy(Score.ID_DOCUMENT.asc()).groupBy(Score.SCORE.count(), Score.ID_DOCUMENT, Score.ID_AUTHOR).size());
    }


    public void testSelectDistinct() throws IOException {
        Date now = new Date();
        Document document1 = new DocumentImpl();
        document1.setTitle("A name");
        document1.setCreatedAt(now);
        document1.setText("A Text");
        assertTrue(facade.save(document1));
        Document document2 = new DocumentImpl();
        document2.setTitle("Another name");
        document2.setCreatedAt(now);
        document2.setText("A Text");
        assertTrue(facade.save(document2));
        Document document3 = new DocumentImpl();
        document3.setTitle("A name");
        document3.setCreatedAt(now);
        document3.setText("A Text");
        Author author3 = Utils.randomAuthor();
        author3.setName("A name");
        author3.setCreatedAt(now);
        document3.setAuthor(author3);
        assertTrue(facade.save(document3));
        assertTrue(facade.save(author3));
        assertTrue(facade.withAdd(author3,document3));
        QuerySet<Document> querySet = facade.query(Document.class);
        assertEquals(3, querySet.count());
        Set<String> titles = querySet.selectDistinct(Document.TITLE);
        assertEquals(2, titles.size());
        Set<String> texts = querySet.selectDistinct(Document.TEXT);
        assertEquals(1, texts.size());
        Set<String> ids = querySet.filter(Document.ID_AUTHOR.eq(Author.ID)).selectDistinct(Document.ID_AUTHOR);
        assertEquals(1, ids.size());
        assertEquals(ids.iterator().next(), author3.getId());
        ids = querySet.filter(Document.ID_AUTHOR.eq(Author.ID)).selectDistinct(Author.ID);
        assertEquals(1, ids.size());
        assertEquals(ids.iterator().next(), author3.getId());
    }


    public void testLikeAndGlob() {
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
            assertTrue(facade.save(author1));
            assertTrue(facade.save(author2));
            assertTrue(facade.save(author3));
            assertTrue(facade.save(author4));
        } catch (IOException e) {
            fail(StringUtil.getStackTrace(e));
        }
        // buscas com LIKE
        Collection<Author> authors = facade.query(Author.class,
                Q.like(Author.NAME, "%no_e"))
                .all();
        assertNotNull(authors);
        assertEquals(2, authors.size());
        assertTrue(authors.contains(author1));
        assertTrue(authors.contains(author2));
        // buscas com GLOB
        authors = facade.query(Author.class,
                Q.glob(Author.NAME, "*[nm]?[mw]e"))
                .all();
        assertNotNull(authors);
        assertEquals(2, authors.size());
        assertTrue(authors.contains(author1));
        assertTrue(authors.contains(author2));

        authors = facade.query(Author.class, Q.glob(
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

    public void testLimitOffset() {
        try {
            Date now = new Date();
            Author author1 = new AuthorImpl(null, now, null, null, "author1", true, null, null);
            Author author2 = new AuthorImpl(null, now, null, null, "author2", true, null, null);
            Author author3 = new AuthorImpl(null, now, null, null, "author3", false, null, null);
            Author author4 = new AuthorImpl(null, now, null, null, "author4", true, null, null);

            assertTrue(facade.save(author1));
            assertTrue(facade.save(author2));
            assertTrue(facade.save(author3));
            assertTrue(facade.save(author4));

            List<Author> authors = facade.query(Author.class).limit(1).all();
            assertEquals(1, authors.size());

            authors = facade.query(Author.class).offset(2).all();
            assertEquals(2, authors.size());

            authors = facade.query(Author.class, Author.ACTIVE.eq(true))
                    .limit(1).offset(1)
                    .orderBy(Author.NAME)
                    .all();
            assertEquals(1, authors.size());
            assertEquals(author2, authors.get(0));
        } catch (Exception e) {
            fail(StringUtil.getStackTrace(e));
        }
    }

    public void testQueryByDate() {
        Date referenceDate = new Date();
        Date beforeReference = new Date(referenceDate.getTime() - 2 * 60 * 1000);
        Date afterReference = new Date(referenceDate.getTime() + 2 * 60 * 1000);
        Author author1 = Utils.randomAuthor();
        author1.setCreatedAt(beforeReference);
        Author author2 = Utils.randomAuthor();
        author2.setCreatedAt(afterReference);

        try {
            assertTrue(facade.save(author1));
            assertTrue(facade.save(author2));

            List<Author> authorsCreatedBefore = facade
                    .query(Author.class, Author.CREATED_AT.lt(referenceDate))
                    .all();
            assertEquals(1, authorsCreatedBefore.size());
            assertEquals(author1, authorsCreatedBefore.get(0));

            List<Author> authorsCreatedAfter = facade
                    .query(Author.class, Author.CREATED_AT.gt(referenceDate))
                    .all();
            assertEquals(1, authorsCreatedAfter.size());
            assertEquals(author2, authorsCreatedAfter.get(0));
        } catch (Exception e) {
            fail(StringUtil.getStackTrace(e));
        }
    }

    public void testJoinQuery() {
        Author author1 = Utils.randomAuthor();
        Author author2 = Utils.randomAuthor();
        Author author3 = Utils.randomAuthor();

        Document doc1 = Utils.randomDocument();
        Document doc2 = Utils.randomDocument();
        Document doc3 = Utils.randomDocument();

        Score score1 = Utils.randomScore();
        Score score2 = Utils.randomScore();
        Score score3 = Utils.randomScore();

        score1.setAuthor(author1);
        score1.setDocument(doc1);
        score2.setAuthor(author2);
        score2.setDocument(doc2);
        score3.setAuthor(author3);
        score3.setDocument(doc3);
        try {
            assertTrue(facade.save(author1));
            assertTrue(facade.withAdd(author1, doc1));
            assertTrue(facade.save(author2));
            assertTrue(facade.withAdd(author2, doc2));
            assertTrue(facade.save(author3));
            assertTrue(facade.withAdd(author3, doc3));
            assertTrue(facade.save(score1));
            assertTrue(facade.save(score2));
            assertTrue(facade.save(score3));
            List<Author> authors = facade.query(Author.class,
                    Author.ID.eq(Document.ID_AUTHOR)
                            .and(Document.ID.eq(doc2.getId()))
            ).all();
            assertEquals(1, authors.size());
            assertEquals(author2, authors.get(0));
            QuerySet<Score> scoreQuery = facade.query(Score.class,
                    Score.ID_DOCUMENT.eq(Document.ID)
                            .and(Score.ID_AUTHOR.eq(Author.ID))
            );
            List<Score> scores = scoreQuery.all();
            assertEquals(3, scores.size());
            assertEquals(score1, scores.get(0));
            assertEquals(score2, scores.get(1));
            assertEquals(score3, scores.get(2));
            scores = author1.getAuthorScores().filter(
                    Score.ID_DOCUMENT.eq(Document.ID)
                            .and(Score.ID_AUTHOR.eq(Author.ID)).and(Author.ID.eq(author1.getId()))
            ).all();
            assertEquals(1, scores.size());
            assertEquals(score1, scores.get(0));
        } catch (IOException e) {
            fail(StringUtil.getStackTrace(e));
        }
    }

    public void testImmutableQuerySet() throws Exception {
        for (int i = 0; i < 10; i++) {
            Author author1 = new AuthorImpl();
            author1.setName("nome[" + i + "]");
            author1.setCreatedAt(new Date());
            try {
                facade.save(author1);
            } catch (IOException e) {
                fail(StringUtil.getStackTrace(e));
            }
        }
        final QuerySet<Author> qs1 = facade.query(Author.class);
        List<Author> list1 = qs1.all();
        int qty = list1.size();
        Author first = qs1.first();

        {
            QuerySet<Author> qs2 = qs1.filter(Author.NAME.eq("nome[" + 9 + "]"));
            List<Author> list2 = qs2.all();
            list1 = qs1.all();
            assertFalse(qs1 == qs2);
            assertEquals(qty, list1.size());
            assertTrue(list1.size() > list2.size());
        }

        {
            QuerySet<Author> qs3 = qs1.limit(3);
            List<Author> list2 = qs3.all();
            list1 = qs1.all();
            assertFalse(qs1 == qs3);
            assertEquals(qty, list1.size());
            assertTrue(list1.size() > list2.size());
        }

        {
            QuerySet<Author> qs3 = qs1.orderBy(Author.ID.desc());
            list1 = qs1.all();
            assertFalse(qs1 == qs3);
            List<Author> list3 = qs3.all();
            assertEquals(first, list1.get(0));
            assertFalse(first.equals(list3.get(0)));
        }
    }


}
