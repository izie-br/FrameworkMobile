package com.quantium.mobile.framework.libjdbctest.tests;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.DAOFactory;
import com.quantium.mobile.framework.PrimaryKeyUpdater;
import com.quantium.mobile.framework.Save;
import com.quantium.mobile.framework.db.FirstLevelCache;
import com.quantium.mobile.framework.libjdbctest.MemDaoFactory;
import com.quantium.mobile.framework.libjdbctest.vo.Author;
import com.quantium.mobile.framework.libjdbctest.gen.AuthorEditable;
import com.quantium.mobile.framework.libjdbctest.gen.DocumentEditable;
import com.quantium.mobile.framework.libjdbctest.vo.AuthorImpl;
import com.quantium.mobile.framework.libjdbctest.vo.Customer;
import com.quantium.mobile.framework.libjdbctest.vo.CustomerImpl;
import com.quantium.mobile.framework.libjdbctest.vo.Document;
import com.quantium.mobile.framework.libjdbctest.vo.DocumentImpl;
import com.quantium.mobile.framework.libjdbctest.vo.Score;
import com.quantium.mobile.framework.libjdbctest.vo.ScoreImpl;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.utils.StringUtil;
import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.framework.validation.ValidationError;

public class GeradorTest {

	DAOFactory daoFactory = new MemDaoFactory();

	@Test
	public void testInsertUpdateDelete(){
		try {
			DAO<Author> dao = daoFactory.getDaoFor(Author.class);
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
	@Test
	public void testQuery(){
		int count = 5;
		DAO<Author> dao = daoFactory.getDaoFor(Author.class);
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

	@Test
	public void testGetById () {
		DAO<Author> dao = daoFactory.getDaoFor (Author.class);

		Author author1 = randomAuthor ();
		Author author2 = randomAuthor ();

		try{
			assertTrue (dao.save (author1));
			assertTrue (dao.save (author2));
		} catch (IOException e) {
			fail ();
		}

		long id1 = author1.getId ();
		Author author1FromDb = dao.get (id1);
		assertEquals (author1, author1FromDb);

		long id2 = author2.getId ();
		Author author2FromDb = dao.get (id2);
		assertEquals (author2, author2FromDb);
	}

	@Test
	public void testSaveInsertIfNotExists () {
		DAO<Author> dao = daoFactory.getDaoFor (Author.class);

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

	@Test
	public void testMapSerialization(){
		DAO<Author> dao = daoFactory.getDaoFor(Author.class);
		DAO<Document> documentDao = daoFactory.getDaoFor(Document.class);
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

	@Test
	public void testDeleteCascade () {
		try {
		DAO<Author> authorDao = daoFactory.getDaoFor(Author.class);
		DAO<Document> docDao = daoFactory.getDaoFor(Document.class);
		DAO<Score> scoreDao = daoFactory.getDaoFor(Score.class);
		DAO<Customer> customerDao = daoFactory.getDaoFor(Customer.class);
		Author author = randomAuthor();
		assertTrue(authorDao.save(author));

		Document document = randomDocument();
		document.setAuthor(author);
		assertTrue(docDao.save(document));

		Score score = new ScoreImpl();
		score.setAuthor(author);
		score.setDocument(document);
		score.setScore((new Random().nextInt())%100);
		assertTrue(scoreDao.save(score));

		Customer customer = randomCustomer();
		assertTrue(customerDao.save(customer));

		// adicionar os documentos oa customer
		//    e a busca pelo queryset deve achar este
		assertTrue(customerDao.with(customer).add(document));
		Collection<Document> documents = customer.getDocumentDocuments ().all ();
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
		documents = customer.getDocumentDocuments ().all ();
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
		documents = customer.getDocumentDocuments ().all ();
		assertEquals(0, documents.size());

		customerDao.delete(customer);
		customer = customerDao.query(Customer.ID.eq(customer.getId()))
				.first();
		assertNull(customer);
		} catch (IOException e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	@Test
	public void testInsertMultipleManyToManyEntries() {
		DAO<Author> authorDao = this.daoFactory.getDaoFor(Author.class);
		DAO<Document> documentDao = this.daoFactory.getDaoFor(Document.class);
		DAO<Customer> customerDao = this.daoFactory.getDaoFor(Customer.class);

		try {
			Author author = randomAuthor();
			assertTrue(authorDao.save(author));

			Document document = randomDocument();
			assertTrue(authorDao.with(author).add(document));

			Customer customer = randomCustomer();

			// O metodo with(  ).add() deve salvar o customer que ainda
			// que ainda nao tem ID
			assertTrue(documentDao.with(document).add(customer));
			List<Document> documents = customer.getDocumentDocuments().all();
			assertEquals(1, documents.size());
			assertEquals(document, documents.get(0));

			assertFalse(customerDao.with(customer).add(document));
			List<Customer> customers = document.getDocumentCustomers().all();
			assertEquals(1, customers.size());
			assertEquals(customer, customers.get(0));
		} catch (IOException e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	@Test
	public void testFirstLevelCache() {
		try {
			Author author = randomAuthor();
			DAO<Author> authorDao = daoFactory.getDaoFor(Author.class);
			assertTrue(authorDao.save(author));

			Document document = randomDocument();
			DAO<Document> documentDao = daoFactory.getDaoFor(Document.class);
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

	@Test
	public void testLazyInvocationHandler() {
		try {
			// Substituindo as SoftReference por WeakReference
			// As anteriores demoram muito ate serem coletadas
			DAOFactory weakRefFactory = weakRefCacheFactory();

			DAO<Author> authorDao = weakRefFactory.getDaoFor(Author.class);
			DAO<Document> documentDao = weakRefFactory.getDaoFor(Document.class);

			Author author = randomAuthor();
			assertTrue(authorDao.save(author));

			long authorId = author.getId();
			assertTrue(authorId != 0);
			String authorName = author.getName();
			assertNotNull(authorName);

			Document document = randomDocument();
			document.setAuthor(author);
			assertTrue(documentDao.save(document));

			// armazenando o ID para uma busca
			long documentId = document.getId();

			// Usando o objeto Author para monitorar o gc
			WeakReference<Author> authorWeakRef =
					new WeakReference<Author>(author);
			// Removendo todas referencias fortes ao Author
			author = null;
			document = null;

			// Quando esta referencia fraca se tornar null
			// significa que o GC rodou!
			while (authorWeakRef.get() != null) {
				System.gc();
				Thread.sleep(50);
			}

			// Buscando novamente o document
			document = documentDao.get(documentId);
			assertNotNull(document);

			// O author deve "existir" e deve ser um Proxy
			// @see java.lang.reflect.Proxy
			author = document.getAuthor();
			assertNotNull(author);
			assertTrue(Proxy.isProxyClass(author.getClass()));

			assertEquals(authorId, author.getId());
			assertEquals(authorName, author.getName());
		} catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	@Test
	public void testFirstLevelCacheTrim() {
		try {
			// Substituindo as SoftReference por WeakReference
			// As anteriores demoram muito ate serem coletadas
			DAOFactory weakRefFactory = weakRefCacheFactory();

			DAO<Author> authorDao = weakRefFactory.getDaoFor(Author.class);

			Author author = randomAuthor();
			assertTrue(authorDao.save(author));

			long authorId = author.getId();
			assertTrue(authorId != 0);
			String authorName = author.getName();
			assertNotNull(authorName);

			// Usando o objeto Author para monitorar o gc
			WeakReference<Author> authorWeakRef =
					new WeakReference<Author>(author);
			// Removendo todas referencias fortes ao Author
			author = null;

			// Quando esta referencia fraca se tornar null
			// significa que o GC rodou!
			while (authorWeakRef.get() != null) {
				System.gc();
				Thread.sleep(50);
			}

			// Nesta implementacal o dao herda do FirstLevelCache
			FirstLevelCache cache = (FirstLevelCache)weakRefFactory;
			// deve rodar sem excecao
			cache.trim();

			author = authorDao.get(authorId);
			assertEquals(authorId, author.getId());
			assertEquals(authorName, author.getName());

		} catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	@Test
	public void testCacheUpdate() {
		try {
			DAO<Author> dao = daoFactory.getDaoFor(Author.class);
			Author originalAuthor = randomAuthor();
			assertTrue(dao.save(originalAuthor));
			assertTrue(originalAuthor.getId() != 0);
	
			// Ao inserir um autor com dados diferentes mas
			AuthorEditable otherAuthor = (AuthorEditable)randomAuthor();
			// com mesmo ID
			otherAuthor.setId(originalAuthor.getId());

			String otherAuthorNewName = otherAuthor.getName();
			Date otherAuthorNewCreatedAt = otherAuthor.getCreatedAt();

			assertTrue(dao.save(otherAuthor));

			// O author inicial, em cache deve ser alterado
			assertEquals(originalAuthor, otherAuthor);
			assertEquals(otherAuthorNewName, originalAuthor.getName());
			assertEquals(otherAuthorNewCreatedAt, originalAuthor.getCreatedAt());

			Author authorCache = dao.get(originalAuthor.getId());
			assertEquals(otherAuthor, authorCache);
		}catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	@Test
	public void testUpdateCacheWithAssociation() {
		try {
			DAO<Author> authorDao = daoFactory.getDaoFor(Author.class);
			DAO<Document> documentDao = daoFactory.getDaoFor(Document.class);

			Author originalAuthor = randomAuthor();
			Author otherAuthor = randomAuthor();

			assertTrue(authorDao.save(originalAuthor));
			assertTrue(authorDao.save(otherAuthor));

			// os authors devem ter diferentes ids
			assertTrue(originalAuthor.getId() != otherAuthor.getId());

			Document originalDocument = randomDocument();
			originalDocument.setAuthor(originalAuthor);
			assertTrue(documentDao.save(originalDocument));

			// o outro Document tem mesmo ID mas Author diferente
			Document otherDocument = randomDocument();
			((DocumentEditable)otherDocument).setId(originalDocument.getId());
			otherDocument.setAuthor(otherAuthor);

			// O Document original deve ter o Author alterado
			assertTrue(documentDao.save(otherDocument));
			assertEquals(otherDocument, originalDocument);
			assertEquals(otherAuthor, originalDocument.getAuthor());

			// deve funcionar tambem ao marcar author como NULL
			// lembrando que author pode ser NULL
			otherDocument.setAuthor(null);
			assertTrue(documentDao.save(otherDocument));
			assertEquals(otherDocument, originalDocument);
			assertEquals(null, originalDocument.getAuthor());

		}catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	@Test
	public void testValidateVO () {
		Author author = new AuthorImpl ();
		author.setActive (true);
		author.setName (null);
		author.setCreatedAt (null);

		Collection<ValidationError> validationErrors = author.validate ();

		boolean nameNull = false;
		boolean createdAtNull = false;

		for (ValidationError error : validationErrors) {
			if (error.getColumn ().equals (Author.NAME) &&
			    error.getConstraint() instanceof Constraint.NotNull)
			{
				nameNull = true;
			} else if (error.getColumn ().equals (Author.CREATED_AT) &&
			           error.getConstraint() instanceof Constraint.NotNull)
			{
				createdAtNull = true;
			} else {
				fail (
					"O usuario deve ter nome null ou createdAt null, mas " +
					"foi encontrado " +
					error.getColumn ().getName () + 
					" com constraint invalida: " +
					error.getConstraint ().getClass().getSimpleName()
				);
			}
		}
		if (!nameNull)
			fail ("Usuario com nome NULL deve ser invalido");
		if (!createdAtNull)
			fail ("Usuario com createdAt NULL deve ser invalido");

		author.setName ("Nome Qualquer");
		author.setCreatedAt (new Date ());

		// a lista de erros deve ser uma lista vazia (nao pode ser null)
		assertEquals (0, author.validate ().size ());

		// o nome tem tamanho maximo de 79 pois eh um VARCHAR[79]
		author.setName(RandomStringUtils.randomAlphanumeric(80));
		validationErrors = author.validate();
		assertEquals(1, validationErrors.size());
		ValidationError maxError = validationErrors.iterator().next();
		assertEquals(Author.NAME, maxError.getColumn());
		assertTrue(maxError.getConstraint() instanceof Constraint.Max);
	}

	@Test
	public void testValidateThroughDAO () {
		DAO<Author> dao = daoFactory.getDaoFor (Author.class);
		Author author1= randomAuthor ();
		try {
			assertTrue (dao.save (author1));
		} catch (IOException e) {
			fail ();
		}
		String author1Name = author1.getName ();

		Author author2;
		do {
			author2 = randomAuthor ();
		} while (author2.getName ().equals (author1Name));

		Collection<ValidationError> errors = dao.validate (author2);
		assertEquals (0, errors.size ());

		author2.setName (author1Name);
		errors = dao.validate (author2);
		assertEquals (1, errors.size ());

		ValidationError error = errors.iterator ().next ();
		assertTrue (error.getConstraint () instanceof Constraint.Unique);
		assertEquals (Author.NAME, error.getColumn ());
	}

	@Test
	public void testQuerySetCount () {
		DAO<Author> dao = daoFactory.getDaoFor (Author.class);

		Author author1 = randomAuthor ();
		Author author2 = randomAuthor ();
		Author author3 = randomAuthor ();

		// Dois dos autores estao com active false
		author1.setActive (false);
		author2.setActive (false);
		author3.setActive (true);

		try {
			assertTrue (dao.save (author1));
			assertTrue (dao.save (author2));
			assertTrue (dao.save (author3));
		} catch (IOException e) {
			fail ();
		}
		// Dois dos autores devem estar com active false
		long qty = dao.query (Author.ACTIVE.eq (false)).count ();
		assertEquals (2, qty);
	}

	@Test
	public void testNullQuery () {
		try {
			DAO<Customer> customerDao = this.daoFactory.getDaoFor(Customer.class);
	
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
	@Test
	public void testNullToZeroQuery () {
		try {
			DAO<Author> authorDao = this.daoFactory.getDaoFor(Author.class);
			DAO<Document> docDao = this.daoFactory.getDaoFor(Document.class);

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

	@Test
	public void testUpdatePrimaryKey() {
		try {
			DAO<Author> authorDao = this.daoFactory.getDaoFor(Author.class);
			DAO<Document> documentDao = this.daoFactory.getDaoFor(Document.class);
			DAO<Score> scoreDao = this.daoFactory.getDaoFor(Score.class);
			DAO<Customer> customerDao = this.daoFactory.getDaoFor(Customer.class);

			@SuppressWarnings("unchecked")
			PrimaryKeyUpdater<Document> primaryKeyUpdater =
					(PrimaryKeyUpdater<Document>)documentDao;

			Author author = randomAuthor();
			assertTrue(authorDao.save(author));

			Document document = randomDocument();
			document.setAuthor(author);
			assertTrue(documentDao.save(document));

			Score score1 = new ScoreImpl(0, author, document, 55);
			assertTrue(scoreDao.save(score1));

			Score score2 = new ScoreImpl(0, author, document, 67);
			assertTrue(scoreDao.save(score2));

			Customer customer1 = randomCustomer();
			assertTrue(customerDao.save(customer1));
			documentDao.with(document).add(customer1);

			Customer customer2 = randomCustomer();
			assertTrue(customerDao.save(customer2));
			documentDao.with(document).add(customer2);

			// QuerySets de antes da troca de ID
			QuerySet<Score> scoresQuery = document.getDocumentScores();
			List<Score> scores = scoresQuery.all();
			QuerySet<Customer> customersQuery= document.getDocumentCustomers();
			List<Customer> customers = customersQuery.all();

			assertEquals(2, scoresQuery.count());
			for (Score score : scores) {
				assertTrue(score.equals(score1) || score.equals(score2));
			}

			assertEquals(2, customersQuery.count());
			for (Customer c :customers) {
				assertTrue(c.equals(customer1) || c.equals(customer2));
			}

			long oldPk = document.getId();
			long newPk = oldPk + 2000;
			primaryKeyUpdater.updatePrimaryKey(document, newPk);

			assertEquals(newPk, document.getId());

			assertNull(documentDao.get(oldPk));

			// Os querysets antigos apontam para os ID antigo do "document"
			// nao devem haver registros
			assertEquals(0, scoresQuery.count());
			assertEquals(0, customersQuery.count());

			// ao autalizar os querysets
			scoresQuery = document.getDocumentScores();
			// eles devem trazer "scores" com o "document" de ID novo
			assertEquals(2, scoresQuery.count());
			for (Score score : scores) {
				assertTrue(score.equals(score1) || score.equals(score2));
				assertEquals(newPk, score.getDocument().getId());
			}

			// ao autalizar os querysets
			customersQuery = document.getDocumentCustomers();
			// eles devem trazer "customers" com o "document" com ID novo
			assertEquals(2, customersQuery.count());
			for (Customer c :customers) {
				assertTrue(c.equals(customer1) || c.equals(customer2));
				List<Document> documents = c.getDocumentDocuments().all();
				assertEquals(1, documents.size());
				Document customerDocument = documents.get(0);
				assertEquals(newPk, customerDocument.getId());
			}

		} catch (IOException e) {
			fail(e.getMessage());
		}
	}


	@SuppressWarnings("deprecation")
	public static Author randomAuthor() {
		Author author = new AuthorImpl();
		author.setName(RandomStringUtils.random(60));
		Date now = new Date();
		author.setCreatedAt(new Date(
				now.getYear(), now.getMonth(), now.getDate(),
				now.getHours(), now.getMinutes()));
		author.setActive(true);
		return author;
	}

	@SuppressWarnings("deprecation")
	public static Document randomDocument () {
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

	public static Customer randomCustomer () {
		Customer customer = new CustomerImpl();
		customer.setName(RandomStringUtils.random(60));
		return customer;
	}

	private DAOFactory weakRefCacheFactory() {
		DAOFactory weakRefFactory = new MemDaoFactory(){
			@Override
			protected <T> java.lang.ref.Reference<T> createReference(T obj) {
				return new WeakReference<T>(obj);
			}
		};
		return weakRefFactory;
	}

}
