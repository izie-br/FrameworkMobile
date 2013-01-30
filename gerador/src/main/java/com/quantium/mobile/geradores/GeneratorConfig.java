package com.quantium.mobile.geradores;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.quantium.mobile.geradores.util.Constants;
import com.quantium.mobile.geradores.util.LoggerUtil;

public class GeneratorConfig {

	private String basePackage;
	private String generatedCodePackageName = "gen";
	private String generatedCodePackage;

	private String inputFilePath;
	private String coreDirectoryPath;
	private String androidDirectoryPath;
	private String jdbcDirectoryPath;
	private String propertiesFilePath;
	private String androidManifestFilePath;

	private File migrationsOutput;
	private File sqlXmlOutput;

	private String coreTemporaryDirectoryPath = "__tempgen_core";
	private String androidTemporaryDirectoryPath = "__tempgen_android";
	private String jdbcTemporaryDirectoryPath = "__tempgen_jdbc";
	private String iosTemporaryDirectoryPath = "__tempgen_appios";

	private File inputFile;
	private File androidManifest;
	private File coreDirectory;
	private File androidDirectory;
	private File jdbcDirectory;
	private File propertiesFile;
	private File sourceDirectory;

	private File coreTemporaryDirectory;
	private File androidTemporaryDirectory;
	private File jdbcTemporaryDirectory;
	private File iosTemporaryDirectory;

	private Integer databaseVersion = null;

	public static Builder builder() {
		return new GeneratorConfig.Builder();
	}

	private GeneratorConfig(){}

	public String getBasePackage() {
		return basePackage;
	}

	public File getMigrationsOutputDir() {
		return migrationsOutput;
	}

	public File getSqlXmlOutput() {
		return sqlXmlOutput;
	}

	public String getInputFilePath() {
		return inputFilePath;
	}

	public String getAndroidManifestFilePath() {
		return androidManifestFilePath;
	}

	public String getCoreDirectoryPath() {
		return coreDirectoryPath;
	}

	public String getAndroidDirectoryPath() {
		return androidDirectoryPath;
	}

	public String getJdbcDirectoryPath() {
		return jdbcDirectoryPath;
	}

	public String getPropertiesFilePath() {
		return propertiesFilePath;
	}

	public File getInputFile() {
		return inputFile;
	}

	public File getCoreDirectory() {
		return coreDirectory;
	}

	public File getAndroidDirectory() {
		return androidDirectory;
	}

	public File getJdbcDirectory() {
		return jdbcDirectory;
	}

	public File getPropertiesFile() {
		return propertiesFile;
	}

	public File getSourceDirectory() {
		return sourceDirectory;
	}

	public String getDBClassSourceCodeFilePath() {
		return getBasePackageDirectoryPath() + System.getProperty ("file.separator")
				+ Constants.DB_PACKAGE + System.getProperty ("file.separator") + Constants.DB_CLASS
				+ ".java";
	}

	public String getBasePackageDirectoryPath() {
		return basePackage.replaceAll("\\.", System.getProperty ("file.separator"));
	}

	public String getGeneratedCodePackageName() {
		return generatedCodePackageName;
	}

	public String getGeneratedCodePackage() {
		return generatedCodePackage;
	}

//	public String retrieveAndroidManifestBasePackage() throws GeradorException {
//		return getBasePackageFromManifest(getAndroidManifestFile());
//	}

	public File getCoreTemporaryDirectory() {
		return coreTemporaryDirectory;
	}

	public File getAndroidTemporaryDirectory() {
		return androidTemporaryDirectory;
	}

	public File getJDBCTemporaryDirectory() {
		return jdbcTemporaryDirectory;
	}

	public File getIOSTemporaryDirectory() {
		return iosTemporaryDirectory;
	}

	public File getAndroidManifestFile() {
		return androidManifest;
	}

	/**
	 * Busca a versao do banco no arquivo DB.java.
	 * Encontra o arquivo e busca pela constante DB_VERSAO, que deve ser um
	 * literal inteiro.
	 * 
	 * @param srcFolder   diretorio de fontes do aplicativo
	 * @param basePackage pacote base do aplicativo
	 * @return
	 * @throws GeradorException
	 */
	public int retrieveDatabaseVersion() throws GeradorException {
		if (databaseVersion != null)
			return databaseVersion;

		File dbFile = new File(sourceDirectory, getDBClassSourceCodeFilePath());
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
		Pattern dbVersionPattern = Pattern
				.compile("DB_VERSAO\\s*=\\s*([^;]+);");
		int versionNumberGroup = 1;
		String s = scan.findWithinHorizon(dbVersionPattern, 0);
		if (s == null) {
			String errmsg = "DB_VERSAO nao encontrado";
			LoggerUtil.getLog().error(errmsg);
			throw new GeradorException(errmsg);
		}

		Matcher mobj = dbVersionPattern.matcher(s);
		mobj.find();
		databaseVersion = Integer.parseInt(mobj.group(versionNumberGroup));
		return databaseVersion;
	}

	private static String getBasePackageFromManifest(File androidManifest)
			throws GeradorException {
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

	public static class Builder {

		String basePackage;

		String baseDirectoryPath;
		File baseDirectory;

		String migrationsOutputPath;
		String sqlXmlOutputPath;

		String inputFilePath;
		File inputFile;

		String androidManifestPath;
		File androidManifest;

		String coreDirectoryPath;
		File coreDirectory;

		String androidDirectoryPath;
		File androidDirectory;

		String jdbcDirectoryPath;
		File jdbcDirectory;

		String propertiesFilePath;
		File propertiesFile;

		public GeneratorConfig create() throws GeradorException {
			if (this.baseDirectory == null || !this.baseDirectory.exists()) {
				throw new GeradorException(String.format(
						"BaseDirectory %s inexistente",
						(this.baseDirectory == null)?
								"null" :
								this.baseDirectory.getAbsolutePath()
				));
			}

			GeneratorConfig generatorConfig = new GeneratorConfig();
//			generatorConfig.baseDirectoryPath = getBaseDirectoryPath();
//			generatorConfig.baseDirectory = getBaseDirectory();
			generatorConfig.inputFile = getInputFile();
			generatorConfig.inputFilePath = getInputFilePath();

			if (this.migrationsOutputPath != null) {
				generatorConfig.migrationsOutput =
					new File (getBaseDirectory (), this.migrationsOutputPath);
			}
			if (this.sqlXmlOutputPath != null) {
				generatorConfig.sqlXmlOutput =
					new File (getBaseDirectory (), this.sqlXmlOutputPath);
			}

			generatorConfig.propertiesFilePath = getPropertiesFilePath();
			generatorConfig.propertiesFile = getPropertiesFile();

			generatorConfig.coreDirectoryPath = getCoreDirectoryPath();
			generatorConfig.coreDirectory = getCoreDirectory();
			generatorConfig.androidDirectory = getAndroidDirectory();
			generatorConfig.androidDirectoryPath = getAndroidDirectoryPath();
			generatorConfig.jdbcDirectory = getJdbcDirectory();
			generatorConfig.jdbcDirectoryPath = getJdbcDirectoryPath();
			generatorConfig.sourceDirectory =
					(androidDirectory != null)?
							this.androidDirectory :
							this.coreDirectory;
			if (generatorConfig.sourceDirectory == null) {
				throw new GeradorException(
						"jdbcDirectory ou androidDirectory deve " +
						"ser nao-null");
			}

			generatorConfig.androidManifestFilePath = getAndroidManifestPath();
			generatorConfig.androidManifest = getAndroidManifest();
			generatorConfig.basePackage = getBasePackage();
			if (generatorConfig.basePackage == null) {
				throw new GeradorException(
						"basePackage e androidManifest indefinidos");
			}
			generatorConfig.generatedCodePackage =
					/* (
						(this.basePackage==null)? "": (this.basePackage + ".")
					) + */
					generatorConfig.generatedCodePackageName;


			generatorConfig.coreTemporaryDirectory = new File(
					getBaseDirectory(),
					generatorConfig.coreTemporaryDirectoryPath);
			generatorConfig.androidTemporaryDirectory = new File(
					getBaseDirectory(),
					generatorConfig.androidTemporaryDirectoryPath);
			generatorConfig.jdbcTemporaryDirectory = new File(
					getBaseDirectory(),
					generatorConfig.jdbcTemporaryDirectoryPath);
			generatorConfig.iosTemporaryDirectory = new File(
					getBaseDirectory(),
					generatorConfig.iosTemporaryDirectoryPath
			);

			return generatorConfig;
		}

		public Builder setBasePackage(String basePackage) {
			this.basePackage = basePackage;
			return this;
		}

		public Builder setMigrationsOutput(String path) {
			this.migrationsOutputPath = path;
			return this;
		}

		public Builder setSqlXmlOutput(String path) {
			this.sqlXmlOutputPath = path;
			return this;
		}

		public Builder setPropertiesFile(String propertiesFile) {
			this.propertiesFilePath = propertiesFile;
			return this;
		}

		public Builder setBaseDirectory(String baseDirectory) {
			if (baseDirectory == null)
				throw new IllegalArgumentException();
			this.baseDirectoryPath = baseDirectory;
			this.baseDirectory = new File(this.baseDirectoryPath);
			return this;
		}

		public Builder setInputFile(String inputFile) {
			this.inputFilePath =
					(inputFile != null)?
							inputFile :
							"res/values/sql.xml";
			return this;
		}

		public Builder setAndroidManifest(String androidManifestPath) {
			this.androidManifestPath = androidManifestPath;
			return this;
		}

		public Builder setCoreDirectory(String coreDirectory) {
			this.coreDirectoryPath = coreDirectory;
			return this;
		}

		public Builder setAndroidDirectory(String androidDirectory) {
			this.androidDirectoryPath = androidDirectory;
			return this;
		}

		public Builder setJdbcDirectory(String jdbcDirectory) {
			this.jdbcDirectoryPath = jdbcDirectory;
			return this;
		}

		private File getBaseDirectory() throws GeradorException {
			if (this.baseDirectory != null)
				return this.baseDirectory;
			throw new GeradorException("baseDirectory indefinido");
		}

//		private String getBaseDirectoryPath() throws GeradorException {
//			if (this.baseDirectoryPath != null)
//				return this.baseDirectoryPath;
//			return getBaseDirectory().getAbsolutePath();
//		}

		private File getInputFile() throws GeradorException {
			if (this.inputFile == null && this.inputFilePath == null)
				throw new GeradorException("InputFile indefinido");

			synchronized (this) {
				if (this.inputFile == null) {
					this.inputFile = new File(
							getBaseDirectory(),
							this.inputFilePath
					);
					if (!this.inputFile.exists()) {
						throw new GeradorException(String.format(
								"Resources SQL nao encontrados em %s",
								(this.inputFile==null?
										"null":
										this.inputFile.getAbsolutePath())
						));
					}
				}
			}
			return this.inputFile;
		}

		private String getInputFilePath() throws GeradorException {
			if (this.inputFilePath != null)
				return this.inputFilePath;
			return getInputFile().getAbsolutePath();
		}

		private String getAndroidManifestPath() throws GeradorException {
			if (this.androidManifestPath != null)
				return this.androidManifestPath;
			if (this.getAndroidManifest() != null)
				return this.getAndroidManifest().getAbsolutePath();
			return null;
		}

		private File getAndroidManifest() throws GeradorException {
			if (this.androidManifest != null)
				return this.androidManifest;
			synchronized (this) {
				if (this.androidManifestPath != null) {
					this.androidManifest = new File(
							this.androidManifestPath
					);
					if (!this.androidManifest.exists()) {
						throw new GeradorException(String.format(
								"AndroidManifest nao encontrado em %s",
								this.androidManifestPath
						));
					}
				} else {
					File defaultAndroidManifest = new File(
							getBaseDirectory(),
							"/AndroidManifest.xml"
					);
					if (defaultAndroidManifest.exists()) {
						this.androidDirectory = defaultAndroidManifest;
						this.androidDirectoryPath =
								defaultAndroidManifest.getAbsolutePath();
					}
				}
			}
			return this.androidManifest;
		}

		private String getBasePackage() throws GeradorException {
			synchronized (this) {
				if (this.basePackage == null){
					this.basePackage =
							(this.basePackage != null)?
									this.basePackage :
							(getAndroidManifest() != null)?
									getBasePackageFromManifest(getAndroidManifest()):
							// default
									null;
				}
			}
			return this.basePackage;
		}

		private File getCoreDirectory() throws GeradorException {
			if (this.coreDirectory != null)
				return this.coreDirectory;
			synchronized (this) {
				if (this.coreDirectoryPath != null){
					this.coreDirectory = new File(
							getBaseDirectory(),
							this.coreDirectoryPath
					);
					return this.coreDirectory;
				}
			}
			throw new GeradorException("coreDirectory indefinido");
		}

		private String getCoreDirectoryPath() throws GeradorException {
			if (this.coreDirectoryPath != null)
				return this.coreDirectoryPath;
			return getCoreDirectory().getAbsolutePath();
		}

		private File getAndroidDirectory() throws GeradorException {
			if (this.androidDirectory != null)
				return this.androidDirectory;
			synchronized (this) {
				if (this.androidDirectoryPath != null){
					this.androidDirectory = new File(
							getBaseDirectory(),
							this.androidDirectoryPath
					);
					return this.androidDirectory;
				}
			}
			return null;
		}

		private String getAndroidDirectoryPath() throws GeradorException {
			if (this.androidDirectoryPath != null)
				return this.androidDirectoryPath;
			if (this.androidDirectory != null)
				return this.androidDirectory.getAbsolutePath();
			return null;
		}

		private File getJdbcDirectory() throws GeradorException {
			if (this.jdbcDirectory != null)
				return this.jdbcDirectory;
			synchronized (this) {
				if (this.jdbcDirectoryPath != null){
					this.jdbcDirectory = new File(
							getBaseDirectory(),
							this.jdbcDirectoryPath
					);
					return this.jdbcDirectory;
				}
			}
			return null;
		}

		private String getJdbcDirectoryPath() throws GeradorException {
			if (this.jdbcDirectoryPath != null)
				return this.jdbcDirectoryPath;
			if (this.jdbcDirectory != null)
				return this.jdbcDirectory.getAbsolutePath();
			return null;
		}

		private File getPropertiesFile() throws GeradorException {
			if (this.propertiesFile != null)
				return this.propertiesFile;

			synchronized (this) {
				if (this.propertiesFilePath != null) {
					this.propertiesFile = new File(
							getBaseDirectory(),
							this.propertiesFilePath
					);
				} else {
					this.propertiesFile = new File(
							getBaseDirectory(),
							Constants.DEFAULT_GENERATOR_CONFIG
					);
					this.propertiesFilePath =
							this.propertiesFile.getAbsolutePath();
				}
			}
			return this.propertiesFile;
		}

		private String getPropertiesFilePath() throws GeradorException {
			if (this.propertiesFilePath != null)
				return this.propertiesFilePath;
			return getPropertiesFile().getAbsolutePath();
		}

	}

}
