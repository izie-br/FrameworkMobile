package br.com.cds.mobile.geradores.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import br.com.cds.mobile.geradores.CodeModelBeanFactory;
import br.com.cds.mobile.geradores.GeradorDeBeans;
import br.com.cds.mobile.geradores.util.SQLiteUtil;
import br.com.cds.mobile.geradores.util.XMLUtil;

/**
 * @goal checar
 * @requiresProject false
 */
public class GeradorMojo extends AbstractMojo{

    private static final String DEFAULT_ENCODING = "UTF-8";

	/**
     * @parameter expression="${basedir}"
     */
    private String basedir;

    /**
     * @parameter
     */
    private String sqlResource;

    public File getSqlResource() throws MojoExecutionException{
        File resource = null;
        if(sqlResource!=null)
            resource = new File(sqlResource);
        else
            resource = new File(basedir+"/res/values/sql.xml");
        if (!resource.exists()){
            throw new MojoExecutionException(
                    "Resources SQL nao encontrados em "+
                    resource.getAbsolutePath()
            );
        }
        return resource;
    }

    /**
     * @parameter
     */
    private String manifestFile;

    public File getAndroidManifest() throws MojoExecutionException {
        File manifest = null;
        if(manifestFile!=null)
            manifest = new File(manifestFile);
        else
            manifest = new File(basedir+"/AndroidManifest.xml");
        if (!manifest.exists()) {
            throw new MojoExecutionException(
                    "Manifest nao encontrado em "+
                     manifest.getAbsolutePath()
            );
        }
        return manifest;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        getLog().info("iniciando gerador");
        Integer dbVersion = getDBVersion();
        if(dbVersion==null)
            throw new MojoFailureException("versao do banco nao encontrada");
        String basePackage = getBasePackage();
        if(basePackage!=null)
            getLog().info("package "+basePackage);
        String val = getSqlTill(dbVersion);
        if(val!=null){
            val = sqliteSchema(val);
            getLog().info(val);
            try {
                GeradorDeBeans.gerarBeansWithJsqlparserAndCodeModel(
                        basePackage,
                        new StringReader(val),
                        basedir+getSrcFolder(),
                        basePackage+".gen"
                );
            } catch (FileNotFoundException e) {
                throw new MojoExecutionException(e.getLocalizedMessage());
            } catch (IOException e) {
                throw new MojoExecutionException(e.getLocalizedMessage());
            }
        }
        getLog().info("finalizando gerador");
    }

	private String getSrcFolder() {
		return "/src/main/java";
	}

    public Integer getDBVersion() throws MojoFailureException{
        String packageFolder;
        try {
            packageFolder = getBasePackage().replaceAll("\\.", File.separator);
        } catch (MojoExecutionException e) {
            throw new MojoFailureException(e.getLocalizedMessage());
        }
        File dbFile = new File(
            basedir + File.separator + getSrcFolder() + File.separator +
            packageFolder +
            File.separator + GeradorDeBeans.DB_PACKAGE + File.separator +
            GeradorDeBeans.DB_CLASS + ".java"
        );
        if (!dbFile.exists()) {
           getLog().error(dbFile.getAbsolutePath());
        }
        Scanner scan;
        try {
            scan = new Scanner(dbFile);
        } catch (FileNotFoundException e) {
            throw new MojoFailureException (e.getLocalizedMessage());
        }
        Pattern dbVersionPattern = Pattern.compile(
            "DB_VERSAO\\s*=\\s*([^;]);"
        );
        int versionNumberGroup = 1;
        String s = scan.findWithinHorizon(
            dbVersionPattern,
            0
        );
        if (s == null)
            getLog().error("DB_VERSAO nao encontrado");
        
        Matcher mobj = dbVersionPattern.matcher(s);
        mobj.find();
        Integer ver = Integer.parseInt(mobj.group(versionNumberGroup));
        return ver;/*
        Properties props = getPropertiesFile();
        try {
            return Integer.parseInt(props.getProperty("DBVersion"));
        } catch (NumberFormatException e) {
            return null;
        }*/
    }

    public String sqliteSchema(String sql){
        try {
            return SQLiteUtil.getSchema(sql);
        } catch (SQLException e) {
        	e.printStackTrace();
			throw new RuntimeException(e);
		}
    }

    public String getSqlTill(Integer version) throws MojoExecutionException{
        StringBuilder out = new StringBuilder();
        for (int i = 0; i <= version ; i++) {
            Map<String, String> nodes = XMLUtil.getChildren(
                getSqlResource(),
                "//string[contains(@name,\"db_versao_\") and " +
                "number(substring(@name,11)) = "+i+"]"
            );
            for(String key : nodes.keySet())
                out.append(nodes.get(key));
            }
        return out.toString();
    }

    private Properties getPropertiesFile() throws MojoFailureException{
        try {
            File configFile = new File(basedir+"/.gerador.xml");
            if(!configFile.exists()){
                configFile.createNewFile();
            }
            Properties props = new Properties();
            try {
                props.loadFromXML(new FileInputStream(configFile));
            } catch (InvalidPropertiesFormatException e) {
                props = new Properties();
                FileOutputStream out = new FileOutputStream(configFile);
                props.storeToXML(
                    out,
                    null,
                    DEFAULT_ENCODING
                );
                out.close();
            }
            return props;
        }
        catch (IOException e) {
            throw new MojoFailureException(
                    "Arquivo de configuracao inacessivel");
        }
    }

    private String getBasePackage() throws MojoExecutionException{
        try {
            File manifest = getAndroidManifest();
            Pattern pat = Pattern.compile(
                    ".*<manifest[^>]*package=\"([^\"]*)\"[^>]*>.*",
                    Pattern.MULTILINE
            );
            String manifestStr = convertStreamToString(
                    new FileInputStream(manifest));
            Matcher mobj = pat.matcher(manifestStr);
            if(mobj.find())
                return mobj.group(1);
            return null;
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage());
        }
    }

    private static String convertStreamToString(InputStream is)
            throws IOException
    {
        /*
         * To convert the InputStream to String we use the Reader.read(char[]
         * buffer) method. We iterate until the Reader return -1 which means
         * there's no more data to read. We use the StringWriter class to
         * produce the string.
         */
        if (is != null) {
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
}
