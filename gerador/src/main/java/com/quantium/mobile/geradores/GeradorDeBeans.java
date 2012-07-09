package com.quantium.mobile.geradores;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
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


import com.quantium.mobile.geradores.dao.CodeModelDaoFactory;
import com.quantium.mobile.geradores.filters.CamelCaseFilter;
import com.quantium.mobile.geradores.filters.PrefixoTabelaFilter;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoPorNomeFilter;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Propriedade;
import com.quantium.mobile.geradores.json.CodeModelJsonSerializacaoFactory;
import com.quantium.mobile.geradores.sqlparser.SqlTabelaSchemaFactory;
import com.quantium.mobile.geradores.tabelaschema.TabelaSchema;
import com.quantium.mobile.geradores.util.LoggerUtil;
import com.quantium.mobile.geradores.util.SQLiteGeradorUtils;
import com.quantium.mobile.geradores.util.XMLUtil;
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

	public static final String DB_CLASS = "DB";
	public static final String DB_RESOURCE_FILE = "/DB.java";
	public static final String DB_PACKAGE = "db";
	public static final String GENERIC_BEAN_CLASS = "GenericBean";
	public static final String GENERIC_BEAN_PACKAGE = null;

	private static final String CUSTOM_SRC_PACKAGES_CLASSES_RESOURCES[][] ={
		{DB_PACKAGE,	DB_CLASS,	DB_RESOURCE_FILE},
		{"",	"Aplicacao",	"/Aplicacao.java"},
		{"",	"Constantes",	"/Constantes.java"},
		{"",	GENERIC_BEAN_CLASS,	"/GenericBean.java"}
	};


	/**
	 * @param args
	 */
	public static void main(String[] args) throws GeradorException{
		// exemploDeUsoDoCodeModel();
		// exemploDeUsoJSqlParser();

		if(args==null||args.length<3){
			LoggerUtil.getLog().info(
					"Uso:\n"+
					"java -classpath <JARS> " +
					GeradorDeBeans.class.getName()+ " " +
					"androidManifest arquivo_sql pastaSrc"
			);
			return;
		}

		String manifest = args[0];
		String arquivo = args[1];
		String pastaSrc = args[2];

		try {
			new GeradorDeBeans().gerarBeansWithJsqlparserAndCodeModel(
				new File(manifest),
				new File (arquivo),
				pastaSrc,
				"gen"
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
				LoggerUtil.getLog().info(
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

	public void gerarBeansWithJsqlparserAndCodeModel(
			File androidManifestFile,
			File sqlResource,
			String pastaSrc,
			String pacoteGen
	)
			throws IOException, FileNotFoundException, GeradorException
	{

		String pacote = getBasePackage(androidManifestFile);
		conferirArquivosCustomSrc(pacote, pastaSrc);

		Integer dbVersion = getDBVersion(pastaSrc, pacote);
		if(dbVersion==null)
			throw new GeradorException("versao do banco nao encontrada");
//		String basePackage = getBasePackage();
//		if(basePackage!=null)
//			getLog().info("package "+basePackage);
		String val = getSqlTill(sqlResource,dbVersion);
		if(val!=null){
			val = sqliteSchema(val);
			BufferedReader reader = new BufferedReader(new StringReader(val));
			String line = reader.readLine();
			while (line != null) {
				LoggerUtil.getLog().info(line);
				line = reader.readLine();
			}
		}

		String pacoteDb = pacote + (
			(DB_PACKAGE == null || DB_PACKAGE.matches("\\s*")) ?
				"" :
				"." + DB_PACKAGE
		);
		String dbClass = pacoteDb + "." +DB_CLASS;
//		String genericBeanClass = pacote;
//		if (GENERIC_BEAN_PACKAGE != null && !GENERIC_BEAN_PACKAGE.matches("\\s*"))
//			genericBeanClass += "." + GENERIC_BEAN_PACKAGE;
//		genericBeanClass += GENERIC_BEAN_CLASS;
		String dbStaticMethod = "getDb";

		Collection<TabelaSchema> tabelasBanco =
				getTabelasDoSchema(new StringReader(val));

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
				classeGerada = jbf.generateClass(
					pacote,
					pacoteGen,
					javaBeanSchema.getNome()
				);
			} catch (JClassAlreadyExistsException e) {
				throw new RuntimeException(e);
			}
			jbf.generateConstants(classeGerada, javaBeanSchema);
			for(String coluna : javaBeanSchema.getColunas()){
				Propriedade p =
					javaBeanSchema.getPropriedade(coluna);
				if(p!=null)
					jbf.generateProperty(classeGerada,p);
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
			daoFactory.generateSaveAndObjects(
					schemaJclass.klass,
					schemaJclass.schema
			);
			for(SchemaXJClass schemaJclassAssoc : listClasses){
				// gerando relacoes
				daoFactory.generateAssociationMethods(
						schemaJclass.klass,
						schemaJclass.schema,
						schemaJclassAssoc.klass,
						schemaJclassAssoc.schema
				);
			}
		}

		Map<JavaBeanSchema,JDefinedClass> map = new HashMap<JavaBeanSchema, JDefinedClass>();
		for(SchemaXJClass schemaJclass : listClasses){
			map.put(schemaJclass.schema, schemaJclass.klass);
		}
		daoFactory.generateDeleteMethods(map);

		for(SchemaXJClass schemaXJClass : listClasses){
			jbf.generateSerialVersionUID(schemaXJClass.klass);
			jbf.generateCloneMethod(
					schemaXJClass.klass,
					schemaXJClass.schema
			);
			jbf.generateHashCodeAndEquals(
					schemaXJClass.klass,
					schemaXJClass.schema
			);
		}

		String pastaGen = (pacote + "."+ pacoteGen)
				.replaceAll("\\.", File.separator);
		File pastaGenFolder = new File(pastaSrc, pastaGen);
		if(pastaGenFolder.exists()){
			LoggerUtil.getLog().info("Deletando " + pastaGenFolder.getAbsolutePath());
			deleteFolderR(pastaGenFolder);
		} else {
			LoggerUtil.getLog().info(
				"Pasta " +
				pastaGenFolder.getAbsolutePath() +
				" nao encontrada"
			);
		}

		//pastaGenFolder.mkdirs();
		jcm.build(new File(pastaSrc));
	}

	public Integer getDBVersion(String srcFolder, String basePackage)
			throws GeradorException {
		String packageFolder;
		packageFolder = basePackage.replaceAll("\\.", File.separator);
		File dbFile = new File(srcFolder + File.separator + packageFolder
				+ File.separator + GeradorDeBeans.DB_PACKAGE + File.separator
				+ GeradorDeBeans.DB_CLASS + ".java");
		if (!dbFile.exists()) {
			String errmsg = dbFile.getAbsolutePath() + " nao encontrado";
			LoggerUtil.getLog().error(errmsg);
			throw new GeradorException(errmsg);
		}
		Scanner scan;
		try {
			scan = new Scanner(dbFile);
		} catch (FileNotFoundException e) {
			throw new GeradorException(e);
		}
		Pattern dbVersionPattern = Pattern.compile("DB_VERSAO\\s*=\\s*([^;]);");
		int versionNumberGroup = 1;
		String s = scan.findWithinHorizon(dbVersionPattern, 0);
		if (s == null) {
			String errmsg = "DB_VERSAO nao encontrado";
			LoggerUtil.getLog().error(errmsg);
			throw new GeradorException(errmsg);
		}

		Matcher mobj = dbVersionPattern.matcher(s);
		mobj.find();
		Integer ver = Integer.parseInt(mobj.group(versionNumberGroup));
		return ver;
	}

	public String sqliteSchema(String sql) {
		try {
			return SQLiteGeradorUtils.getSchema(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public String getSqlTill(File sqlResource, Integer version) {
		StringBuilder out = new StringBuilder();
		List<String> nodes = XMLUtil.xpath(sqlResource, "//string["
				+ "contains(@name,\"db_versao_\") and "
				+ "number(substring(@name,11)) < " + (version++) + "]//text()");
		for (String node : nodes)
			out.append(node);
		return out.toString();
	}

	private String getBasePackage(File androidManifest)
			throws GeradorException
	{
		try {
			Pattern pat = Pattern.compile(
					".*<manifest[^>]*package=\"([^\"]*)\"[^>]*>.*",
					Pattern.MULTILINE);
			String manifestStr = new Scanner(androidManifest)
				.findWithinHorizon(pat, 0);
			Matcher mobj = pat.matcher(manifestStr);
			if (mobj.find())
				return mobj.group(1);
			return null;
		} catch (Exception e) {
			throw new GeradorException(e);
		}
	}


/*    private static String convertStreamToString(InputStream is)
            throws IOException
    {
        /*
         * To convert the InputStream to String we use the Reader.read(char[]
         * buffer) method. We iterate until the Reader return -1 which means
         * there's no more data to read. We use the StringWriter class to
         * produce the string.
         */
/*        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is,DEFAULT_ENCODING)
                );
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }
*/
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
			LoggerUtil.getLog().info("tabela: " +tabela.getNome());
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
						LoggerUtil.getLog().info("create" + ct.getTable().getName());
						for(Object obj :ct.getColumnDefinitions())
							LoggerUtil.getLog().info( ((ColumnDefinition)obj).getColumnName()+"  "+ ((ColumnDefinition)obj).getColDataType().getDataType() );
						LoggerUtil.getLog().info("-------------");
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
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
