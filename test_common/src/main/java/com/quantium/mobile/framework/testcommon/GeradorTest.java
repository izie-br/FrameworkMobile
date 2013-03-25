package com.quantium.mobile.framework.testcommon;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.DAOFactory;
import com.quantium.mobile.framework.PrimaryKeyUpdater;
import com.quantium.mobile.framework.Save;
import com.quantium.mobile.framework.db.FirstLevelCache;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.utils.StringUtil;
import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.framework.validation.ValidationError;

public abstract class GeradorTest<Author,Document,Score,Customer> {

	public static final int SCORE_MAX = 100;
	public static final int CUSTOMER_NAME_LEN = 60;

	private Class<Author> authorClass;
	private Class<Document> documentClass;
	private Class<Score> scoreClass;
	private Class<Customer> customerClass;

	public GeradorTest(
		Class<Author> authorClass, Class<Document> documentClass,
		Class<Score> scoreClass, Class<Customer> customerClass)
	{
		this.authorClass = authorClass;
		this.documentClass = documentClass;
		this.scoreClass = scoreClass;
		this.customerClass = customerClass;
	}

	public abstract DAOFactory getDaoFactory();

	public abstract DAOFactory weakRefCacheFactory();

	public abstract Author randomAuthor();

	public abstract Document randomDocument ();

	public abstract Customer randomCustomer ();

	public abstract Score randomScore();

	public abstract long getId(Object obj);

	public abstract void setId(Object obj, long id);

	public abstract Map<String,Object> toMap(Object author);

	public abstract Collection<ValidationError> validate(Object obj);

	//
	// Author
	//
	public abstract Table.Column<Long> authorId();

	public abstract Table.Column<String> authorName();

	public abstract Table.Column<Date> authorCreatedAt();

	public abstract Table.Column<Boolean> authorActive();

	public abstract String authorGetName(Author author);

	public abstract void authorSetName(Author author, String newName);

	public abstract Date authorGetCreatedAt(Author author);

	public abstract void authorSetCreatedAt(Author author, Date createdAt);

	public abstract boolean authorIsActive(Author author);

	public abstract void authorSetActive(Author author, boolean active);

	//
	// Document
	//
	public abstract Table.Column<Long> documentAuthorId();

	public abstract Author documentGetAuthor(Document document);

	public abstract void documentSetAuthor(Document document, Author author);

	public abstract QuerySet<Customer> documentGetCustomers(Document document);

	public abstract QuerySet<Score> documentGetScores(Document document);

	//
	// Score
	//
	public abstract void scoreSetAuthor(Score score, Author author);

	public abstract Document scoreGetDocument(Score score);

	public abstract void scoreSetDocument(Score score, Document document);

	//
	// Customer
	//
	public abstract Table.Column<Long> customerId();

	public abstract Table.Column<String> customerName();

	public abstract void customerSetName(Customer customer, String newName);

	public abstract QuerySet<Document> customerGetDocuments(Customer customer);

	//
	// Tests
	//
	@Test
	public void testInsertUpdateDelete(){
		try {
			DAO<Author> dao = getDaoFactory().getDaoFor(authorClass);
			int count = dao.query().all().size();
			Author author = randomAuthor();
			assertTrue(dao.save(author));
			long id  = getId(author);
			assertTrue(id>0);
			authorSetName(author, RandomStringUtils.random(40));
			assertTrue(dao.save(author));
			assertEquals(id, getId(author));
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
		DAO<Author> dao = getDaoFactory().getDaoFor(authorClass);
		List<Author> authors  = new ArrayList<Author>();
		// inserindo 5 authors diferentes
		for(int i=0;i<count;i++){
			Author author = randomAuthor();
			// para garantir strings diferentes, o comprimento varia com "i"
			authorSetName(author, RandomStringUtils.random(20+i));
			authorSetActive(author, true);
			try {
				assertTrue(dao.save(author));
				assertTrue(getId(author)>0);
			} catch (IOException e){
				fail(StringUtil.getStackTrace(e));
			}
			authors.add(author);
		}
		// buscando no banco e conferindo se a quantidadee eh igual ou supperior
		// aos inseridos
		Collection<Author> authorsFromDb = dao.query().all();
		assertTrue(authorsFromDb.size()>=authors.size());
		for(Author author : authors){
			assertTrue( authorsFromDb.contains(author) );
		}
		// buscando um a um por nome e conferindo se eh encontrado
		for(Author author : authors){
			Author authorFromDb = dao.query(
						authorId().eq(getId(author)))
					.first();
			assertEquals(authorGetName(author), authorGetName(authorFromDb));
			assertEquals(authorGetCreatedAt(author), authorGetCreatedAt(authorFromDb));
			assertEquals(authorIsActive(author), authorIsActive(authorFromDb));
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
		DAO<Author> dao = getDaoFactory().getDaoFor (authorClass);

		Author author1 = randomAuthor ();
		Author author2 = randomAuthor ();

		try{
			assertTrue (dao.save (author1));
			assertTrue (dao.save (author2));
		} catch (IOException e) {
			fail ();
		}

		long id1 = getId (author1);
		Author author1FromDb = dao.get (id1);
		assertEquals (author1, author1FromDb);

		long id2 = getId (author2);
		Author author2FromDb = dao.get (id2);
		assertEquals (author2, author2FromDb);
	}

	@Test
	public void testMapSerialization(){
		DAO<Author> dao = getDaoFactory().getDaoFor(authorClass);
		DAO<Document> documentDao = getDaoFactory().getDaoFor(documentClass);
		Author author = randomAuthor();
		try {
		assertTrue(dao.save(author));
		assertTrue(getId(author) > 0);
		} catch (IOException e){
			fail(StringUtil.getStackTrace(e));
		}
		Map<String, Object> map = toMap(author);
		assertNotNull(map);
		Author authorDesserialized =
				dao.mapToObject(map);
		assertEquals(author, authorDesserialized);
		Author authorIdNull = randomAuthor();
		Map<String, Object> mapIdNull = toMap(authorIdNull);
		Author authorIdNullDesserialized =
				dao.mapToObject(mapIdNull);
		assertEquals(authorIdNull, authorIdNullDesserialized);
		// A classe document tem um campo date mais "desafiador"
		Document doc = randomDocument();
		map = toMap(doc);
		assertNotNull(map);
		Document docDesserialized = documentDao
			.mapToObject(map);
		assertEquals(doc, docDesserialized);
	}

	@Test
	public void testSaveInsertIfNotExists () {
		DAO<Author> dao = getDaoFactory().getDaoFor (authorClass);

		Author author = randomAuthor ();
		final long id = 5L;
		setId (author, id);

		try{
			assertTrue (dao.save (randomAuthor ()));
			assertTrue (dao.save (randomAuthor ()));
			assertTrue (dao.save (randomAuthor ()));
			assertTrue (dao.save (author, Save.INSERT_IF_NOT_EXISTS));
		} catch (IOException e) {
			fail ();
		}

		assertEquals (id, getId (author));

		Author authorFromDb = dao.get (id);
		assertEquals (author, authorFromDb);
	}

	@Test
	public void testDeleteCascade () {
		try {
		DAO<Author> authorDao = getDaoFactory().getDaoFor(authorClass);
		DAO<Document> docDao = getDaoFactory().getDaoFor(documentClass);
		DAO<Score> scoreDao = getDaoFactory().getDaoFor(scoreClass);
		DAO<Customer> customerDao = getDaoFactory().getDaoFor(customerClass);
		Author author = randomAuthor();
		assertTrue(authorDao.save(author));

		Document document = randomDocument();
		documentSetAuthor(document, author);
		assertTrue(docDao.save(document));

		Score score = randomScore();
		scoreSetAuthor(score, author);
		scoreSetDocument(score, document);
		assertTrue(scoreDao.save(score));

		Customer customer = randomCustomer();
		assertTrue(customerDao.save(customer));

		// adicionar os documentos oa customer
		//    e a busca pelo queryset deve achar este
		assertTrue(customerDao.with(customer).add(document));
		Collection<Document> documents = customerGetDocuments(customer).all();
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
		documents = customerGetDocuments(customer).all();
		assertEquals(1, documents.size());
		document = documents.iterator().next();
		assertEquals(null,documentGetAuthor(document));

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
		documents = customerGetDocuments(customer).all();
		assertEquals(0, documents.size());

		customerDao.delete(customer);
		customer = customerDao.query(customerId().eq(getId(customer)))
				.first();
		assertNull(customer);
		} catch (IOException e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	@Test
	public void testInsertMultipleManyToManyEntries() {
		DAO<Author> authorDao = getDaoFactory().getDaoFor(authorClass);
		DAO<Document> documentDao = getDaoFactory().getDaoFor(documentClass);
		DAO<Customer> customerDao = getDaoFactory().getDaoFor(customerClass);

		try {
			Author author = randomAuthor();
			assertTrue(authorDao.save(author));

			Document document = randomDocument();
			assertTrue(authorDao.with(author).add(document));

			Customer customer = randomCustomer();

			// O metodo with(  ).add() deve salvar o customer que ainda
			// que ainda nao tem ID
			assertTrue(documentDao.with(document).add(customer));
			List<Document> documents = customerGetDocuments(customer).all();
			assertEquals(1, documents.size());
			assertEquals(document, documents.get(0));

			assertFalse(customerDao.with(customer).add(document));
			List<Customer> customers = documentGetCustomers(document).all();
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
			DAO<Author> authorDao = getDaoFactory().getDaoFor(authorClass);
			assertTrue(authorDao.save(author));

			Document document = randomDocument();
			DAO<Document> documentDao = getDaoFactory().getDaoFor(documentClass);
			assertTrue(documentDao.save(document));

			// Se o cache funcionar, a busca pelo mesmo autor (mesma PrimaryKey)
			//   deve retornar o mesmo objeto ja em memoria
			Author sameAuthor = authorDao.query(authorId().eq(getId(author))).first();
			assertTrue (author == sameAuthor);

			// Adiciona o "author" ao objeto "document" em cache.
			// O objeto "document" nao eh salvo, logo o banco nao muda
			documentSetAuthor(document, author);

			// Mesmo o objeto "document" nao sendo salvo apos o setAuthor()
			//   ele deve estar em cache e deve ter o campo _Author anulado
			//   apos a delecao do "author"
			// Como "document" nao foi salvo, o banco nao altera
			assertTrue(authorDao.delete(author));
			assertTrue(documentGetAuthor(document) == null);

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

			DAO<Author> authorDao = weakRefFactory.getDaoFor(authorClass);
			DAO<Document> documentDao = weakRefFactory.getDaoFor(documentClass);

			Author author = randomAuthor();
			assertTrue(authorDao.save(author));

			long authorId = getId(author);
			assertTrue(authorId != 0);
			String authorName = authorGetName(author);
			assertNotNull(authorName);

			Document document = randomDocument();
			documentSetAuthor(document, author);
			assertTrue(documentDao.save(document));

			// armazenando o ID para uma busca
			long documentId = getId(document);

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
			author = documentGetAuthor(document);
			assertNotNull(author);
			assertTrue(Proxy.isProxyClass(author.getClass()));

			assertEquals(authorId, getId(author));
			assertEquals(authorName, authorGetName(author));
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

			DAO<Author> authorDao = weakRefFactory.getDaoFor(authorClass);

			Author author = randomAuthor();
			assertTrue(authorDao.save(author));

			long authorId = getId(author);
			assertTrue(authorId != 0);
			String authorName = authorGetName(author);
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
			assertEquals(authorId, getId(author));
			assertEquals(authorName, authorGetName(author));

		} catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	@Test
	public void testCacheUpdate() {
		try {
			DAO<Author> dao = getDaoFactory().getDaoFor(authorClass);
			Author originalAuthor = randomAuthor();
			assertTrue(dao.save(originalAuthor));
			assertTrue(getId(originalAuthor) != 0);
	
			// Ao inserir um autor com dados diferentes mas
			Author otherAuthor = randomAuthor();
			// com mesmo ID
			setId(otherAuthor, getId(originalAuthor));

			String otherAuthorNewName = authorGetName(otherAuthor);
			Date otherAuthorNewCreatedAt = authorGetCreatedAt(otherAuthor);

			assertTrue(dao.save(otherAuthor));

			// O author inicial, em cache deve ser alterado
			assertEquals(originalAuthor, otherAuthor);
			assertEquals(otherAuthorNewName, authorGetName(originalAuthor));
			assertEquals(otherAuthorNewCreatedAt, authorGetCreatedAt(originalAuthor));

			Author authorCache = dao.get(getId(originalAuthor));
			assertEquals(otherAuthor, authorCache);
		}catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	@Test
	public void testUpdateCacheWithAssociation() {
		try {
			DAO<Author> authorDao = getDaoFactory().getDaoFor(authorClass);
			DAO<Document> documentDao = getDaoFactory().getDaoFor(documentClass);

			Author originalAuthor = randomAuthor();
			Author otherAuthor = randomAuthor();

			assertTrue(authorDao.save(originalAuthor));
			assertTrue(authorDao.save(otherAuthor));

			// os authors devem ter diferentes ids
			assertTrue(getId(originalAuthor) != getId(otherAuthor));

			Document originalDocument = randomDocument();
			documentSetAuthor(originalDocument, originalAuthor);
			assertTrue(documentDao.save(originalDocument));

			// o outro Document tem mesmo ID mas Author diferente
			Document otherDocument = randomDocument();
			setId(otherDocument, getId(originalDocument));
			documentSetAuthor(otherDocument, otherAuthor);

			// O Document original deve ter o Author alterado
			assertTrue(documentDao.save(otherDocument));
			assertEquals(otherDocument, originalDocument);
			assertEquals(otherAuthor, documentGetAuthor(originalDocument));

			// deve funcionar tambem ao marcar author como NULL
			// lembrando que author pode ser NULL
			documentSetAuthor(otherDocument, null);
			assertTrue(documentDao.save(otherDocument));
			assertEquals(otherDocument, originalDocument);
			assertEquals(null, documentGetAuthor(originalDocument));

		}catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	@Test
	public void testValidateVO () {
		Author author = randomAuthor();
		authorSetActive (author, true);
		authorSetName (author, null);
		authorSetCreatedAt (author, null);

		Collection<ValidationError> validationErrors = validate (author);

		boolean nameNull = false;
		boolean createdAtNull = false;

		for (ValidationError error : validationErrors) {
			if (error.getColumn ().equals (authorName()) &&
			    error.getConstraint() instanceof Constraint.NotNull)
			{
				nameNull = true;
			} else if (error.getColumn ().equals (authorCreatedAt()) &&
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

		authorSetName (author, "Nome Qualquer");
		authorSetCreatedAt (author, new Date ());

		// a lista de erros deve ser uma lista vazia (nao pode ser null)
		assertEquals (0, validate(author).size ());

		// o nome tem tamanho maximo de 79 pois eh um VARCHAR[79]
		authorSetName(author, RandomStringUtils.randomAlphanumeric(80));
		validationErrors = validate(author);
		assertEquals(1, validationErrors.size());
		ValidationError maxError = validationErrors.iterator().next();
		assertEquals(authorName(), maxError.getColumn());
		assertTrue(maxError.getConstraint() instanceof Constraint.Max);
	}

	@Test
	public void testValidateThroughDAO () {
		DAO<Author> dao = getDaoFactory().getDaoFor (authorClass);
		Author author1= randomAuthor ();
		try {
			assertTrue (dao.save (author1));
		} catch (IOException e) {
			fail ();
		}
		String author1Name = authorGetName (author1);

		Author author2;
		do {
			author2 = randomAuthor ();
		} while (authorGetName(author2) .equals (author1Name));

		Collection<ValidationError> errors = dao.validate (author2);
		assertEquals (0, errors.size ());

		authorSetName (author2, author1Name);
		errors = dao.validate (author2);
		assertEquals (1, errors.size ());

		ValidationError error = errors.iterator ().next ();
		assertTrue (error.getConstraint () instanceof Constraint.Unique);
		assertEquals (authorName(), error.getColumn ());
	}

	@Test
	public void testQuerySetCount () {
		DAO<Author> dao = getDaoFactory().getDaoFor (authorClass);

		Author author1 = randomAuthor ();
		Author author2 = randomAuthor ();
		Author author3 = randomAuthor ();

		// Dois dos autores estao com active false
		authorSetActive (author1, false);
		authorSetActive (author2, false);
		authorSetActive (author3, true);

		try {
			assertTrue (dao.save (author1));
			assertTrue (dao.save (author2));
			assertTrue (dao.save (author3));
		} catch (IOException e) {
			fail ();
		}
		// Dois dos autores devem estar com active false
		long qty = dao.query (authorActive().eq (false)).count ();
		assertEquals (2, qty);
	}

	@Test
	public void testNullQuery () {
		try {
			DAO<Customer> customerDao = getDaoFactory().getDaoFor(customerClass);
	
			Customer customerNullName = randomCustomer();
			customerSetName(customerNullName, null);
			assertTrue(customerDao.save(customerNullName));
	
			Customer customerNotNullName = randomCustomer();
			assertTrue(customerDao.save(customerNotNullName));
	
			{
				List<Customer> customersNullNameFromDb = customerDao
						.query(customerName().eq((String)null))
						.all();
				assertEquals(1, customersNullNameFromDb.size());
				assertEquals(customerNullName, customersNullNameFromDb.get(0));
			}
			{
				List<Customer> customersNotNullNameFromDb = customerDao
						.query(customerName().isNotNull())
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
			DAO<Author> authorDao = getDaoFactory().getDaoFor(authorClass);
			DAO<Document> docDao = getDaoFactory().getDaoFor(documentClass);

			Author author = randomAuthor();
			assertTrue(authorDao.save(author));
	
			Document documentAuthorNotNull = randomDocument();
			documentSetAuthor(documentAuthorNotNull, author);
			assertTrue(docDao.save(documentAuthorNotNull));

			Document documentAuthorNull = randomDocument();
			documentSetAuthor(documentAuthorNull, null);
			assertTrue(docDao.save(documentAuthorNull));

			List<Document> documentAuthorNullFromDb = docDao
					.query(documentAuthorId().eq((Long)null))
					.all();
			assertEquals(1, documentAuthorNullFromDb.size());
			assertEquals(documentAuthorNull, documentAuthorNullFromDb.get(0));

			documentAuthorNullFromDb = docDao
					.query(documentAuthorId().isNull())
					.all();
			assertEquals(1, documentAuthorNullFromDb.size());
			assertEquals(documentAuthorNull, documentAuthorNullFromDb.get(0));

			List<Document> documentAuthorNotNullFromDb = docDao
					.query(documentAuthorId().isNotNull())
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
			DAOFactory daoFactory = getDaoFactory();
			DAO<Author> authorDao = daoFactory.getDaoFor(authorClass);
			DAO<Document> documentDao = daoFactory.getDaoFor(documentClass);
			DAO<Score> scoreDao = daoFactory.getDaoFor(scoreClass);
			DAO<Customer> customerDao = daoFactory.getDaoFor(customerClass);

			@SuppressWarnings("unchecked")
			PrimaryKeyUpdater<Document> primaryKeyUpdater =
					(PrimaryKeyUpdater<Document>)documentDao;

			Author author = randomAuthor();
			assertTrue(authorDao.save(author));

			Document document = randomDocument();
			documentSetAuthor(document, author);
			assertTrue(documentDao.save(document));

			Score score1 = randomScore();
			scoreSetAuthor(score1, author);
			scoreSetDocument(score1, document);
			assertTrue(scoreDao.save(score1));

			Score score2 = randomScore();
			scoreSetAuthor(score2, author);
			scoreSetDocument(score2, document);
			assertTrue(scoreDao.save(score2));

			Customer customer1 = randomCustomer();
			assertTrue(customerDao.save(customer1));
			documentDao.with(document).add(customer1);

			Customer customer2 = randomCustomer();
			assertTrue(customerDao.save(customer2));
			documentDao.with(document).add(customer2);

			// QuerySets de antes da troca de ID
			QuerySet<Score> scoresQuery = documentGetScores(document);
			List<Score> scores = scoresQuery.all();
			QuerySet<Customer> customersQuery= documentGetCustomers(document);
			List<Customer> customers = customersQuery.all();

			assertEquals(2, scoresQuery.count());
			for (Score score : scores) {
				assertTrue(score.equals(score1) || score.equals(score2));
			}

			assertEquals(2, customersQuery.count());
			for (Customer c :customers) {
				assertTrue(c.equals(customer1) || c.equals(customer2));
			}

			long oldPk = getId(document);
			long newPk = oldPk + 2000;
			primaryKeyUpdater.updatePrimaryKey(document, newPk);

			assertEquals(newPk, getId(document));

			assertNull(documentDao.get(oldPk));

			// Os querysets antigos apontam para os ID antigo do "document"
			// nao devem haver registros
			assertEquals(0, scoresQuery.count());
			assertEquals(0, customersQuery.count());

			// ao autalizar os querysets
			scoresQuery = documentGetScores(document);
			// eles devem trazer "scores" com o "document" de ID novo
			assertEquals(2, scoresQuery.count());
			for (Score score : scores) {
				assertTrue(score.equals(score1) || score.equals(score2));
				assertEquals(newPk, getId(scoreGetDocument(score)));
			}

			// ao autalizar os querysets
			customersQuery = documentGetCustomers(document);
			// eles devem trazer "customers" com o "document" com ID novo
			assertEquals(2, customersQuery.count());
			for (Customer c :customers) {
				assertTrue(c.equals(customer1) || c.equals(customer2));
				List<Document> documents = customerGetDocuments(c).all();
				assertEquals(1, documents.size());
				Document customerDocument = documents.get(0);
				assertEquals(newPk, getId(customerDocument));
			}

		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
