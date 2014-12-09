package com.quantium.mobile.framework.test.gerador;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;

import android.test.ActivityInstrumentationTestCase2;

import com.quantium.mobile.framework.DAO;
import com.quantium.mobile.framework.DAOFactory;
import com.quantium.mobile.framework.PrimaryKeyUpdater;
import com.quantium.mobile.framework.Save;
import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.test.SessionFacade;
import com.quantium.mobile.framework.test.TestActivity;
import com.quantium.mobile.framework.test.utils.Utils;
import com.quantium.mobile.framework.test.vo.Author;
import com.quantium.mobile.framework.test.gen.AuthorEditable;
import com.quantium.mobile.framework.test.vo.AuthorImpl;
import com.quantium.mobile.framework.test.vo.Customer;
import com.quantium.mobile.framework.test.document.gen.DocumentEditable;
import com.quantium.mobile.framework.test.document.vo.Document;
import com.quantium.mobile.framework.test.vo.Score;
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
			List<Score> objects = facade.query(Score.class).all();
			for (Score obj :objects) {
				facade.delete(obj, true);
			}
		}
		{
			List<Customer> objects = facade.query(Customer.class).all();
			for (Customer obj :objects) {
				facade.delete(obj, true);
			}
		}
		{
			List<Document> objects = facade.query(Document.class).all();
			for (Document obj :objects) {
				facade.delete(obj, true);
			}
		}
		{
			List<Author> objects = facade.query(Author.class).all();
			for (Author obj :objects) {
				facade.delete(obj, true);
			}
		}
	}

	public void testInsertUpdateDelete(){
		try {
			int count = facade.query(Author.class).all().size();
			Author author = Utils.randomAuthor();
			assertTrue(facade.save(author));
			String id  = author.getId();
			assertTrue(id!=null);
			author.setName(RandomStringUtils.random(40));
			assertTrue(facade.save(author));
			assertEquals(id, author.getId());
			assertTrue(facade.delete(author));
			int countAfter = facade.query(Author.class).all().size();
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
		Author authors[]  = new Author[count];
		// inserindo 5 authors diferentes
		for(int i=0;i<count;i++){
			Author author = Utils.randomAuthor();
			// para garantir strings diferentes, o comprimento varia com "i"
			author.setName(RandomStringUtils.random(20+i));
			author.setActive(true);
			try {
				assertTrue(facade.save(author));
				assertTrue(author.getId()!=null);
			} catch (IOException e){
				fail(StringUtil.getStackTrace(e));
			}
			authors[i] = author;
		}
		// buscando no banco e conferindo se a quantidadee eh igual ou supperior
		// aos inseridos
		Collection<Author> authorsFromDb = facade.query(Author.class).all();
		assertTrue(authorsFromDb.size()>=authors.length);
		for(Author author : authors){
			assertTrue( authorsFromDb.contains(author) );
		}
		// buscando um a um por nome e conferindo se eh encontrado
		for(Author author : authors){
			Author authorFromDb = facade.query(Author.class,
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

	public void testGetById () {
		Author author1 = Utils.randomAuthor ();
		Author author2 = Utils.randomAuthor ();

		try{
			assertTrue (facade.save (author1));
			assertTrue (facade.save (author2));
		} catch (IOException e) {
			fail ();
		}

		String id1 = author1.getId ();
		Author author1FromDb = facade.get (Author.class, id1);
		assertEquals (author1, author1FromDb);

		String id2 = author2.getId ();
		Author author2FromDb = facade.get (Author.class, id2);
		assertEquals (author2, author2FromDb);
	}

	public void testMapSerialization(){
		Author author = Utils.randomAuthor();
		try {
		assertTrue(facade.save(author));
		assertTrue(author.getId()!=null);
		} catch (IOException e){
			fail(StringUtil.getStackTrace(e));
		}
		Map<String, Object> map = author.toMap();
		assertNotNull(map);
		Author authorDesserialized =
				facade.mapToObject(Author.class, map);
		assertEquals(author, authorDesserialized);
		Author authorIdNull = Utils.randomAuthor();
		Map<String, Object> mapIdNull = authorIdNull.toMap();
		Author authorIdNullDesserialized =
				facade.mapToObject(Author.class, mapIdNull);
		assertEquals(authorIdNull, authorIdNullDesserialized);
		// A classe document tem um campo date mais "desafiador"
		Document doc = Utils.randomDocument();
		map = doc.toMap();
		assertNotNull(map);
		Document docDesserialized = facade
			.mapToObject(Document.class, map);
		assertEquals(doc, docDesserialized);
	}

	public void testUpdateWithMap() {
		try {
			Author author1= Utils.randomAuthor();
			assertTrue(facade.save(author1));

			Author author2 = Utils.randomAuthor();
			assertFalse(author2.equals(author1));

			Date author2CreatedAt = author2.getCreatedAt();
			boolean author2Active = author2.isActive();

			//
			// atualizar somente o nome
			//

			//o nome de author2 nao pode ser NULL, inicialmente
			assertNotNull(author2.getName());

			Map<String,Object> mapWithNameOnly = new HashMap<String, Object>();
			// adicionando o nome NULL no Map
			// eh importante perceber a diferenca de:
			//   + Map com { "name" = NULL }
			//     -  map.containsKey("name") == TRUE;
			//     -  map.get("name") == NULL;
			//   + Map sem chave "name"
			//     -  map.containsKey("name") == FALSE;
			//     -  map.get("name") == NULL;
			mapWithNameOnly.put(Author.NAME.getName(), null);

			// ao rodar updateWithMap
			facade.updateWithMap(author2, mapWithNameOnly);
			// entao, o nome deve ter sido alterado para null
			assertEquals(null, author2.getName());
			// e os outros campos nao
			assertEquals(author2CreatedAt, author2.getCreatedAt());
			assertEquals(author2Active, author2.isActive());

			// ao usar um outro nome
			mapWithNameOnly.put(Author.NAME.getName(), author1.getName());
			facade.updateWithMap(author2, mapWithNameOnly);
			// o nome deve ter sido alterado
			assertEquals(author1.getName(), author2.getName());

			//
			// autualizando tudo menos o nome
			//
			author2.setName("test");
			// Seja um Map populado com os campos de author1
			Map<String,Object> mapWithoutName = author1.toMap();
			assertTrue(mapWithoutName.containsKey(Author.NAME.getName()));
			// mas sem o nome
			mapWithoutName.remove(Author.NAME.getName());

			facade.updateWithMap(author2, mapWithoutName);
			assertEquals("test", author2.getName());
			// e os outros campos devem ter sido alterados
			assertEquals(author1.getCreatedAt(), author2.getCreatedAt());
			assertEquals(author1.isActive(), author2.isActive());
		} catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	public void testUpdateAssociationWithMap() {
		try {
			Author author1= Utils.randomAuthor();
			assertTrue(facade.save(author1));

			Author author2 = Utils.randomAuthor();
			assertTrue(facade.save(author2));

			Document document1 = Utils.randomDocument();
			document1.setAuthor(author1);
			assertTrue(facade.save(document1));

			Document document2 = Utils.randomDocument();
			document2.setAuthor(author2);
			assertTrue(facade.save(document2));

			Map<String,Object> doc2map = document2.toMap();

			facade.updateWithMap(document1, doc2map);

			// todos os campos e a associacao deve ter sido alteradas
			assertEquals(author2, document1.getAuthor());
			assertEquals(document2.getTitle(), document1.getTitle());
			assertEquals(document2.getText(), document1.getText());
			assertEquals(document2.getCreatedAt(), document1.getCreatedAt());
			// Apenas o Id deve permanece inalterado
			assertFalse(document2.getId() == document1.getId());

		} catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}
	public void testSerializationAlias(){
		final String idDocumentAlias = "id_document";
		final String idDocument = "9";
		final String createdAlias = "created";
		final Date created = new Date(99, 0, 1, 1, 0, 0);
		/*
		 * testando document, com dois alias "id_document" e "created"
		 */
		Map<String,Object> map = new HashMap<String, Object>();
		// "idDocmuent" eh o alias de "document.id"
		// ver no pom.xml
		map.put(idDocumentAlias, idDocument);
		// "created" eh o alias de "created_at" para todas classes
		map.put(createdAlias, created);
		// testando mapToObject
		Document document = facade.mapToObject(Document.class, map);
		assertEquals(idDocument, document.getId());
		assertEquals(created, document.getCreatedAt());
		// testando toMap
		Map<String,Object> documentToMap = document.toMap();
		assertEquals(idDocument, documentToMap.get(idDocumentAlias));
		assertEquals(created, documentToMap.get(createdAlias));
		/*
		 *  testando author com 1 alias "created"
		 */
		// "created" eh o alias de "created_at" para todas classes
		Map<String,Object> authorMap = new HashMap<String, Object>();
		authorMap.put(createdAlias, created);
		// testando mapToObject
		Author author = facade.mapToObject(Author.class, authorMap);
		assertEquals(created, author.getCreatedAt());
		// testando toMap
		Map<String,Object> authorToMap = author.toMap();
		assertEquals(created, authorToMap.get(createdAlias));
	}

	public void testSaveInsertIfNotExists () {
		Author author = Utils.randomAuthor ();
		final String id = "5";
		((AuthorEditable)author).setId (id);

		try{
			assertTrue (facade.save (Utils.randomAuthor ()));
			assertTrue (facade.save (Utils.randomAuthor ()));
			assertTrue (facade.save (Utils.randomAuthor ()));
			assertTrue (facade.save (author));
		} catch (IOException e) {
			fail ();
		}

		assertEquals (id, author.getId ());

		Author authorFromDb = facade.get (Author.class, id);
		assertEquals (author, authorFromDb);
	}

	public void testDeleteCascade () {
		try {
		Author author = Utils.randomAuthor();
		assertTrue(facade.save(author));

		Document document = Utils.randomDocument();
		document.setAuthor(author);
		assertTrue(facade.save(document));

		Score score = Utils.randomScore();
		score.setAuthor(author);
		score.setDocument(document);
		assertTrue(facade.save(score));

		Customer customer = Utils.randomCustomer();
		assertTrue(facade.save(customer));

		// adicionar os documentos oa customer
		//    e a busca pelo queryset deve achar este
		assertTrue(facade.withAdd(customer,document));
		Collection<Document> documents = customer.getCustomerDocuments().all();
		assertEquals(document, documents.iterator().next());

		// A author, ao ser "deletado" deve desaparecer do banco
		assertTrue(facade.delete(author, true));
		Author authorFromDb = facade.query(Author.class).first();
		assertNull(authorFromDb);

		// Score tem relacao many-to-one para document e author
		//    ambas com chave NOT NULL
		//    e deve ser removida ao remover o author (veja acima)
		Collection<Score> scoresDb = facade.query(Score.class)
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

		facade.delete(document, true);
		documents = customer.getCustomerDocuments().all();
		assertEquals(0, documents.size());

		facade.delete(customer, true);
		customer = facade.query(Customer.class, Customer.ID.eq(customer.getId()))
				.first();
		assertNull(customer);
		} catch (IOException e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	public void testInsertMultipleManyToManyEntries() {
		try {
			Author author = Utils.randomAuthor();
			assertTrue(facade.save(author));

			Document document = Utils.randomDocument();
			assertTrue(facade.withAdd(author,document));

			Customer customer = Utils.randomCustomer();

			// O metodo with(  ,) deve salvar o customer que ainda
			// que ainda nao tem ID
			assertTrue(facade.withAdd(document,customer));
			List<Document> documents = customer.getCustomerDocuments().all();
			assertEquals(1, documents.size());
			assertEquals(document, documents.get(0));

			assertTrue(facade.withAdd(customer,document));
			List<Customer> customers = document.getCustomerCustomers().all();
			assertEquals(1, customers.size());
			assertEquals(customer, customers.get(0));
		} catch (IOException e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	public void testFirstLevelCache() {
		try {
			Author author = Utils.randomAuthor();
			assertTrue(facade.save(author));

			Document document = Utils.randomDocument();
			assertTrue(facade.save(document));

			// Se o cache funcionar, a busca pelo mesmo autor (mesma PrimaryKey)
			//   deve retornar o mesmo objeto ja em memoria
			Author sameAuthor = facade.query(Author.class, Author.ID.eq(author.getId())).first();
			assertTrue (author == sameAuthor);

			// Adiciona o "author" ao objeto "document" em cache.
			// O objeto "document" nao eh salvo, logo o banco nao muda
			document.setAuthor(author);

			// Mesmo o objeto "document" nao sendo salvo apos o setAuthor()
			//   ele deve estar em cache e deve ter o campo _Author anulado
			//   apos a delecao do "author"
			// Como "document" nao foi salvo, o banco nao altera
			assertTrue(facade.delete(author, true));
			assertTrue(document.getAuthor() == null);

			assertTrue(facade.delete(document));
		} catch (IOException e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

/*
	public void testLazyInvocationHandler() {
		try {
			DAO<Author> authorDao = getDaoFactory().getDaoFor(Author.class);
			DAO<Document> documentDao = getDaoFactory().getDaoFor(Document.class);

			Author author = Utils.randomAuthor();
			assertTrue(facade.save(author));

			long authorId = author.getId();
			assertTrue(authorId != null);
			String authorName = author.getName();
			assertNotNull(authorName);

			Document document = Utils.randomDocument();
			document.setAuthor(author);
			assertTrue(facade.save(document));

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
			// O GC do android precisa de um "hackzinho" para rodar
			// nao isole esta parte abaixo em um metodo,
			// se nao vira loop infinito
			List<WeakReference<byte[]>> listStr = new ArrayList<WeakReference<byte[]>>();
			int i = 1;
			while (authorWeakRef.get() != null) {
				System.gc();
				// Use uma sequencia de fibonacci para evitar
				// estouro de memoria
				int size = fib(i++)*1000000;
				listStr.add(new WeakReference<byte[]>(new byte[size]));
			}
			listStr= null;

			// Buscando novamente o document
			document = facade.get(documentId);
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

	public void testFirstLevelCacheTrim() {
		try {
			DAO<Author> authorDao = getDaoFactory().getDaoFor(Author.class);

			Author author = Utils.randomAuthor();
			assertTrue(facade.save(author));

			long authorId = author.getId();
			assertTrue(authorId != null);
			String authorName = author.getName();
			assertNotNull(authorName);

			// Usando o objeto Author para monitorar o gc
			WeakReference<Author> authorWeakRef =
					new WeakReference<Author>(author);
			// Removendo todas referencias fortes ao Author
			author = null;

			// Quando esta referencia fraca se tornar null
			// significa que o GC rodou!
			// O GC do android precisa de um "hackzinho" para rodar
			// nao isole esta parte abaixo em um metodo,
			// se nao vira loop infinito
			List<WeakReference<byte[]>> listStr = new ArrayList<WeakReference<byte[]>>();
			int i = 1;
			while (authorWeakRef.get() != null) {
				System.gc();
				// Use uma sequencia de fibonacci para evitar
				// estouro de memoria
				int size = fib(i++)*1000000;
				listStr.add(new WeakReference<byte[]>(new byte[size]));
			}
			listStr= null;

			// Nesta implementacal o dao herda do FirstLevelCache
			FirstLevelCache cache = (FirstLevelCache)getDaoFactory();
			// deve rodar sem excecao
			cache.trim();

			author = facade.get(authorId);
			assertEquals(authorId, author.getId());
			assertEquals(authorName, author.getName());

		} catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}
*/

	public void testCacheUpdate() {
		try {
			Author originalAuthor = Utils.randomAuthor();
			assertTrue(facade.save(originalAuthor));
			assertTrue(originalAuthor.getId() != null);
	
			// Ao inserir um autor com dados diferentes mas
			AuthorEditable otherAuthor = (AuthorEditable)Utils.randomAuthor();
			// com mesmo ID
			otherAuthor.setId(originalAuthor.getId());

			String otherAuthorNewName = otherAuthor.getName();
			Date otherAuthorNewCreatedAt = otherAuthor.getCreatedAt();

			assertTrue(facade.save(otherAuthor));

			// O author inicial, em cache deve ser alterado
			assertEquals(originalAuthor, otherAuthor);
			assertEquals(otherAuthorNewName, originalAuthor.getName());
			assertEquals(otherAuthorNewCreatedAt, originalAuthor.getCreatedAt());

			Author authorCache = facade.get(Author.class, originalAuthor.getId());
			assertEquals(otherAuthor, authorCache);
		}catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	public void testUpdateCacheWithAssociation() {
		try {
			Author originalAuthor = Utils.randomAuthor();
			Author otherAuthor = Utils.randomAuthor();

			assertTrue(facade.save(originalAuthor));
			assertTrue(facade.save(otherAuthor));

			// os authors devem ter diferentes ids
			assertTrue(originalAuthor.getId() != otherAuthor.getId());

			Document originalDocument = Utils.randomDocument();
			originalDocument.setAuthor(originalAuthor);
			assertTrue(facade.save(originalDocument));

			// o outro Document tem mesmo ID mas Author diferente
			Document otherDocument = Utils.randomDocument();
			((DocumentEditable)otherDocument).setId(originalDocument.getId());
			otherDocument.setAuthor(otherAuthor);

			// O Document original deve ter o Author alterado
			assertTrue(facade.save(otherDocument));
			assertEquals(otherDocument, originalDocument);
			assertEquals(otherAuthor, originalDocument.getAuthor());

			// deve funcionar tambem ao marcar author como NULL
			// lembrando que author pode ser NULL
			otherDocument.setAuthor(null);
			assertTrue(facade.save(otherDocument));
			assertEquals(otherDocument, originalDocument);
			assertEquals(null, originalDocument.getAuthor());

		}catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	public void testValidateVO () {
		Author author = Utils.randomAuthor();
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

	public void testValidateThroughDAO () throws IOException {
		Author author1= Utils.randomAuthor ();
		try {
			assertTrue (facade.save (author1));
		} catch (IOException e) {
			fail ();
		}
		String author1Name = author1.getName ();

		Author author2;
		do {
			author2 = Utils.randomAuthor ();
		} while (author2.getName ().equals (author1Name));

		Collection<ValidationError> errors = facade.validate (author2);
		assertEquals (0, errors.size ());

		author2.setName (author1Name);
		errors = facade.validate (author2);
		assertEquals (1, errors.size ());

		ValidationError error = errors.iterator ().next ();
		assertTrue (error.getConstraint () instanceof Constraint.Unique);
		assertEquals (Author.NAME, error.getColumn ());
	}

	public void testQuerySetCount () {
		Author author1 = Utils.randomAuthor ();
		Author author2 = Utils.randomAuthor ();
		Author author3 = Utils.randomAuthor ();

		// Dois dos autores estao com active false
		author1.setActive (false);
		author2.setActive (false);
		author3.setActive (true);

		try {
			assertTrue (facade.save (author1));
			assertTrue (facade.save (author2));
			assertTrue (facade.save (author3));
		} catch (IOException e) {
			fail ();
		}
		// Dois dos autores devem estar com active false
		long qty = facade.query (Author.class, Author.ACTIVE.eq (false)).count ();
		assertEquals (2, qty);
	}

	public void testNullQuery () {
		try {
			Customer customerNullName = Utils.randomCustomer();
			customerNullName.setName(null);
			assertTrue(facade.save(customerNullName));
	
			Customer customerNotNullName = Utils.randomCustomer();
			assertTrue(facade.save(customerNotNullName));
	
			{
				List<Customer> customersNullNameFromDb = facade
						.query(Customer.class, Customer.NAME.eq((String)null))
						.all();
				assertEquals(1, customersNullNameFromDb.size());
				assertEquals(customerNullName, customersNullNameFromDb.get(0));
			}
			{
				List<Customer> customersNotNullNameFromDb = facade
						.query(Customer.class, Customer.NAME.isNotNull())
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
			Author author = Utils.randomAuthor();
			assertTrue(facade.save(author));
	
			Document documentAuthorNotNull = Utils.randomDocument();
			documentAuthorNotNull.setAuthor(author);
			assertTrue(facade.save(documentAuthorNotNull));

			Document documentAuthorNull = Utils.randomDocument();
			documentAuthorNull.setAuthor(null);
			assertTrue(facade.save(documentAuthorNull));

			List<Document> documentAuthorNullFromDb = facade
					.query(Document.class, Document.ID_AUTHOR.eq((String)null))
					.all();
			assertEquals(1, documentAuthorNullFromDb.size());
			assertEquals(documentAuthorNull, documentAuthorNullFromDb.get(0));

			documentAuthorNullFromDb = facade
					.query(Document.class, Document.ID_AUTHOR.isNull())
					.all();
			assertEquals(1, documentAuthorNullFromDb.size());
			assertEquals(documentAuthorNull, documentAuthorNullFromDb.get(0));

			List<Document> documentAuthorNotNullFromDb = facade
					.query(Document.class, Document.ID_AUTHOR.isNotNull())
					.all();
			assertEquals(1, documentAuthorNotNullFromDb.size());
			assertEquals(documentAuthorNotNull, documentAuthorNotNullFromDb.get(0));

		} catch (IOException e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	public void testUpdatePrimaryKey() {
		try {
			@SuppressWarnings("unchecked")

			Author author = Utils.randomAuthor();
			assertTrue(facade.save(author));

			Document document = Utils.randomDocument();
			document.setAuthor(author);
			assertTrue(facade.save(document));

			Score score1 = Utils.randomScore();
			score1.setAuthor(author);
			score1.setDocument(document);
			assertTrue(facade.save(score1));

			Score score2 = Utils.randomScore();
			score2.setAuthor(author);
			score2.setDocument(document);
			assertTrue(facade.save(score2));

			Customer customer1 = Utils.randomCustomer();
			assertTrue(facade.save(customer1));
			facade.withAdd(document,customer1);

			Customer customer2 = Utils.randomCustomer();
			assertTrue(facade.save(customer2));
			facade.withAdd(document,customer2);

			// QuerySets de antes da troca de ID
			QuerySet<Score> scoresQuery = document.getDocumentScores();
			List<Score> scores = scoresQuery.all();
			QuerySet<Customer> customersQuery= document.getCustomerCustomers();
			List<Customer> customers = customersQuery.all();

			assertEquals(2, scoresQuery.count());
			for (Score score : scores) {
				assertTrue(score.equals(score1) || score.equals(score2));
			}

			assertEquals(2, customersQuery.count());
			for (Customer c :customers) {
				assertTrue(c.equals(customer1) || c.equals(customer2));
			}

			String oldPk = document.getId();
			String newPk = String.valueOf(Long.parseLong(oldPk) + 2000);
			facade.updatePrimaryKey(document, newPk);

			assertEquals(newPk, document.getId());

			assertNull(facade.get(Document.class, oldPk));

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
			customersQuery = document.getCustomerCustomers();
			// eles devem trazer "customers" com o "document" com ID novo
			assertEquals(2, customersQuery.count());
			for (Customer c :customers) {
				assertTrue(c.equals(customer1) || c.equals(customer2));
				List<Document> documents = c.getCustomerDocuments().all();
				assertEquals(1, documents.size());
				Document customerDocument = documents.get(0);
				assertEquals(newPk, customerDocument.getId());
			}

		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	public void testMinMaxIntValidation () {
		try {
			Author author = Utils.randomAuthor();
			assertTrue(facade.save(author));

			Document document = Utils.randomDocument();
			document.setAuthor(author);
			assertTrue(facade.save(document));

			Score score = Utils.randomScore();
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

			assertTrue(facade.delete(document));
			assertTrue(facade.delete(author));
		}catch (Exception e) {
			fail(StringUtil.getStackTrace(e));
		}
	}

	public void testMinMaxStringValidation() {
		Collection<ValidationError> errors;
		ValidationError error;

		Author author = Utils.randomAuthor();
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

		Customer customer = Utils.randomCustomer();
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

/*
	private static int fib(int n) {
		if (n < 2) {
			return n;
		} else {
			return fib(n - 1) + fib(n - 2);
		}
	}
*/
}
