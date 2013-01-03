package com.quantium.mobile.geradores;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.parsers.FileParserMapper;
import com.quantium.mobile.geradores.parsers.InputParser;
import com.quantium.mobile.geradores.parsers.InputParserRepository;
import com.quantium.mobile.geradores.util.Constants;
import com.quantium.mobile.geradores.util.LoggerUtil;
import com.quantium.mobile.geradores.velocity.VelocityCustomClassesFactory;
import com.quantium.mobile.geradores.velocity.VelocityDaoFactory;
import com.quantium.mobile.geradores.velocity.VelocityObjcFactory;
import com.quantium.mobile.geradores.velocity.VelocityVOFactory;

public class Generator {

	private GeneratorConfig projectInformation;
	private InputParser inputParser;

	public Generator(GeneratorConfig info)
			throws GeradorException{
		this.projectInformation = info;
		InputParser parser = InputParserRepository.getInputParser(
			FileParserMapper.getTypeFromFileName(
				info.getInputFilePath()
			)
		);

		this.inputParser = parser;
	}


	/**
	 * @param args
	 */
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
		PropertiesLocal props = getProperties(projectInformation.getPropertiesFile());
		// Ultima versao do banco lida pelo gerador
		// Se for diferente da versao em DB.VERSION, o gerador deve
		//   reescrever os arquivos
		int propertyVersion = props.containsKey(Constants.PROPERTIY_DB_VERSION) ?
				Integer.parseInt(props.getProperty(Constants.PROPERTIY_DB_VERSION)) :
				0;

		int dbVersion = projectInformation.retrieveDatabaseVersion();

		/*
		 * Se a versao do banco é a mesma no generator.xml, finaliza o gerador
		 */
		LoggerUtil.getLog().info(
				"Last dbVersion:" + propertyVersion +
				" current dbVersion" + dbVersion);
		if (propertyVersion == (int)dbVersion)
			return;

		Collection<JavaBeanSchema> javaBeanSchemas = inputParser.getSchemas(
				null, projectInformation, defaultProperties);

		@SuppressWarnings("unchecked")
		Map<String,String> serializationAliases =
			(Map<String,String>)defaultProperties.get(Constants.PROPERTIY_SERIALIZATION_ALIAS);


		// Removendo os diretoros temporarios dos arquivos gerados e
		//   recriando-os vazios.
		File coreTempDir = resetDir(projectInformation.getCoreTemporaryDirectory());
		File androidTempDir = resetDir(projectInformation.getAndroidTemporaryDirectory());
		File jdbcTempDir = resetDir(projectInformation.getJDBCTemporaryDirectory());
		File appiosTempDir = resetDir(projectInformation.getIOSTemporaryDirectory());

		//inicializa e configura a VelocityEngine
		VelocityEngine ve = initVelocityEngine();

		VelocityDaoFactory vdaof = null;
		VelocityDaoFactory vJdbcDaoFactory = null;
		if (projectInformation.getAndroidDirectory() != null) {
			vdaof = new VelocityDaoFactory(
				"DAO.java",
				ve, androidTempDir,
				projectInformation.getGeneratedCodePackage(),
				serializationAliases);
		}
		if (projectInformation.getJdbcDirectory() != null) {
			vJdbcDaoFactory = new VelocityDaoFactory(
					"JdbcDao.java",
					ve, jdbcTempDir,
					projectInformation.getGeneratedCodePackage(),
					serializationAliases);
		}
		VelocityVOFactory vvof = new VelocityVOFactory(ve, coreTempDir,
				projectInformation.getBasePackage(), projectInformation.getGeneratedCodePackage(), serializationAliases);
		VelocityObjcFactory vobjcf = new VelocityObjcFactory(ve, appiosTempDir,
				projectInformation.getBasePackage(), projectInformation.getGeneratedCodePackage(), serializationAliases);

		for(JavaBeanSchema javaBeanSchema : javaBeanSchemas){
			if( javaBeanSchema.isNonEntityTable())
				continue;

			//vdaof.generateDAOAbstractClasses(javaBeanSchema, javaBeanSchemas);
			if (vdaof != null){
				vdaof.generateDAOImplementationClasses(
						javaBeanSchema, javaBeanSchemas);
			}
			if (vJdbcDaoFactory != null){
				vJdbcDaoFactory.generate(
						javaBeanSchema, "JdbcDAO", "",
						true, javaBeanSchemas);
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

		if (projectInformation.getAndroidDirectory() != null) {
			VelocityCustomClassesFactory.generateDAOFactory(
					"SQLiteDAOFactory.java",ve, javaBeanSchemas,
					projectInformation.getGeneratedCodePackage(), androidTempDir);
		}
		if (projectInformation.getJdbcDirectory() != null) {
			VelocityCustomClassesFactory.generateDAOFactory(
					"JdbcDAOFactory.java",ve, javaBeanSchemas,
					projectInformation.getGeneratedCodePackage(), jdbcTempDir);
		}


		String pastaGen = projectInformation.getGeneratedPackageDirectoryPath();

		// Substitui os pacotes gen por pastas vazias, para remover os
		//   arquivos antigos
		File coreGenFolder = resetDir(new File(projectInformation.getCoreDirectory(), pastaGen));
		File androidGenDir = resetDir(new File(projectInformation.getAndroidDirectory(), pastaGen));
		File jdbcGenDir    = resetDir(new File(projectInformation.getJdbcDirectory(), pastaGen));

		// Copia os novos arquivos para os pacotes gen vazios
		// OBS.: Para o caso de ambas as pastas "gen" serem a mesma pasta,
		//       no caso do "replaceGenFolder"
		for (File f : coreTempDir.listFiles())
			copyFile(f, new File(coreGenFolder, f.getName()));
		if (projectInformation.getAndroidDirectory() != null) {
			for (File f : androidTempDir.listFiles())
				copyFile(f, new File(androidGenDir, f.getName()));
		}
		if (projectInformation.getJdbcDirectory() != null) {
			for (File f : jdbcTempDir.listFiles())
				copyFile(f, new File(jdbcGenDir, f.getName()));
		}

//		props.save();
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
		ve.init();
		return ve;
	}

	/**
	 * Classe properties com metodo save para persistir como xml.
	 * Atualmente nao e utilizada.
	 * 
	 * @author Igor Soares
	 *
	 */
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
	private File resetDir(File dir) throws IOException {
		if (dir.exists())
			deleteFolderR(dir);
		dir.mkdirs();
		return dir;
	}

	/**
	 * Copia um arquivo
	 * 
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
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
