package com.quantium.mobile.framework.test.gerador;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;

import android.test.ActivityInstrumentationTestCase2;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.test.SessionFacade;
import com.quantium.mobile.framework.test.TestActivity;
import com.quantium.mobile.framework.test.gen.Author;
import com.quantium.mobile.framework.test.gen.AuthorImpl;
import com.quantium.mobile.framework.test.gen.Customer;
import com.quantium.mobile.framework.test.gen.CustomerImpl;
import com.quantium.mobile.framework.test.gen.Document;
import com.quantium.mobile.framework.test.gen.DocumentImpl;
import com.quantium.mobile.framework.test.gen.Score;
import com.quantium.mobile.framework.test.gen.ScoreImpl;
import com.quantium.mobile.framework.utils.StringUtil;

public class GeradorTests extends ActivityInstrumentationTestCase2<TestActivity> {

	SessionFacade facade = new SessionFacade();

	public GeradorTests() {
		super("com.quantium.mobile.framework.test", TestActivity.class);
	}

	public void testInsertUpdateDelete(){
		try {
			DAO<Author> dao = facade.getDAOFactory().getDaoFor(Author.class);
			int count = dao.query().all().size();
			Author author = randomAuthor();
			assertTrue(dao.save(author));
			long id  = author.getId();
			assertTrue(id>0);
			author.setName(RandomStringUtils.random(40));
			assertTrue(dao.save(author));
			assertEquals(id, author.getId());
			assertTrue(dao.delete(author));
			int countAfter = dao.query().all().size();
			assertEquals(count, countAfter);
		} catch (IOException e){
			fail(StringUtil.getStackTrace(e));
		}
	}

	/**
	 * testa buscas com metodos objects . filter . (all | first).
	 * Este teste tambem pode falhar por erro no metodo equals!
	 */
	public void testQuery(){
		int count = 5;
		DAO<Author> dao = facade.getDAOFactory().getDaoFor(Author.class);
		Author authors[]  = new Author[count];
		// inserindo 5 authors diferentes
		for(int i=0;i<count;i++){
			Author author = randomAuthor();
			// para garantir strings diferentes, o comprimento varia com "i"
			author.setName(RandomStringUtils.random(20+i));
			author.setActive(true);
			try {
				assertTrue(dao.save(author));
				assertTrue(author.getId()>0);
			} catch (IOException e){
				fail(StringUtil.getStackTrace(e));
			}
			authors[i] = author;
		}
		// buscando no banco e conferindo se a quantidadee eh igual ou supperior
		// aos inseridos
		Collection<Author> authorsFromDb = dao.query().all();
		assertTrue(authorsFromDb.size()>=authors.length);
		for(Author author : authors){
			assertTrue( authorsFromDb.contains(author) );
		}
		// buscando um a um por nome e conferindo se eh encontrado
		for(Author author : authors){
			Author authorFromDb = dao.query(
						Author.ID.eq(author.getId()))
					.first();
			assertEquals(author.getName(), authorFromDb.getName());
			assertEquals(author.getCreatedAt(), authorFromDb.getCreatedAt());
			assertEquals(author.isActive(), authorFromDb.isActive());
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
		DAO<Author> dao = facade.getDAOFactory().getDaoFor(Author.class);
		DAO<Document> documentDao = facade.getDAOFactory().getDaoFor(Document.class);
		Author author = randomAuthor();
		try {
		assertTrue(dao.save(author));
		assertTrue(author.getId()>0);
		} catch (IOException e){
			fail(StringUtil.getStackTrace(e));
		}
		Map<String, Object> map = author.toMap();
		assertNotNull(map);
		Author authorDesserialized =
				dao.mapToObject(map);
		assertEquals(author, authorDesserialized);
		Author authorIdNull = randomAuthor();
		Map<String, Object> mapIdNull = authorIdNull.toMap();
		Author authorIdNullDesserialized =
				dao.mapToObject(mapIdNull);
		assertEquals(authorIdNull, authorIdNullDesserialized);
		// A classe document tem um campo date mais "desafiador"
		Document doc = randomDocument();
		map = doc.toMap();
		assertNotNull(map);
		Document docDesserialized = documentDao
			.mapToObject(map);
		assertEquals(doc, docDesserialized);
	}

	public void testSerializationAlias(){
		DAO<Document> documentDao = facade.getDAOFactory().getDaoFor(Document.class);
		int idDocument = 9;
		Map<String,Object> map = new HashMap<String, Object>();
		// idDocmuent eh o alias de document.id
		// ver no pom.xml
		map.put("id_document", idDocument);
		Document document = documentDao.mapToObject(map);
		assertEquals(idDocument, document.getId());
	}

/*	public void testGenericBean() {
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
*/
	public void testDeleteCascade () {
		try {
		DAO<Author> authorDao = facade.getDAOFactory().getDaoFor(Author.class);
		DAO<Document> docDao = facade.getDAOFactory().getDaoFor(Document.class);
		DAO<Score> scoreDao = facade.getDAOFactory().getDaoFor(Score.class);
		DAO<Customer> customerDao = (DAO<Customer>) facade.getDAOFactory()
				.getDaoFor(Customer.class);
		Author author = randomAuthor();
		assertTrue(authorDao.save(author));

		Document document = randomDocument();
		document.setAuthor(author);
		assertTrue(docDao.save(document));

		Score score = new ScoreImpl(author, document, (new Random().nextInt())%100);
		assertTrue(scoreDao.save(score));

		Customer customer = randomCustomer();
		assertTrue(customerDao.save(customer));

		// adicionar os documentos oa customer
		//    e a busca pelo queryset deve achar este
		assertTrue(customerDao.with(customer).add(document));
		Collection<Document> documents = customer.getDocuments().all();
		assertEquals(document, documents.iterator().next());

		// A author, ao ser "deletado" deve desaparecer do banco
		assertTrue(authorDao.delete(author));
		Author authorFromDb = authorDao.query().first();
		assertNull(authorFromDb);

		// Score tem relacao many-to-one para document e author
		//    ambas com chave NOT NULL
		//    e deve ser removida ao remover o author (veja acima)
		Collection<Score> scoresDb = scoreDao.query()
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

		docDao.delete(document);
		documents = customer.getDocuments().all();
		assertEquals(0, documents.size());

		customerDao.delete(customer);
		customer = customerDao.query(Customer.ID.eq(customer.getId()))
				.first();
		assertNull(customer);
		} catch (IOException e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	public void testFirstLevelCache() {
		try {
			Author author = randomAuthor();
			DAO<Author> authorDao = facade.getDAOFactory().getDaoFor(Author.class);
			assertTrue(authorDao.save(author));

			Document document = randomDocument();
			DAO<Document> documentDao = facade.getDAOFactory().getDaoFor(Document.class);
			assertTrue(documentDao.save(document));

			// Se o cache funcionar, a busca pelo mesmo autor (mesma PrimaryKey)
			//   deve retornar o mesmo objeto ja em memoria
			Author sameAuthor = authorDao.query(Author.ID.eq(author.getId())).first();
			assertTrue (author == sameAuthor);

			// Adiciona o "author" ao objeto "document" em cache.
			// O objeto "document" nao eh salvo, logo o banco nao muda
			document.setAuthor(author);

			// Mesmo o objeto "document" nao sendo salvo apos o setAuthor()
			//   ele deve estar em cache e deve ter o campo _Author anulado
			//   apos a delecao do "author"
			// Como "document" nao foi salvo, o banco nao altera
			assertTrue(authorDao.delete(author));
			assertTrue(document.getAuthor() == null);

			assertTrue(documentDao.delete(document));
		} catch (IOException e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	private Author randomAuthor() {
		Author author = new AuthorImpl();
		author.setName(RandomStringUtils.random(60));
		Date now = new Date();
		author.setCreatedAt(new Date(
				now.getYear(), now.getMonth(), now.getDate(),
				now.getHours(), now.getMinutes()));
		author.setActive(true);
		return author;
	}

	private Document randomDocument () {
		Document document = new DocumentImpl();
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
		Customer customer = new CustomerImpl();
		customer.setName(RandomStringUtils.random(60));
		return customer;
	}

}
