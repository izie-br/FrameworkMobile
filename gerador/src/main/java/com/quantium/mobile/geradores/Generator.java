package com.quantium.mobile.geradores;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoManyToMany;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.ModelSchema;
import com.quantium.mobile.geradores.parsers.FileParserMapper;
import com.quantium.mobile.geradores.parsers.InputParser;
import com.quantium.mobile.geradores.parsers.InputParserRepository;
import com.quantium.mobile.geradores.parsers.MigrationsInputParser;
import com.quantium.mobile.geradores.util.Constants;
import com.quantium.mobile.geradores.util.LoggerUtil;
import com.quantium.mobile.geradores.util.TableUtil;
import com.quantium.mobile.geradores.velocity.VelocityCustomClassesFactory;
import com.quantium.mobile.geradores.velocity.VelocityDaoFactory;
import com.quantium.mobile.geradores.velocity.VelocityObjcFactory;
import com.quantium.mobile.geradores.velocity.VelocitySqlXmlFactory;
import com.quantium.mobile.geradores.velocity.VelocityStubFactory;
import com.quantium.mobile.geradores.velocity.VelocityVOFactory;

/**
 * TODO: mover os defaultProperties para o {@link GeneratorConfig}
 * 
 * @author Igor Soares
 *
 */
public class Generator {

	private static final boolean VELOCITY_PERFORMANCE_PARAMS = true;

	private GeneratorConfig projectConfig;
	private InputParser inputParser;

	public Generator(GeneratorConfig info)
			throws GeradorException{
		this.projectConfig = info;
		InputParser parser = InputParserRepository.getInputParser(
			FileParserMapper.getTypeFromFileName(
				info.getInputFilePath()
			)
		);

		this.inputParser = parser;
	}


	public static void main(String[] args) throws GeradorException{

		if(args==null||args.length < 4){
			LoggerUtil.getLog().info(
					"Uso:\n"+
					"java -classpath <JARS> " +
					Generator.class.getName()+ " " +
					"pacote arquivo_sql coreSrc androidSrc jdbcSrc [properties]"
			);
			return;
		}

		Map<String,Object> defaultProperties = new HashMap<String,Object>();
		
		GeneratorConfig config = getInfoFromCommandLineArgs(args);
		try {
			new Generator(config).generate(defaultProperties);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static GeneratorConfig getInfoFromCommandLineArgs(String[] args)
			throws GeradorException {
		String basePackage = args[0];
		String inputFilePath = args[1];
		String coreDirectoryPath = args[2];
		String androidDirectoryPath = args[3];
		String jdbcDirectoryPath = args[4];
		String propertiesFilePath = (args.length > 5) ? args[5]
				: Constants.DEFAULT_GENERATOR_CONFIG;
		return GeneratorConfig.builder()
				.setBasePackage(basePackage)
				.setInputFile(inputFilePath)
				.setBaseDirectory(System.getProperty("user.dir"))
				.setCoreDirectory(coreDirectoryPath)
				.setAndroidDirectory(androidDirectoryPath)
				.setJdbcDirectory(jdbcDirectoryPath)
				.setPropertiesFile(propertiesFilePath)
				.create();
	}

	public void generate(
			Map<String,Object> defaultProperties)
			throws IOException, FileNotFoundException, GeradorException
	{

		// Arquivo de propriedades para armazenar dados internos do gerador
//		PropertiesLocal props = getProperties(projectInformation.getPropertiesFile());
		// Ultima versao do banco lida pelo gerador
		// Se for diferente da versao em DB.VERSION, o gerador deve
		//   reescrever os arquivos
//		int propertyVersion = props.containsKey(Constants.PROPERTIY_DB_VERSION) ?
//				Integer.parseInt(props.getProperty(Constants.PROPERTIY_DB_VERSION)) :
//				0;

		int dbVersion = projectConfig.retrieveDatabaseVersion();

		/*
		 * Se a versao do banco é a mesma no generator.xml, finaliza o gerador
		 */
//		LoggerUtil.getLog().info(
//				"Last dbVersion:" + propertyVersion +
//				" current dbVersion" + dbVersion);
//		if (propertyVersion == (int)dbVersion)
//			return;

		Collection<JavaBeanSchema> javaBeanSchemas = inputParser.getSchemas(projectConfig, defaultProperties);

		//gerando migracoes
		generateMigrations (inputParser, projectConfig, javaBeanSchemas);

		@SuppressWarnings("unchecked")
		Map<String,String> serializationAliases =
			(Map<String,String>)defaultProperties.get(Constants.PROPERTIY_SERIALIZATION_ALIAS);


		// Removendo os diretoros temporarios dos arquivos gerados e
		//   recriando-os vazios.
		File coreTempDir = resetDir(projectConfig.getCoreTemporaryDirectory());
		File androidTempDir = resetDir(projectConfig.getAndroidTemporaryDirectory());
		File jdbcTempDir = resetDir(projectConfig.getJDBCTemporaryDirectory());
		File appiosTempDir = resetDir(projectConfig.getIOSTemporaryDirectory());

		//inicializa e configura a VelocityEngine
		VelocityEngine ve = initVelocityEngine();

		generateSqlXml(dbVersion, ve);

		VelocityDaoFactory vdaof = null;
		VelocityDaoFactory vJdbcDaoFactory = null;
		if (projectConfig.getAndroidDirectory() != null) {
			vdaof = new VelocityDaoFactory(
				ve, androidTempDir,
				VelocityDaoFactory.Type.ANDROID,
				projectConfig.getBasePackage (),
				projectConfig.getGeneratedCodePackage(),
				"vo",
				serializationAliases);
		}
		if (projectConfig.getJdbcDirectory() != null) {
			vJdbcDaoFactory = new VelocityDaoFactory(
					ve, jdbcTempDir,
					VelocityDaoFactory.Type.JDBC,
					projectConfig.getBasePackage (),
					projectConfig.getGeneratedCodePackage(),
					"vo",
					serializationAliases);
		}
		VelocityVOFactory vvof = new VelocityVOFactory(ve, coreTempDir,
				projectConfig.getBasePackage(), projectConfig.getGeneratedCodePackage(), "vo", serializationAliases);
		VelocityObjcFactory vobjcf = new VelocityObjcFactory(ve, appiosTempDir,
				projectConfig.getBasePackage(), projectConfig.getGeneratedCodePackage(), serializationAliases);

		generateClasses(javaBeanSchemas, vdaof, vJdbcDaoFactory, vvof, vobjcf);

		if (projectConfig.getAndroidDirectory() != null) {
			VelocityCustomClassesFactory.generateDAOFactory(
					"SQLiteDAOFactory.java",
					VelocityDaoFactory.Type.ANDROID.getSuffix (),
					ve, javaBeanSchemas,
					projectConfig.getBasePackage (),
					projectConfig.getGeneratedCodePackage(),
					"vo",
					androidTempDir);
		}
		if (projectConfig.getJdbcDirectory() != null) {
			VelocityCustomClassesFactory.generateDAOFactory(
					"JdbcDAOFactory.java",
					VelocityDaoFactory.Type.JDBC.getSuffix (),
					ve, javaBeanSchemas,
					projectConfig.getBasePackage (),
					projectConfig.getGeneratedCodePackage(),
					"vo",
					jdbcTempDir);
		}


//		String pastaGen = projectInformation.getGeneratedPackageDirectoryPath();
//
//		// Substitui os pacotes gen por pastas vazias, para remover os
//		//   arquivos antigos
//		File coreGenFolder = resetDir(new File(projectInformation.getCoreDirectory(), pastaGen));
//		File androidGenDir = resetDir(new File(projectInformation.getAndroidDirectory(), pastaGen));
//		File jdbcGenDir    = resetDir(new File(projectInformation.getJdbcDirectory(), pastaGen));

		// Copia os novos arquivos para os pacotes gen vazios
		// OBS.: Para o caso de ambas as pastas "gen" serem a mesma pasta,
		//       no caso do "replaceGenFolder"

		String baseDirectory = projectConfig.getBasePackageDirectoryPath ();

		File coreBaseDir = new File(projectConfig.getCoreDirectory (), baseDirectory);
		resetAllGenFolders (coreTempDir, coreBaseDir);
		File androidBaseDir = null;
		if (projectConfig.getAndroidDirectory() != null) {
			androidBaseDir = new File(
					projectConfig.getAndroidDirectory (), baseDirectory);
			resetAllGenFolders (androidTempDir, androidBaseDir);
		}
		File jdbcBaseDir = null;
		if (projectConfig.getJdbcDirectory() != null) {
			jdbcBaseDir = new File(
					projectConfig.getJdbcDirectory (), baseDirectory);
			resetAllGenFolders (jdbcTempDir, jdbcBaseDir);
		}

		copyDirContentsR (coreTempDir, coreBaseDir);
		if (androidBaseDir != null) {
			copyDirContentsR (androidTempDir, androidBaseDir);
		}
		if (jdbcBaseDir != null) {
			copyDirContentsR (jdbcTempDir,jdbcBaseDir);
		}

		//Stubs
		VelocityStubFactory implStubFactory = new VelocityStubFactory(
				ve,
				VelocityStubFactory.StubType.IMPLEMENTATION,
				projectConfig.getBasePackage(),
				projectConfig.getGeneratedCodePackage(),
				"vo",
				coreBaseDir,
				javaBeanSchemas);
		VelocityStubFactory interfaceStubFactory = new VelocityStubFactory(
				ve,
				VelocityStubFactory.StubType.INTERFACE,
				projectConfig.getBasePackage(),
				projectConfig.getGeneratedCodePackage(),
				"vo",
				coreBaseDir,
				javaBeanSchemas);
		checkForStubs(interfaceStubFactory, javaBeanSchemas);
		checkForStubs(implStubFactory, javaBeanSchemas);

//		props.save();
	}


	private void generateSqlXml(int dbVersion, VelocityEngine ve)
			throws IOException, FileNotFoundException {
		{
			File sqlXmlFile = projectConfig.getSqlXmlOutput ();
			Map<String, InputStream> migrationsMap = getMigrationsMap (projectConfig, dbVersion);
			if (sqlXmlFile != null && migrationsMap != null) {
				if (sqlXmlFile.exists ())
					sqlXmlFile.delete ();
				sqlXmlFile.createNewFile ();
				OutputStream out = new FileOutputStream (sqlXmlFile);
				VelocitySqlXmlFactory vsqlf = new VelocitySqlXmlFactory (ve, out);
				vsqlf.generateSqlXml (migrationsMap);
			}
		}
	}


	private void generateClasses(Collection<JavaBeanSchema> javaBeanSchemas,
			VelocityDaoFactory vdaof, VelocityDaoFactory vJdbcDaoFactory,
			VelocityVOFactory vvof, VelocityObjcFactory vobjcf)
			throws IOException {
		for(JavaBeanSchema javaBeanSchema : javaBeanSchemas){
			if( javaBeanSchema.isNonEntityTable())
				continue;

			//vdaof.generateDAOAbstractClasses(javaBeanSchema, javaBeanSchemas);
			if (vdaof != null){
				vdaof.generate(javaBeanSchema, javaBeanSchemas);
			}
			if (vJdbcDaoFactory != null){
				vJdbcDaoFactory.generate(javaBeanSchema, javaBeanSchemas);
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
	}

	private static void checkForStubs (
			VelocityStubFactory stubFactory,
			Collection<JavaBeanSchema> schemas)
	{
		for (JavaBeanSchema schema : schemas) {
			if (schema.isNonEntityTable())
				continue;
			if (!stubFactory.isStubFound(schema)) {
				stubFactory.createStubFor(schema);
			}
		}
	}

	private VelocityEngine initVelocityEngine() {
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
			      LoggerUtil.class.getName() );

		// Para mandar o log do Velocity para Log do Maven
		ve.setProperty("runtime.log.logsystem.log4j.logger",
		               LoggerUtil.LOG_NAME);

		// Para carregar a os templates de dentro do jar,
		//   localizados em src/main/resources
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
		ve.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

		/*
		 * parametros para otimizacao da performance
		 */
		if (VELOCITY_PERFORMANCE_PARAMS){
			/*
			 * Impede reload da bibioteca de macros do velocity
			 * O valor padrao ja parece ser false
			 * Redefini apenas para reforcar
			 */
			ve.setProperty("velocimacro.library.autoreload", false);
			/*
			 *   Os templates nao serao alterados durante execucao do plugin,
			 * logo, deve-se usar cache e nao conferir  mais por modificacoes
			 */
			ve.setProperty("class.resource.loader.cache", true);
			ve.setProperty("class.resource.loader.modificationCheckInterval", "-1");
		}

		ve.init();
		return ve;
	}

	/*
	 * Classe properties com metodo save para persistir como xml.
	 * 
	 * @author Igor Soares
	 *
	 */
//	private static class PropertiesLocal extends Properties{
//		private static final long serialVersionUID = -3878975503573330508L;
//
//		private File f;
//
//		private PropertiesLocal(File f) {
//			super();
//			if (f == null ){
//				throw new IllegalArgumentException("file is null");
//			}
//			this.f = f;
//			try{
//				if (!f.exists()){
//					if (f.createNewFile())
//						this.save();
//					else
//						throw new RuntimeException("Could not create " + f.getAbsolutePath());
//				}
//				FileInputStream fis = new FileInputStream(f);
//				if (f.getName().endsWith(".xml")){
//					this.loadFromXML(fis);
//				}
//				else
//					this.load(fis);
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//				throw new RuntimeException(e);
//			} catch (IOException e) {
//				e.printStackTrace();
//				throw new RuntimeException(e);
//			}
//		}
//
//		private void save() throws FileNotFoundException, IOException {
//			FileOutputStream fos = new FileOutputStream(f);
//			if (f.getName().endsWith(".xml"))
//				this.storeToXML(fos, null);
//			else
//				this.store(fos, null);
//		}
//
//	}
//
//	private PropertiesLocal getProperties(File f){
//		return new PropertiesLocal(f);
//	}

	/**
	 * Remover um diretorio e todos seus arquivos e diretorios recursivamente
	 * 
	 * @param f
	 * @throws IOException
	 */
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

	/**
	 * Remove um diretorio e todos seus arquivos,e depois o recria.
	 * 
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	private static File resetDir(File dir) throws IOException {
		if (dir.exists())
			deleteFolderR(dir);
		dir.mkdirs();
		return dir;
	}

	public static void copyDirContentsR (File sourceDir, File destDir)
			throws IOException
	{
		if (!destDir.exists ())
			destDir.mkdir ();

		for (File f : sourceDir.listFiles ()) {
			// File.getName () retorna apenas a ultima componente do caminho
			// sendo usado como caminho relativo
			File dest = new File (destDir, f.getName ());
			if (f.isDirectory ()){
				copyDirContentsR (f, dest);
			}
			else
				copyFile (f, dest);
		}
	}

	/**
	 * Confere a arvore de pastas em "tempgen" e localiza as pastas que em que
	 * ha arqivos de texto (que nao sao diretorios).
	 * As pastas com arquivos de texto sao removidas, assim como as subpastas.
	 * 
	 * @param tempDir pasta "tempgen"
	 * @param destDir pasta de destino
	 * @throws IOException
	 */
	private static void resetAllGenFolders (File tempDir, File destDir)
		throws IOException
	{
		if (!destDir.exists ())
			destDir.mkdir ();

		for (File f : tempDir.listFiles ()) {
			// File.getName () retorna apenas a ultima componente do caminho
			// sendo usado como caminho relativo
			File dest = new File (destDir, f.getName ());
			if (f.isDirectory ()){
				resetAllGenFolders (f, dest);
			} else if (destDir.getName().equals("gen")){
				// apenas deiretorios de nome "gen" sao removidos
				// REFATORAR: a string "gen" deveria ser um parametro da funcao
				resetDir (destDir);
				break;
			}
		}
	}

	/**
	 * Copia um arquivo
	 * 
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	private static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	private static void generateMigrations(
			InputParser parser,
			GeneratorConfig config,
			Collection<JavaBeanSchema> schemas) throws GeradorException
	{
		Map<String,Table> tables = new HashMap<String, Table> ();
		for (JavaBeanSchema jbschema : schemas) {
			Table table = jbschema.getTabela ();
			tables.put (table.getName (), table);
			for (Associacao assoc : jbschema.getAssociacoes ()) {
				if (assoc instanceof AssociacaoManyToMany) {
					AssociacaoManyToMany m2m = (AssociacaoManyToMany) assoc;
					ModelSchema jointable = m2m.getTabelaJuncao ();
					tables.put (
							jointable.getName (),
							TableUtil.tableForModelSchema (jointable));
				}
			}
		}
		parser.generateSqlResources (config, tables.values ());
	}

	private static Map<String, InputStream> getMigrationsMap (
			GeneratorConfig config, int version)
	{
		Map<String, InputStream> scripts = new TreeMap <String, InputStream> ();
		File migrationsDir = config.getMigrationsOutputDir ();
		if (migrationsDir == null || !migrationsDir.exists ())
			return null;
		Collection<File> orderedFiles = MigrationsInputParser.getOrderedFiles (migrationsDir, version);
		for (File f: orderedFiles) {
			try {
				scripts.put (f.getName ().replace (".sql", ""), new FileInputStream (f));
			} catch (Exception e) {
				throw new RuntimeException (e);
			}
		}
		return scripts;
	}

}
