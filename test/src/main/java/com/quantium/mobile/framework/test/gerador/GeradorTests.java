package com.quantium.mobile.framework.test.gerador;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;

import android.test.ActivityInstrumentationTestCase2;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.Save;
import com.quantium.mobile.framework.test.SessionFacade;
import com.quantium.mobile.framework.test.TestActivity;
import com.quantium.mobile.framework.test.vo.Author;
import com.quantium.mobile.framework.test.gen.AuthorEditable;
import com.quantium.mobile.framework.test.vo.AuthorImpl;
import com.quantium.mobile.framework.test.vo.Customer;
import com.quantium.mobile.framework.test.vo.CustomerImpl;
import com.quantium.mobile.framework.test.document.vo.Document;
import com.quantium.mobile.framework.test.document.vo.DocumentImpl;
import com.quantium.mobile.framework.test.vo.Score;
import com.quantium.mobile.framework.test.vo.ScoreImpl;
import com.quantium.mobile.framework.utils.StringUtil;
import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.framework.validation.ValidationError;

public class GeradorTests extends ActivityInstrumentationTestCase2<TestActivity> {

	private static final int SCORE_MIN = 0;
	private static final int SCORE_MAX = 100;
	private static final int AUTHOR_NAME_MIN = 5;
	private static final int AUTHOR_NAME_MAX = 79;
	private static final int CUSTOMER_NAME_LEN = 60;

	SessionFacade facade = new SessionFacade();

	public GeradorTests() {
		super("com.quantium.mobile.framework.test", TestActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		//Limpar o banco
		{
			DAO<Score> dao = facade.getDAOFactory().getDaoFor(Score.class);
			List<Score> objects = dao.query().all();
			for (Score obj :objects) {
				dao.delete(obj);
			}
		}
		{
			DAO<Customer> dao = facade.getDAOFactory().getDaoFor(Customer.class);
			List<Customer> objects = dao.query().all();
			for (Customer obj :objects) {
				dao.delete(obj);
			}
		}
		{
			DAO<Document> dao = facade.getDAOFactory().getDaoFor(Document.class);
			List<Document> objects = dao.query().all();
			for (Document obj :objects) {
				dao.delete(obj);
			}
		}
		{
			DAO<Author> dao = facade.getDAOFactory().getDaoFor(Author.class);
			List<Author> objects = dao.query().all();
			for (Author obj :objects) {
				dao.delete(obj);
			}
		}
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
		final String idDocumentAlias = "id_document";
		final long idDocument = 9;
		final String createdAlias = "created";
		final Date created = new Date(99, 0, 1, 1, 0, 0);
		/*
		 * testando document, com dois alias "id_document" e "created"
		 */
		DAO<Document> documentDao =
				facade.getDAOFactory().getDaoFor(Document.class);
		Map<String,Object> map = new HashMap<String, Object>();
		// "idDocmuent" eh o alias de "document.id"
		// ver no pom.xml
		map.put(idDocumentAlias, idDocument);
		// "created" eh o alias de "created_at" para todas classes
		map.put(createdAlias, created);
		// testando mapToObject
		Document document = documentDao.mapToObject(map);
		assertEquals(idDocument, document.getId());
		assertEquals(created, document.getCreatedAt());
		// testando toMap
		Map<String,Object> documentToMap = document.toMap();
		assertEquals(idDocument, documentToMap.get(idDocumentAlias));
		assertEquals(created, documentToMap.get(createdAlias));
		/*
		 *  testando author com 1 alias "created"
		 */
		DAO<Author> authorDao = facade.getDAOFactory().getDaoFor(Author.class);
		// "created" eh o alias de "created_at" para todas classes
		Map<String,Object> authorMap = new HashMap<String, Object>();
		authorMap.put(createdAlias, created);
		// testando mapToObject
		Author author = authorDao.mapToObject(authorMap);
		assertEquals(created, author.getCreatedAt());
		// testando toMap
		Map<String,Object> authorToMap = author.toMap();
		assertEquals(created, authorToMap.get(createdAlias));
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

	public void testSaveInsertIfNotExists () {
		DAO<Author> dao = facade.getDAOFactory ().getDaoFor (Author.class);

		Author author = randomAuthor ();
		final long id = 5L;
		((AuthorEditable)author).setId (id);

		try{
			assertTrue (dao.save (randomAuthor ()));
			assertTrue (dao.save (randomAuthor ()));
			assertTrue (dao.save (randomAuthor ()));
			assertTrue (dao.save (author, Save.INSERT_IF_NOT_EXISTS));
		} catch (IOException e) {
			fail ();
		}

		assertEquals (id, author.getId ());

		Author authorFromDb = dao.get (id);
		assertEquals (author, authorFromDb);
	}

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

		Score score = new ScoreImpl();
		score.setAuthor(author);
		score.setDocument(document);
		score.setScore((new Random().nextInt())%SCORE_MAX);
		assertTrue(scoreDao.save(score));

		Customer customer = randomCustomer();
		assertTrue(customerDao.save(customer));

		// adicionar os documentos oa customer
		//    e a busca pelo queryset deve achar este
		assertTrue(customerDao.with(customer).add(document));
		Collection<Document> documents = customer.getCustomerDocuments().all();
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
		documents = customer.getCustomerDocuments().all();
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
		documents = customer.getCustomerDocuments().all();
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

	public void testCacheUpdate() {
		try {
			DAO<Author> dao = facade.getDAOFactory().getDaoFor(Author.class);
			Author originalAuthor = randomAuthor();
			assertTrue(dao.save(originalAuthor));
			assertTrue(originalAuthor.getId() != 0);
	
			// Ao inserir um autor com dados diferentes mas
			AuthorEditable otherAuthor = (AuthorEditable)randomAuthor();
			// com mesmo ID
			otherAuthor.setId(originalAuthor.getId());
			assertTrue(dao.save(otherAuthor));

			// O author inicial, em cache deve ser alterado
			assertEquals(originalAuthor, otherAuthor);
			Author authorCache = dao.get(originalAuthor.getId());
			assertEquals(otherAuthor, authorCache);
		}catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	public void testNullQuery () {
	try {
		DAO<Customer> customerDao = facade.getDAOFactory().getDaoFor(Customer.class);

		Customer customerNullName = randomCustomer();
		customerNullName.setName(null);
		assertTrue(customerDao.save(customerNullName));

		Customer customerNotNullName = randomCustomer();
		assertTrue(customerDao.save(customerNotNullName));

		{
			List<Customer> customersNullNameFromDb = customerDao
					.query(Customer.NAME.eq((String)null))
					.all();
			assertEquals(1, customersNullNameFromDb.size());
			assertEquals(customerNullName, customersNullNameFromDb.get(0));
		}
		{
			List<Customer> customersNotNullNameFromDb = customerDao
					.query(Customer.NAME.isNotNull())
					.all();
			assertEquals(1, customersNotNullNameFromDb.size());
			assertEquals(customerNotNullName, customersNotNullNameFromDb.get(0));
		}
	} catch (IOException e) {
		fail(StringUtil.getStackTrace(e));
	}
}


	/**
	 * Quando o tipo eh Long, Double e afins, NULL se torna 0 no banco
	 */
	public void testNullToZeroQuery () {
		try {
			DAO<Author> authorDao = facade.getDAOFactory().getDaoFor(Author.class);
			DAO<Document> docDao = facade.getDAOFactory().getDaoFor(Document.class);

			Author author = randomAuthor();
			assertTrue(authorDao.save(author));
	
			Document documentAuthorNotNull = randomDocument();
			documentAuthorNotNull.setAuthor(author);
			assertTrue(docDao.save(documentAuthorNotNull));

			Document documentAuthorNull = randomDocument();
			documentAuthorNull.setAuthor(null);
			assertTrue(docDao.save(documentAuthorNull));

			List<Document> documentAuthorNullFromDb = docDao
					.query(Document.ID_AUTHOR.eq((Long)null))
					.all();
			assertEquals(1, documentAuthorNullFromDb.size());
			assertEquals(documentAuthorNull, documentAuthorNullFromDb.get(0));

			documentAuthorNullFromDb = docDao
					.query(Document.ID_AUTHOR.isNull())
					.all();
			assertEquals(1, documentAuthorNullFromDb.size());
			assertEquals(documentAuthorNull, documentAuthorNullFromDb.get(0));

			List<Document> documentAuthorNotNullFromDb = docDao
					.query(Document.ID_AUTHOR.isNotNull())
					.all();
			assertEquals(1, documentAuthorNotNullFromDb.size());
			assertEquals(documentAuthorNotNull, documentAuthorNotNullFromDb.get(0));

		} catch (IOException e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	public void testMinMaxIntValidation () {
		try {
			DAO<Author> authorDao = facade.getDAOFactory().getDaoFor(Author.class);
			DAO<Document> docDao = facade.getDAOFactory().getDaoFor(Document.class);

			Author author = randomAuthor();
			assertTrue(authorDao.save(author));

			Document document = randomDocument();
			document.setAuthor(author);
			assertTrue(docDao.save(document));

			Score score = new ScoreImpl();
			score.setAuthor(author);
			score.setDocument(document);

			Collection<ValidationError> errors;
			ValidationError error;

			{
				score.setScore(SCORE_MIN);
				assertEquals(0, score.validate().size());
			}
			{
				score.setScore(SCORE_MIN-1);
				errors = score.validate();
				assertEquals(1, errors.size());
				error = errors.iterator().next();
				assertEquals(Score.SCORE, error.getColumn());
				assertTrue(error.getConstraint() instanceof Constraint.Min);
			}
			{
				score.setScore(SCORE_MAX);
				assertEquals(0, score.validate().size());
			}
			{
				score.setScore(SCORE_MAX+1);
				errors = score.validate();
				assertEquals(1, errors.size());
				error = errors.iterator().next();
				assertEquals(Score.SCORE, error.getColumn());
				assertTrue(error.getConstraint() instanceof Constraint.Max);
			}

			assertTrue(docDao.delete(document));
			assertTrue(authorDao.delete(author));
		}catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	public void testMinMaxStringValidation() {
		Collection<ValidationError> errors;
		ValidationError error;

		Author author = randomAuthor();
		assertEquals(0, author.validate().size());

		{
			String maxName = RandomStringUtils.randomAlphanumeric(AUTHOR_NAME_MAX);
			author.setName(maxName);
			assertEquals(0, author.validate().size());
		}
		{
			String biggerName = RandomStringUtils.randomAlphanumeric(AUTHOR_NAME_MAX+1);
			author.setName(biggerName);
			errors = author.validate();
			assertEquals(1, errors.size());
			error = errors.iterator().next();
			assertEquals(Author.NAME, error.getColumn());
			assertTrue(error.getConstraint() instanceof Constraint.Max);
		}
		{
			String minName = RandomStringUtils.randomAlphanumeric(AUTHOR_NAME_MIN);
			author.setName(minName);
			assertEquals(0, author.validate().size());
		}
		{
			String smallerName = RandomStringUtils.randomAlphanumeric(AUTHOR_NAME_MIN-1);
			author.setName(smallerName);
			errors = author.validate();
			assertEquals(1, errors.size());
			error = errors.iterator().next();
			assertEquals(Author.NAME, error.getColumn());
			assertTrue(error.getConstraint() instanceof Constraint.Min);
		}

	}

	/**
	 * Fixei o comprimento do nome do customer em 60
	 * Nao faz sentido, mas eh necessario para o teste
	 */
	public void testFixedLengthValidation () {
		Collection<ValidationError> errors;
		ValidationError error;

		Customer customer = randomCustomer();
		assertEquals(CUSTOMER_NAME_LEN, customer.getName().length());
		assertEquals(0, customer.validate().size());

		String biggerName = customer.getName() +"a";
		customer.setName(biggerName);
		errors = customer.validate();
		assertEquals(1, errors.size());
		error = errors.iterator().next();
		assertEquals(Customer.NAME, error.getColumn());
		assertTrue(error.getConstraint() instanceof Constraint.Length);

		String smallerName = customer.getName().substring(0, CUSTOMER_NAME_LEN-1);
		customer.setName(smallerName);
		errors = customer.validate();
		assertEquals(1, errors.size());
		error = errors.iterator().next();
		assertEquals(Customer.NAME, error.getColumn());
		assertTrue(error.getConstraint() instanceof Constraint.Length);
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
		customer.setName(RandomStringUtils.random(CUSTOMER_NAME_LEN));
		return customer;
	}
	
}
