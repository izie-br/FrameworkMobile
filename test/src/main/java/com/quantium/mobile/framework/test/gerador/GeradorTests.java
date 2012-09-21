package com.quantium.mobile.framework.test.gerador;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;

import android.database.sqlite.SQLiteDatabase;
import android.test.ActivityInstrumentationTestCase2;

import com.quantium.mobile.framework.Session;
import com.quantium.mobile.framework.test.GenericBean;
import com.quantium.mobile.framework.test.TestActivity;
import com.quantium.mobile.framework.test.db.DB;
import com.quantium.mobile.framework.test.gen.Author;
import com.quantium.mobile.framework.test.gen.Customer;
import com.quantium.mobile.framework.test.gen.Document;
import com.quantium.mobile.framework.test.gen.Score;

public class GeradorTests extends ActivityInstrumentationTestCase2<TestActivity> {

	public GeradorTests() {
		super("com.quantium.mobile.framework.test", TestActivity.class);
	}

	public Session getSession(){
		return new Session() {
			
			@Override
			public SQLiteDatabase getDb() {
				return DB.getDb();
			}
		};
	}

	public void testInsertUpdateDelete(){
		int count = Author.objects(getSession()).all().size();
		Author author = randomAuthor();
		assertTrue(author.save(getSession()));
		long id  = author.getId();
		assertTrue(id>0);
		author.setName(RandomStringUtils.random(40));
		author.save(getSession());
		assertEquals(id, author.getId());
		assertTrue(author.delete());
		int countAfter = Author.objects(getSession()).all().size();
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
			assertTrue(author.save(getSession()));
			assertTrue(author.getId()>0);
			authors[i] = author;
		}
		// buscando no banco e conferindo se a quantidadee eh igual ou supperior
		// aos inseridos
		Collection<Author> authorsFromDb = Author.objects(getSession()).all();
		assertTrue(authorsFromDb.size()>=authors.length);
		for(Author author : authors){
			assertTrue( authorsFromDb.contains(author) );
		}
		// buscando um a um por nome e conferindo se eh encontrado
		for(Author author : authors){
			Author authorFromDb = Author
					.objects(getSession())
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

	public void testMapSerialization(){
		Author author = randomAuthor();
		assertTrue(author.save(getSession()));
		assertTrue(author.getId()>0);
		Map<String, Object> map = author.toMap();
		assertNotNull(map);
		Author authorDesserialized =
				new Author().mapToObject(map);
		assertEquals(author, authorDesserialized);
		Author authorIdNull = randomAuthor();
		Map<String, Object> mapIdNull = authorIdNull.toMap();
		Author authorIdNullDesserialized =
				new Author().mapToObject(mapIdNull);
		assertEquals(authorIdNull, authorIdNullDesserialized);
		// A classe document tem um campo date mais "desafiador"
		Document doc = randomDocument();
		map = doc.toMap();
		assertNotNull(map);
		Document docDesserialized = new Document()
			.mapToObject(map);
		assertEquals(doc, docDesserialized);
	}

	public void testSerializationAlias(){
		int idDocument = 9;
		Map<String,Object> map = new HashMap<String, Object>();
		// idDocmuent eh o alias de document.id
		// ver no pom.xml
		map.put("id_document", idDocument);
		Document document = new Document().mapToObject(map);
		assertEquals(idDocument, document.getId());
	}

	public void testGenericBean() {
		Author obj = randomAuthor();
		try {
			GenericBean genericObj = obj;
			assertTrue(genericObj.save(getSession()));
			assertTrue(obj.getId()>0);
			long id = obj.getId();
			assertTrue(genericObj.delete());
			Author fromDb = Author.objects(getSession()).filter(Author.ID.eq(id)).first();
			assertNull(fromDb);
		} catch (ClassCastException e) {
			fail ("Beans nao herdam da classe " + GenericBean.class.getSimpleName());
		}
	}

	public void testDeleteCascade () {
		Author author = randomAuthor();
		assertTrue(author.save(getSession()));

		Document document = randomDocument();
		document.setAuthor(author);
		assertTrue(document.save(getSession()));

		Score score = new Score();
		score.setAuthor(author);
		score.setDocument(document);
		score.setScore( (new Random().nextInt())%100 );
		assertTrue(score.save(getSession()));

		Customer customer = randomCustomer();
		assertTrue(customer.save(getSession()));

		// adicionar os documentos oa customer
		//    e a busca pelo queryset deve achar este
		assertTrue(customer.addDocument(document));
		Collection<Document> documents = customer.getDocuments().all();
		assertEquals(document, documents.iterator().next());

		// A author, ao ser "deletado" deve desaparecer do banco
		assertTrue(author.delete());
		Author authorFromDb = Author.objects(getSession()).first();
		assertNull(authorFromDb);

		// Score tem relacao many-to-one para document e author
		//    ambas com chave NOT NULL
		//    e deve ser removida ao remover o author (veja acima)
		Collection<Score> scoresDb = Score.objects(getSession())
				.all();
		assertEquals(0, scoresDb.size());
//		Score scoreDb = scoresDb.iterator().next();
//		// document_id pode ser NULL
//		assertEquals(null, scoreDb.getDocument());

		// Document tem uma chave para author, mas pode ser null
		//    o document nao deve ser removido com o author,
		//    mas deve ter sua chave de author anulada
		documents = customer.getDocuments().all();
		assertEquals(1, documents.size());
		document = documents.iterator().next();
		assertEquals(null,document.getAuthor());

//		// Scores e Document tem chaves estrangeiras NOT NULL
//		//    ambas as classes filhas sao deletadas
//		assertTrue(author.delete());
//		Collection<Document> documentsDb =Document.objects()
//				.filter(Document.ID.eq(document.getId()))
//				.all();
//		assertEquals(0, documentsDb.size());
//		scoreDb = Score.objects()
//				.filter(Score.ID_AUTHOR.eq(author.getId()))
//				.first();
//		// refazer este teste
//		//assertNull(scoreDb);

		document.delete();
		documents = customer.getDocuments().all();
		assertEquals(0, documents.size());

		customer.delete();
		customer = Customer.objects(getSession())
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
		Date now = new Date();
		document.setCreatedAt( new Date(
			now.getYear(), now.getMonth(), now.getDate(),
			now.getHours(), now.getMinutes()//, now.getSeconds()
		));
		return document;
	}

	private Customer randomCustomer () {
		Customer customer = new Customer();
		customer.setName(RandomStringUtils.random(60));
		return customer;
	}

}
