package br.com.cds.mobile.framework.test.gerador;

import java.util.Collection;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.color;
import android.test.ActivityInstrumentationTestCase2;
import br.com.cds.mobile.framework.test.GenericBean;
import br.com.cds.mobile.framework.test.TestActivity;
import br.com.cds.mobile.framework.test.gen.Author;
import br.com.cds.mobile.framework.test.gen.Customer;
import br.com.cds.mobile.framework.test.gen.Document;
import br.com.cds.mobile.framework.test.gen.Score;

public class GeradorTests extends ActivityInstrumentationTestCase2<TestActivity> {

	public GeradorTests() {
		super("br.com.cds.mobile.framework.test", TestActivity.class);
	}

	public void testInsertUpdateDelete(){
		int count = Author.objects().all().size();
		Author author = randomAuthor();
		assertTrue(author.save());
		long id  = author.getId();
		assertTrue(id>0);
		author.setName(RandomStringUtils.random(40));
		author.save();
		assertEquals(id, author.getId());
		assertTrue(author.delete());
		int countAfter = Author.objects().all().size();
		assertEquals(count, countAfter);
	}

	/**
	 * testa buscas com metodos objects . filter . (all | first).
	 * Este teste tambem pode falhar por erro no metodo equals!
	 */
	public void testQuery(){
		int count = 5;
		Author authors[]  = new Author[count];
		// inserindo 5 authors diferentes
		for(int i=0;i<count;i++){
			Author author = new Author();
			// para garantir strings diferentes, o comprimento varia com "i"
			author.setName(RandomStringUtils.random(20+i));
			assertTrue(author.save());
			assertTrue(author.getId()>0);
			authors[i] = author;
		}
		// buscando no banco e conferindo se a quantidadee eh igual ou supperior
		// aos inseridos
		Collection<Author> authorsFromDb = Author.objects().all();
		assertTrue(authorsFromDb.size()>=authors.length);
		for(Author author : authors){
			assertTrue( authorsFromDb.contains(author) );
		}
		// buscando um a um por nome e conferindo se eh encontrado
		for(Author author : authors){
			Author authorFromDb = Author
					.objects()
					.filter(Author.ID.eq(author.getId()))
					.first();
			assertEquals(author, authorFromDb);
		}
		// query complexa
		// qstr = "where author.id IN (?,?,?,?,?)"
//		StringBuilder qstr = new StringBuilder(Author.ID+" IN (");
//		Object args[] = new Object[authors.length];
//		for(int i=0;i<authors.length;i++){
//			args[i] = authors[i].getId();
//			qstr.append('?');
//			if( i < (authors.length-1) )
//				qstr.append(',');
//			else
//				qstr.append(')');
//		}
//		authorsFromDb = Author.objects().filter(qstr.toString(), args).all();
//		// conferindo se apenas o resultado esperado foi obtido
//		assertEquals(authors.length, authorsFromDb.size());
//		for(Author author : authors)
//			assertTrue(authorsFromDb.contains(author));
	}

	public void testJsonSerialization(){
		Author author = randomAuthor();
		assertTrue(author.save());
		assertTrue(author.getId()>0);
		JSONObject json = author.toJson();
		assertNotNull(json);
		try {
			Author authorDesserialized = new Author().jsonToObjectWithPrototype(json);
			assertEquals(author, authorDesserialized);
		} catch (JSONException e) {
			fail(e.getLocalizedMessage());
		}
		Author authorIdNull = randomAuthor();
		JSONObject jsonIdNull = authorIdNull.toJson();
		try {
			Author authorIdNullDesserialized = new Author().jsonToObjectWithPrototype(jsonIdNull);
			assertEquals(authorIdNull, authorIdNullDesserialized);
		} catch (JSONException e) {
			fail(e.getLocalizedMessage());
		}
		
	}

	public void testGenericBean() {
		Author obj = randomAuthor();
		try {
			GenericBean genericObj = obj;
			assertTrue(genericObj.save());
			assertTrue(obj.getId()>0);
			long id = obj.getId();
			assertTrue(genericObj.delete());
			Author fromDb = Author.objects().filter(Author.ID.eq(id)).first();
			assertNull(fromDb);
		} catch (ClassCastException e) {
			fail ("Beans nao herdam da classe " + GenericBean.class.getSimpleName());
		}
	}

	public void testDeleteCascade () {
		Author author = randomAuthor();
		assertTrue(author.save());

		Document document = randomDocument();
		document.setAuthor(author);
		assertTrue(document.save());

		Score score = new Score();
		score.setAuthor(author);
		score.setDocument(document);
		score.setScore( (new Random().nextInt())%100 );
		assertTrue(score.save());

		Customer customer = randomCustomer();
		assertTrue(customer.save());

		assertTrue(customer.addDocument(document));
		Collection<Document> documents = customer.getDocuments().all();
		assertEquals(1,documents.size());

		assertTrue(document.delete());
		documents = customer.getDocuments().all();
		assertEquals(0,documents.size());

		// Em score tem relacao many-to-one para document e autor,
		//   mas document_id PODE SER NULL, logo score tem
		//   document_id anulado apos delete do "document" referido
		Collection<Score> scoresDb = Score.objects()
				.filter(Score.ID_AUTHOR.eq(author.getId()))
				.all();
		assertEquals(1, scoresDb.size());
		Score scoreDb = scoresDb.iterator().next();
		// document_id pode ser NULL
		assertEquals(null, scoreDb.getDocument());

		// Scores e Document tem chaves estrangeiras NOT NULL
		//    ambas as classes filhas sao deletadas
		assertTrue(author.delete());
		Collection<Document> documentsDb =Document.objects()
				.filter(Document.ID.eq(document.getId()))
				.all();
		assertEquals(0, documentsDb.size());
		scoreDb = Score.objects()
				.filter(Score.ID_AUTHOR.eq(author.getId()))
				.first();
		//assertNull(scoreDb);

		documents = customer.getDocuments().all();
		assertEquals(0, documents.size());

		customer.delete();
		customer = Customer.objects()
				.filter(Customer.ID.eq(customer.getId()))
				.first();
		assertNull(customer);
	}

	private Author randomAuthor() {
		Author author = new Author();
		author.setName(RandomStringUtils.random(60));
		return author;
	}

	private Document randomDocument () {
		Document document = new Document();
		document.setText(RandomStringUtils.random(6000));
		document.setTitle(RandomStringUtils.random(60));
		return document;
	}

	private Customer randomCustomer () {
		Customer customer = new Customer();
		customer.setName(RandomStringUtils.random(60));
		return customer;
	}

}
