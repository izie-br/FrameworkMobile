package com.quantium.mobile.geradores;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import com.quantium.mobile.geradores.filters.PrefixoTabelaFilter;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoPorNomeFilter;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.sqlparser.SqlTabelaSchemaFactory;
import com.quantium.mobile.geradores.tabelaschema.TabelaSchema;
import com.quantium.mobile.geradores.util.LoggerUtil;
import com.quantium.mobile.geradores.util.SQLiteGeradorUtils;
import com.quantium.mobile.geradores.util.XMLUtil;
import com.quantium.mobile.geradores.velocity.VelocityCustomClassesFactory;
import com.quantium.mobile.geradores.velocity.VelocityDaoFactory;
import com.quantium.mobile.geradores.velocity.VelocityObjcFactory;
import com.quantium.mobile.geradores.velocity.VelocityVOFactory;

public class Generator {

	public static final String DEFAULT_GENERATOR_CONFIG = "generator.xml";
	public static final String PROPERTIY_DB_VERSION = "dbVersion";
	public static final String PROPERTIY_IGNORED = "ignore";
	public static final String PROPERTIY_SERIALIZATION_ALIAS = "alias";

	public static final String DB_CLASS = "DB";
	public static final String DB_RESOURCE_FILE = "/DB.java";
	public static final String DB_PACKAGE = "db";
	public static final String GENERIC_BEAN_CLASS = "GenericBean";
	public static final String GENERIC_BEAN_PACKAGE = null;

	public static final long DEFAULT_ID = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws GeradorException{

		if(args==null||args.length < 4){
			LoggerUtil.getLog().info(
					"Uso:\n"+
					"java -classpath <JARS> " +
					Generator.class.getName()+ " " +
					"androidManifest arquivo_sql coreSrc androidSrc jdbcSrc [properties]"
			);
			return;
		}

		Map<String,Object> defaultProperties = new HashMap<String,Object>();
		String manifest = args[0];
		String arquivo = args[1];
		String pastaSrc = args[2];
		String androidSrc = args[3];
		String jdbcSrc = args[4];
		String properties = (args.length > 5) ? args[5] : DEFAULT_GENERATOR_CONFIG;

		try {
			new Generator().generateBeansWithJsqlparserAndVelocity(
				new File(manifest), new File (arquivo),
				new File(pastaSrc), new File(androidSrc), new File(jdbcSrc),
				"gen", new File(properties), defaultProperties);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void generateBeansWithJsqlparserAndVelocity(
			File androidManifestFile, File sqlResource,
			File coreSrcDir, File androidSrcDir, File jdbcSrcDir,
			String pacoteGen, File properties, Map<String,Object> defaultProperties)
			throws IOException, FileNotFoundException, GeradorException
	{

		String pacote = getBasePackage(androidManifestFile);

		// Arquivo de propriedades para armazenar dados internos do gerador
		PropertiesLocal props = getProperties(properties);
		// Ultima versao do banco lida pelo gerador
		// Se for diferente da versao em DB.VERSION, o gerador deve
		//   reescrever os arquivos
		int propertyVersion = props.containsKey(PROPERTIY_DB_VERSION) ?
				Integer.parseInt(props.getProperty(PROPERTIY_DB_VERSION)) :
				0;

		Integer dbVersion = getDBVersion(androidSrcDir, pacote);
		if(dbVersion==null)
			throw new GeradorException("versao do banco nao encontrada");

		/*
		 * Se a versao do banco é a mesma no generator.xml, finaliza o gerador
		 */
		LoggerUtil.getLog().info(
				"Last dbVersion:" + propertyVersion +
				" current dbVersion" + dbVersion);
		if (propertyVersion == (int)dbVersion)
			return;

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

//		String genericBeanClass = pacote;
//		if (GENERIC_BEAN_PACKAGE != null && !GENERIC_BEAN_PACKAGE.matches("\\s*"))
//			genericBeanClass += "." + GENERIC_BEAN_PACKAGE;
//		genericBeanClass += GENERIC_BEAN_CLASS;

		Collection<TabelaSchema> tabelasBanco =
				getTabelasDoSchema(
						new StringReader(val),
						(String)defaultProperties.get(PROPERTIY_IGNORED));

		JavaBeanSchema.Factory factory = new JavaBeanSchema.Factory();
		factory.addFiltroFactory(
			new PrefixoTabelaFilter.Factory("tb_"));
		factory.addFiltroFactory(new AssociacaoPorNomeFilter.Factory(
				"{COLUMN=id}_{TABLE}"
		));

		// gerando os JavaBeanSchemas
		Collection<JavaBeanSchema> javaBeanSchemas =
			new ArrayList<JavaBeanSchema>();
		for(TabelaSchema tabela : tabelasBanco)
			javaBeanSchemas.add(
				factory.javaBeanSchemaParaTabela(tabela));

		@SuppressWarnings("unchecked")
		Map<String,String> serializationAliases =
			(Map<String,String>)defaultProperties.get(PROPERTIY_SERIALIZATION_ALIAS);


		// Removendo os diretoros temporarios dos arquivos gerados e
		//   recriando-os vazios.
		File coreTempDir = resetDir("__tempgen_core");
		File androidTempDir =
			(androidSrcDir == null) ?
				null :
				resetDir("__tempgen_android");
		File jdbcTempDir =
			(jdbcSrcDir == null) ?
				null :
				resetDir("__tempgen_jdbc");
		File appiosTempDir = resetDir("__tempgen_appios");

		//inicializa e configura a VelocityEngine
		VelocityEngine ve = initVelocityEngine();

		VelocityDaoFactory vdaof = null;
		VelocityDaoFactory vJdbcDaoFactory = null;
		if (androidSrcDir != null) {
			vdaof = new VelocityDaoFactory(
				"DAO.java",
				ve, androidTempDir,
				pacote+ "."+ pacoteGen,
				serializationAliases);
		}
		if (jdbcSrcDir != null) {
			vJdbcDaoFactory = new VelocityDaoFactory(
					"JdbcDao.java",
					ve, jdbcTempDir,
					pacote+ "."+ pacoteGen,
					serializationAliases);
		}
		VelocityVOFactory vvof = new VelocityVOFactory(ve, coreTempDir,
				pacote, pacote+'.'+pacoteGen, serializationAliases);
		VelocityObjcFactory vobjcf = new VelocityObjcFactory(ve, appiosTempDir,
				pacote, pacote+'.'+pacoteGen, serializationAliases);

		for(JavaBeanSchema javaBeanSchema : javaBeanSchemas){
			if( javaBeanSchema.isNonEntityTable())
				continue;

			//vdaof.generateDAOAbstractClasses(javaBeanSchema, javaBeanSchemas);
			if (vdaof != null){
				vdaof.generateDAOImplementationClasses(
						javaBeanSchema, javaBeanSchemas);
			}
			if (vJdbcDaoFactory != null){
				vJdbcDaoFactory.generateDAOImplementationClasses(
						javaBeanSchema, javaBeanSchemas);
			}
			vvof.generateVO(javaBeanSchema, javaBeanSchemas,
			                VelocityVOFactory.Type.INTERFACE);
			vvof.generateVO(javaBeanSchema, javaBeanSchemas,
			                VelocityVOFactory.Type.IMPLEMENTATION);
			vvof.generateVO(javaBeanSchema, javaBeanSchemas,
			                VelocityVOFactory.Type.EDITABLE_INTERFACE);

			vobjcf.generateVO(javaBeanSchema, javaBeanSchemas,
			                  VelocityObjcFactory.Type.PROTOCOL);
			vobjcf.generateVO(javaBeanSchema, javaBeanSchemas,
			                  VelocityObjcFactory.Type.PROTOCOL_IMPL);
			vobjcf.generateVO(javaBeanSchema, javaBeanSchemas,
			                  VelocityObjcFactory.Type.IMPLEMENTATION);
			vobjcf.generateVO(javaBeanSchema, javaBeanSchemas,
			                  VelocityObjcFactory.Type.INTERFACE);
			vobjcf.generateVO(javaBeanSchema, javaBeanSchemas,
			                  VelocityObjcFactory.Type.EDITABLE_PROTOCOL);
		}

		if (androidSrcDir != null) {
			VelocityCustomClassesFactory.generateDAOFactory(
					"SQLiteDAOFactory.java",ve, javaBeanSchemas,
					pacote+'.'+pacoteGen, androidTempDir);
		}
		if (jdbcSrcDir != null) {
			VelocityCustomClassesFactory.generateDAOFactory(
					"JdbcDAOFactory.java",ve, javaBeanSchemas,
					pacote+'.'+pacoteGen, androidTempDir);
		}


		String pastaGen = (pacote + "."+ pacoteGen)
				.replaceAll("\\.", File.separator);

		// Substitui os pacotes gen por pastas vazias, para remover os
		//   arquivos antigos
		File coreGenFolder = replaceGenFolder(coreTempDir, coreSrcDir, pastaGen);
		File androidGenDir = replaceGenFolder(androidTempDir, androidSrcDir, pastaGen);

		// Copia os novos arquivos para os pacotes gen vazios
		// OBS.: Para o caso de ambas as pastas "gen" serem a mesma pasta,
		//       no caso do "replaceGenFolder"
		for (File f : coreTempDir.listFiles())
			copyFile(f, new File(coreGenFolder, f.getName()));
		for (File f : androidTempDir.listFiles())
			copyFile(f, new File(androidGenDir, f.getName()));

//		props.save();
	}

	private File replaceGenFolder(
			File tempDir, File srcDir, String genFolder)
			throws IOException
	{
		File coreGenFolder = new File(srcDir, genFolder);
		if(coreGenFolder.exists()){
			LoggerUtil.getLog().info("Deletando " + coreGenFolder.getAbsolutePath());
			deleteFolderR(coreGenFolder);
		} else {
			LoggerUtil.getLog().info(
				"Pasta " +
				coreGenFolder.getAbsolutePath() +
				" nao encontrada"
			);
		}
		coreGenFolder.mkdirs();
		return coreGenFolder;
	}

	private VelocityEngine initVelocityEngine() {
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
			      LoggerUtil.class.getName() );
		ve.setProperty("runtime.log.logsystem.log4j.logger",
		               LoggerUtil.LOG_NAME);
//		ve.setProperty(RuntimeConstants.RESOURCE_LOADER,
//				"classpath");
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
		ve.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		ve.init();
		return ve;
	}

	
	private File resetDir(String dirName) throws IOException {
		File dir = new File(dirName);
		if (dir.exists())
			deleteFolderR(dir);
		dir.mkdir();
		return dir;
	}


	private Integer getDBVersion(File srcFolder, String basePackage)
			throws GeradorException {
		String packageFolder;
		packageFolder = basePackage.replaceAll("\\.", File.separator);
		File dbFile = new File(
				srcFolder,
				(packageFolder
				+ File.separator + Generator.DB_PACKAGE + File.separator
				+ Generator.DB_CLASS + ".java" ) );
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
		Pattern dbVersionPattern = Pattern.compile("DB_VERSAO\\s*=\\s*([^;]+);");
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

	private static class PropertiesLocal extends Properties{
		private static final long serialVersionUID = -3878975503573330508L;

		private File f;

		private PropertiesLocal(File f) {
			super();
			if (f == null ){
				throw new IllegalArgumentException("file is null");
			}
			this.f = f;
			try{
				if (!f.exists()){
					if (f.createNewFile())
						this.save();
					else
						throw new RuntimeException("Could not create " + f.getAbsolutePath());
				}
				FileInputStream fis = new FileInputStream(f);
				if (f.getName().endsWith(".xml")){
					this.loadFromXML(fis);
				}
				else
					this.load(fis);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		private void save() throws FileNotFoundException, IOException {
			FileOutputStream fos = new FileOutputStream(f);
			if (f.getName().endsWith(".xml"))
				this.storeToXML(fos, null);
			else
				this.store(fos, null);
		}

	}

	private PropertiesLocal getProperties(File f){
		return new PropertiesLocal(f);
	}

	private String sqliteSchema(String sql) {
		try {
			return SQLiteGeradorUtils.getSchema(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private String getSqlTill(File sqlResource, Integer version) {
		StringBuilder out = new StringBuilder();
		List<String> nodes = XMLUtil.xpath(sqlResource, "//string["
				+ "contains(@name,\"db_versao_\") and "
				+ "number(substring(@name,11)) < " + (++version) + "]//text()");
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

	public static String getPacotePath(String pacote) {
		return pacote.replaceAll("\\.", File.separator);
	}

	public static Collection<TabelaSchema> getTabelasDoSchema(Reader input, String ignored)
			throws IOException
	{
		BufferedReader reader = new BufferedReader(input);
		Collection<TabelaSchema> tabelas =
			new ArrayList<TabelaSchema>();
		SqlTabelaSchemaFactory factory = new SqlTabelaSchemaFactory();
		Pattern createTablePattern = Pattern.compile(
				"CREATE\\s+(TEMP\\w*\\s+)?TABLE\\s+(IF\\s+NOT\\s+EXISTS\\s+)?(\\w+)",
				Pattern.CASE_INSENSITIVE |
				Pattern.MULTILINE
			);
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
			String ignoredTables [] = getIgnoredTables(ignored);
			if(
					createTableStatement==null ||
					createTableStatement.matches("^[\\s\\n]*$")
			){
				break;
			}
			Matcher mobj = createTablePattern.matcher(createTableStatement);
			if(
					!mobj.find() ||
					checkIfIgnored( mobj.group(3), ignoredTables)
			){
				LoggerUtil.getLog().info("IGNORED::" +sb.toString());
				continue;
			}
			TabelaSchema tabela =
				factory.gerarTabelaSchema(createTableStatement);
			tabelas.add(tabela);
			LoggerUtil.getLog().info("tabela: " +tabela.getNome());
		}
		return tabelas;
	}

	private static boolean checkIfIgnored(String table, String ignored[]){
		if (ignored == null)
			return false;
		for (String str : ignored){
			if(table.equalsIgnoreCase(str))
				return true;
		}
		return false;
	}

	private static String [] getIgnoredTables(String ignored){
		return ignored == null ? new String[0] : ignored.split("[\\|,]");
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

	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}

}
