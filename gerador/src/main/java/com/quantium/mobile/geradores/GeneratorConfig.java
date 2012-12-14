package com.quantium.mobile.geradores;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.quantium.mobile.geradores.parsers.FileParserMapper;
import com.quantium.mobile.geradores.util.Constants;
import com.quantium.mobile.geradores.util.LoggerUtil;

public class GeneratorConfig {

	private String basePackage;
	private String generatedCodePackageName;
	private String generatedCodePackage;

	private String inputFilePath;
	private String baseDirectoryPath;
	private String coreDirectoryPath;
	private String androidDirectoryPath;
	private String jdbcDirectoryPath;
	private String propertiesFilePath;
	private String androidManifestFilePath;

	private String coreTemporaryDirectoryPath;
	private String androidTemporaryDirectoryPath;
	private String jdbcTemporaryDirectoryPath;
	private String iosTemporaryDirectoryPath;

	private File inputFile;
	private File androidManifest;
	private File baseDirectory;
	private File coreDirectory;
	private File androidDirectory;
	private File jdbcDirectory;
	private File propertiesFile;
	private File sourceDirectory;

	private File coreTemporaryDirectory;
	private File androidTemporaryDirectory;
	private File jdbcTemporaryDirectory;
	private File iosTemporaryDirectory;

	private Integer databaseVersion;

	public GeneratorConfig(String basePackage, String inputFilePath,
			String baseDirectoryPath, String coreDirectoryPath,
			String androidDirectoryPath, String jdbcDirectoryPath,
			String propertiesFilePath, String androidManifestFilePath)
			throws GeradorException {

		this.baseDirectoryPath = baseDirectoryPath;

		this.coreDirectoryPath = coreDirectoryPath;
		this.androidDirectoryPath = androidDirectoryPath;
		this.jdbcDirectoryPath = jdbcDirectoryPath;
		this.androidManifestFilePath = androidManifestFilePath;

		this.propertiesFilePath = propertiesFilePath;

		this.coreTemporaryDirectoryPath = "__tempgen_core";
		this.androidTemporaryDirectoryPath = "__tempgen_android";
		this.jdbcTemporaryDirectoryPath = "__tempgen_jdbc";
		this.iosTemporaryDirectoryPath = "__tempgen_appios";

		this.baseDirectory = new File(this.baseDirectoryPath);

		if (inputFilePath != null) {
			this.inputFile = new File(baseDirectory, inputFilePath);
		} else {
			this.inputFile = new File(baseDirectory, "/res/values/sql.xml");
		}
		if (!this.inputFile.exists()) {
			throw new GeradorException("Resources SQL nao encontrados em "
					+ this.inputFile.getAbsolutePath());
		}
		this.inputFilePath = inputFile.getAbsolutePath();

		this.coreDirectory = new File(baseDirectory, coreDirectoryPath);
		this.androidDirectory = androidDirectoryPath != null ? new File(
				baseDirectory, androidDirectoryPath) : null;
		this.jdbcDirectory = jdbcDirectoryPath != null ? new File(
				baseDirectory, jdbcDirectoryPath) : null;
		this.propertiesFile = propertiesFilePath != null ? new File(
				baseDirectory, propertiesFilePath) : new File(baseDirectory,
				Constants.DEFAULT_GENERATOR_CONFIG);
		this.androidManifest = (androidManifestFilePath != null) ? new File(
				baseDirectory, androidManifestFilePath) : new File(
				baseDirectory, "/AndroidManifest.xml");
		if (!androidManifest.exists()) {
			androidManifest = null;
		}

		this.coreTemporaryDirectory = new File(baseDirectory,
				coreTemporaryDirectoryPath);
		this.androidTemporaryDirectory = new File(baseDirectory,
				androidTemporaryDirectoryPath);
		this.jdbcTemporaryDirectory = new File(baseDirectory,
				jdbcTemporaryDirectoryPath);
		this.iosTemporaryDirectory = new File(baseDirectory,
				iosTemporaryDirectoryPath);

		this.sourceDirectory = (androidDirectory != null) ? androidDirectory
				: coreDirectory;

		this.basePackage = basePackage != null ? basePackage
				: retrieveAndroidManifestBasePackage();
		this.generatedCodePackageName = "gen";
		this.generatedCodePackage = this.basePackage + "."
				+ this.generatedCodePackageName;

		this.databaseVersion = null;

	}

	public String getBasePackage() {
		return basePackage;
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
		return getBasePackageDirectoryPath() + File.separator
				+ Constants.DB_PACKAGE + File.separator + Constants.DB_CLASS
				+ ".java";
	}

	public String getBasePackageDirectoryPath() {
		return basePackage.replaceAll("\\.", File.separator);
	}

	public String getGeneratedCodePackageName() {
		return generatedCodePackageName;
	}

	public String getGeneratedCodePackage() {
		return generatedCodePackage;
	}

	public String getGeneratedPackageDirectoryPath() {
		return (getGeneratedCodePackage()).replaceAll("\\.", File.separator);
	}

	public String retrieveAndroidManifestBasePackage() throws GeradorException {
		return getBasePackageFromManifest(getAndroidManifestFile());
	}

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

	private String getBasePackageFromManifest(File androidManifest)
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

}
