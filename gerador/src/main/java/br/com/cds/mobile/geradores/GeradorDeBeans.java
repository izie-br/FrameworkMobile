package br.com.cds.mobile.geradores;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

import br.com.cds.mobile.geradores.dao.CodeModelDaoFactory;
import br.com.cds.mobile.geradores.filters.CamelCaseFilter;
import br.com.cds.mobile.geradores.filters.PrefixoTabelaFilter;
import br.com.cds.mobile.geradores.filters.associacao.AssociacaoPorNomeFilter;
import br.com.cds.mobile.geradores.javabean.JavaBeanSchema;
import br.com.cds.mobile.geradores.javabean.Propriedade;
import br.com.cds.mobile.geradores.json.CodeModelJsonSerializacaoFactory;
import br.com.cds.mobile.geradores.sqlparser.SqlTabelaSchemaFactory;
import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

public class GeradorDeBeans {

	private static final String DB_CLASS = "DB";
	private static final String DB_RESOURCE_FILE = "/DB.java";
	private static final String DB_PACKAGE = "db";

	private static final String CUSTOM_SRC_PACKAGES_CLASSES_RESOURCES[][] ={
		{DB_PACKAGE,	DB_CLASS,	DB_RESOURCE_FILE},
		{"",	"Aplicacao",	"/Aplicacao.java"},
		{"",	"Constantes",	"/Constantes.java"},
		{"",	"GenericBean",	"/GenericBean.java"}
	};

	public static PrintStream out = System.out;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// exemploDeUsoDoCodeModel();
		// exemploDeUsoJSqlParser();

		if(args==null||args.length<3){
			System.out.println(
					"Uso:\n"+
					"java -classpath <JARS> " +
					GeradorDeBeans.class.getName()+ " " +
					"arquivo_sql pacote pastaSrc"
			);
			return;
		}

		String arquivo = args[0];
		String pacote = args[1];
		String pastaSrc = args[2];

		try {
			gerarBeansWithJsqlparserAndCodeModel(
				pacote, new FileReader(new File(arquivo)),
				pastaSrc, pacote+".gen"
			);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void conferirArquivosCustomSrc(
			String pacote,
			String pastaSrc
	){
		for(
			String pacoteClassResource [] :
			CUSTOM_SRC_PACKAGES_CLASSES_RESOURCES
		){
			String pacoteClass =
				pacote + '.' +
				pacoteClassResource[0];
			String pacotePath = getPacotePath(pacoteClass);
			File folder = new File(pastaSrc + "/" + pacotePath);
			if(!folder.exists())
				folder.mkdirs();
			File f = new File(
				folder,
				pacoteClassResource[1] + ".java"
			);
			if(!f.exists()){
				out.println(
					"Criando arquivo " + f.getPath()
				);
				try{
					gerarClasseCustomSrc(
						pacoteClass,
						f,
						pacoteClassResource[2]
					);
				} catch (IOException e ) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public static void gerarClasseCustomSrc(
			String pacote,
			File classFile,
			String classResource
	)
			throws IOException
	{
		classFile.createNewFile();
		InputStream is = GeradorDeBeans.class
			.getResourceAsStream(classResource);
		if(is == null)
			throw new RuntimeException(
				"Resource " + classResource + " nao encontrado"
			);
		classFile.createNewFile();
		OutputStream os = new FileOutputStream(classFile);
		if(pacote.charAt(pacote.length()-1)=='.')
			pacote = pacote.substring(0,pacote.length()-1);
		os.write( ("package " + pacote + ";\n\n").getBytes("ASCII"));
		byte buffer [] = new byte[255];
		for(
			int charsRead = is.read(buffer);
			charsRead >= 0;
			charsRead = is.read(buffer)
		){
			os.write(buffer,0,charsRead);
		}
		os.close();
		buffer = null;
	}

	public static void gerarBeansWithJsqlparserAndCodeModel(
			String pacote,
			Reader sql,
			String pastaSrc,
			String pacoteGen
	)
			throws IOException, FileNotFoundException
	{
		conferirArquivosCustomSrc(pacote, pastaSrc);
		String pacoteDb = getPacoteDb(pacote);
		String dbClass = pacoteDb + DB_CLASS;
		String dbStaticMethod = "getDb";

		Collection<TabelaSchema> tabelasBanco =
				getTabelasDoSchema(sql);

		JavaBeanSchema.Factory factory = new JavaBeanSchema.Factory();
		factory.addFiltroFactory(
			new PrefixoTabelaFilter.Factory("tb_"));
		factory.addFiltroFactory(new AssociacaoPorNomeFilter.Factory(
				"{COLUMN=id}_{TABLE}"
		));
		factory.addFiltroFactory(
			new CamelCaseFilter.Factory());

		// gerando os JavaBeanSchemas
		Collection<JavaBeanSchema> javaBeanSchemas =
			new ArrayList<JavaBeanSchema>();
		for(TabelaSchema tabela : tabelasBanco)
			javaBeanSchemas.add(
				factory.javaBeanSchemaParaTabela(tabela));

		JCodeModel jcm = new JCodeModel();
		CodeModelBeanFactory jbf = new CodeModelBeanFactory(jcm);
		CodeModelDaoFactory daoFactory =
			new CodeModelDaoFactory(jcm,dbClass,dbStaticMethod);
		CodeModelJsonSerializacaoFactory jsonFactory =
			new CodeModelJsonSerializacaoFactory(jcm);

		ArrayList<SchemaXJClass> listClasses =
			new ArrayList<GeradorDeBeans.SchemaXJClass>();
		for(JavaBeanSchema javaBeanSchema : javaBeanSchemas){
			if( javaBeanSchema.isNonEntityTable())
				continue;
			JDefinedClass classeGerada;
			try {
				classeGerada = jbf.gerarClasse(
					pacoteGen+'.'+javaBeanSchema.getNome());
			} catch (JClassAlreadyExistsException e) {
				throw new RuntimeException(e);
			}
			jbf.gerarConstantes(classeGerada, javaBeanSchema);
			for(String coluna : javaBeanSchema.getColunas()){
				Propriedade p =
					javaBeanSchema.getPropriedade(coluna);
				if(p!=null)
					jbf.gerarPropriedade(classeGerada,p);
			}
			jsonFactory.gerarMetodosDeSerializacaoJson(
					classeGerada,
					javaBeanSchema
			);
			SchemaXJClass schemaXjclass= new SchemaXJClass();
			schemaXjclass.klass = classeGerada;
			schemaXjclass.schema = javaBeanSchema;
			listClasses.add(schemaXjclass);
		}

		// gera metodos de acesso a banco e ralacoes
		for(SchemaXJClass schemaJclass : listClasses){
			daoFactory.gerarAcessoDB(
					schemaJclass.klass,
					schemaJclass.schema
			);
			for(SchemaXJClass schemaJclassAssoc : listClasses){
				// gerando relacoes
				daoFactory.gerarRelacoes(
						schemaJclass.klass,
						schemaJclass.schema,
						schemaJclassAssoc.klass,
						schemaJclassAssoc.schema
				);
			}
		}

		for(SchemaXJClass schemaXJClass : listClasses){
			jbf.gerarSerialVersionUID(schemaXJClass.klass);
			jbf.gerarMetodoClone(
					schemaXJClass.klass,
					schemaXJClass.schema
			);
			jbf.gerarHashCodeAndEquals(
					schemaXJClass.klass,
					schemaXJClass.schema
			);
		}

		String pastaGen = pacoteGen.replaceAll("\\.", File.separator);
		File pastaGenFolder = new File(pastaSrc, pastaGen);
		if(pastaGenFolder.exists()){
			System.out.println("Deletando " + pastaGenFolder.getAbsolutePath());
			deleteFolderR(pastaGenFolder);
		} else {
			System.out.println(
				"Pasta " +
				pastaGenFolder.getAbsolutePath() +
				"nao encontrada"
			);
		}

		//pastaGenFolder.mkdirs();
		jcm.build(new File(pastaSrc));
	}

	protected static String getPacoteDb(String pacote) {
		return pacote+".db.";
	}

	protected static String getPacotePath(String pacote) {
		return pacote.replaceAll("\\.", File.separator);
	}

	public static Collection<TabelaSchema> getTabelasDoSchema(Reader input)
			throws IOException
	{
		BufferedReader reader = new BufferedReader(input);
		Collection<TabelaSchema> tabelas =
			new ArrayList<TabelaSchema>();
		SqlTabelaSchemaFactory factory = new SqlTabelaSchemaFactory();
		for(;;){
			StringBuilder sb = new StringBuilder();
			int c;
			for(;;){
				c = reader.read();
				if(c<0)
					break;
				sb.append((char)c);
				if(c==';')
					break;
			}
			String createTableStatement = sb.toString();
			if(
				Pattern.compile(
					"create\\s+view",
					Pattern.CASE_INSENSITIVE |
					Pattern.MULTILINE
				).matcher(createTableStatement).find()
			)
				continue;
			if(
				createTableStatement==null ||
				createTableStatement.matches("^[\\s\\n]*$")
			){
				break;
			}
			TabelaSchema tabela =
				factory.gerarTabelaSchema(createTableStatement);
			tabelas.add(tabela);
			System.out.println("tabela: " +tabela);
		}
		return tabelas;
	}

	private static class SchemaXJClass{
		JDefinedClass klass;
		JavaBeanSchema schema;
	}

	private static void deleteFolderR(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				deleteFolderR(c);
		}
		if (!f.delete()){
			throw new FileNotFoundException(
				"Failed to delete file: " + f
			);
		}
	}

	/***********************************
	 * Exemplos de uso das bibliotecas *
	 *     jCodeModel e jSqlParser     *
	 **********************************/


	/**
	 * exemplo de uso do codemodel
	 * @throws JClassAlreadyExistsException
	 * @throws IOException
	 */
	public static void exemploDeUsoDoCodeModel()
			throws JClassAlreadyExistsException, IOException {
		JCodeModel jcm = new JCodeModel();
		//classe
		JDefinedClass klass = jcm._class(JMod.PUBLIC,"br.com.cds.mobile.flora.eb.ClienteBean",ClassType.CLASS);
		JDocComment comment = klass.javadoc();
		comment.add("Classe gerada automaticamente");
		// public BigDecimal precoFromString(String precoString)
		JMethod metodo = klass.method(JMod.PUBLIC, BigDecimal.class, "precoFromString");
		comment = metodo.javadoc();
		comment.add("Comentario do metodo");
		JVar var = metodo.param(jcm._ref(String.class), "precoString");
		//escrevendo o corpo
		JBlock blocoMetodo = metodo.body();
		// if(precoString==null||precoString.equals(""))
		JConditional cond = blocoMetodo._if(var.eq(JExpr._null()).cor(var.invoke("trim").invoke("equals").arg("")));
		// precoString = new String("0")
		cond._then().assign(var, JExpr._new(jcm._ref(String.class)).arg("0"));
		// else
		// System.out.print("OK")
		cond._else().invoke(jcm.ref(System.class).staticRef("out"),"print").arg("OK");
		// return new BigDecimal(precoString)
		blocoMetodo._return(JExpr._new(jcm.ref(BigDecimal.class)).arg(var));
		jcm.build(new File("customGen"));
	}

	public static void exemploDeUsoJSqlParser(){
		try {
			CCJSqlParserManager manager = new CCJSqlParserManager();
			
			BufferedReader reader = new BufferedReader(new FileReader("script/schema.sql"));
			for(;;){
				String stringStm = reader.readLine();
				if(stringStm==null)
					break;
				Statement stm = manager.parse(new StringReader(stringStm));
				stm.accept(new StatementVisitor() {
					
					@Override
					public void visit(CreateTable ct) {
						System.out.print("create ");
						System.out.println(ct.getTable().getName());
						for(Object obj :ct.getColumnDefinitions())
							System.out.println( ((ColumnDefinition)obj).getColumnName()+"  "+ ((ColumnDefinition)obj).getColDataType().getDataType() );
						System.out.println("-------------");
					}
					
					@Override public void visit(Truncate arg0) {}
					@Override public void visit(Drop arg0) {}
					@Override public void visit(Replace arg0) {}
					@Override public void visit(Insert arg0) {}
					@Override public void visit(Update arg0) {}
					@Override public void visit(Delete arg0) {}
					@Override public void visit(Select arg0) {}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
