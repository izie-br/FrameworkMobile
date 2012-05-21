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

import br.com.cds.mobile.geradores.GeradorDeBeans;

/**
 * @goal checar
 * @requiresProject false
 */
public class GeradorMojo extends AbstractMojo{

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
        if (!resource.exists())
            throw new MojoExecutionException("Manifest nao encontrado em "+resource.getAbsolutePath());
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
        if (!manifest.exists())
            throw new MojoExecutionException("Manifest nao encontrado em "+manifest.getAbsolutePath());
        return manifest;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("iniciando gerador");
        Integer dbVersion = getDBVersion();
        if(dbVersion!=null)
            getLog().info("dbversion: "+ dbVersion.toString());
        String basePackage = getBasePackage();
        if(basePackage!=null)
            getLog().info("package "+basePackage);
        String val = getSqlTill(1);
        if(val!=null){
            val = sqliteSchema(val);
            getLog().info(val);
            try {
                GeradorDeBeans.gerarBeansWithJsqlparserAndCodeModel(
                        basePackage,
                        new StringReader(val),
                        "customSrc",
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

    public Integer getDBVersion() throws MojoFailureException{
        Properties props = getPropertiesFile();
        try {
            return Integer.parseInt(props.getProperty("DBVersion"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String sqliteSchema(String sql){
        try {
//            File f = new File("__schema.init");
//            if(f.exists())
//                f.delete();
//            f.createNewFile();
//            FileOutputStream out = new FileOutputStream(f);
//            PrintWriter pw = new PrintWriter(out);
//            pw.print(sql);
//            pw.close();
//            Process p = Runtime.getRuntime().exec("sqlite3 __temp.db ' "+sql+"';"+"sqlite3 __temp.db "+".schema");
//            //InputStream is = p.getInputStream();
//            p.waitFor();
//            String schema = "";//convertStreamToString(is);
            return SQLiteUtil.getSchema(sql);
        } catch (SQLException e) {
        	e.printStackTrace();
			throw new RuntimeException(e);
		}
    }

    public String getSqlTill(Integer version) throws MojoExecutionException{
        StringBuilder sb = new StringBuilder();
        Map<String, String> nodes = XMLUtil.getChildren(
            getSqlResource(),
            "//string[contains(@name,\"db_versao_\") and number(substring(@name,11)) < "+(version+1)+"]"
        );
        getLog().info(sb.toString());
        StringBuilder out = new StringBuilder();
        for(String key : nodes.keySet())
            out.append(nodes.get(key));
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
                    "UTF-8"
                );
                out.close();
            }
            return props;
        }
        catch (IOException e) {
            throw new MojoFailureException("Arquivo de configuracao inacessivel");
        }
    }

    private String getBasePackage() throws MojoExecutionException{
        try {
            File manifest = getAndroidManifest();
            Pattern pat = Pattern.compile(
                    ".*<manifest[^>]*package=\"([^\"]*)\"[^>]*>.*",
                    Pattern.MULTILINE
            );
            String manifestStr = convertStreamToString(new FileInputStream(manifest));
            Matcher mobj = pat.matcher(manifestStr);
            if(mobj.find())
                return mobj.group(1);
            return null;
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage());
        }
    }

    private static String convertStreamToString(InputStream is) throws IOException {
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
                        new InputStreamReader(is,"UTF-8")
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
