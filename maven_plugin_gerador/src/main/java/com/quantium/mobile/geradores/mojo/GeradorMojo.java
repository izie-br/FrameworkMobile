package com.quantium.mobile.geradores.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.pyx4j.log4j.MavenLogAppender;
import com.quantium.mobile.geradores.Generator;
import com.quantium.mobile.geradores.GeradorException;


/**
 * @goal gerar
 * @requiresProject false
 */
public class GeradorMojo extends AbstractMojo{

	private static final String PROPRIEDADE_INDEFINIDA_ERRMSG = "propriedade '%s' indefinida";

	/**
     * @parameter expression="${basedir}"
     */
    private String basedir;

    /**
     * @parameter
     */
    private String coreSrcDir;

    /**
     * @parameter
     */
    private String androidSrcDir;

    /**
     * @parameter
     */
    private String jdbcSrcDir;

    /**
     * The Maven Session.
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession mavenSession;

    /**
     * @parameter expression="false"
     */
    protected boolean runOnRootOnly;

    /**
     * @parameter
     */
    private String config;

    /**
     * @parameter
     */
    private List<String> ignore;

    private String getIgnored(){
        if (ignore == null || ignore.size() < 1)
            return null;
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = ignore.iterator();
        for (;;){
            sb.append(it.next());
            if (!it.hasNext())
                break;
            sb.append(',');
        }
        return sb.toString();
    }

    /**
     * @parameter
     */
    private Map<String,String> serializationAlias;

    private Map<String,String> getAliases(){
        return serializationAlias;
    }

    private File getConfigResource(){
        File resource;
        if (config != null)
            resource = new File(config);
        else
            resource = new File(
                basedir + "/" +
                Generator.DEFAULT_GENERATOR_CONFIG);
        return resource;
    }

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
        if (runOnRootOnly  && !isThisTheExecutionRoot()){
            getLog().info("not root");
            return;
        }

        MavenLogAppender.startPluginLog(this);
        Logger log = Logger.getLogger(getClass().getName());
        log.info("iniciando gerador");

        if (basedir == null)
            throw new MojoFailureException(String.format(PROPRIEDADE_INDEFINIDA_ERRMSG, "basedir"));
        if (coreSrcDir == null)
            throw new MojoFailureException(String.format(PROPRIEDADE_INDEFINIDA_ERRMSG, "coreSrcDir"));
        if (androidSrcDir == null && jdbcSrcDir == null)
            throw new MojoFailureException(String.format(
                    PROPRIEDADE_INDEFINIDA_ERRMSG,
                    "androidSrcDir OR jdbcSrcDir"));

        Map<String,Object> defaultProperties = new HashMap<String,Object>();
        String ignored = getIgnored();
        if (ignored != null)
            defaultProperties.put(Generator.PROPERTIY_IGNORED,ignored);
        Map<String, String> aliases = getAliases();
        if (aliases != null)
            defaultProperties.put(Generator.PROPERTIY_SERIALIZATION_ALIAS,
                                  aliases);
        try {
            new Generator().generateBeansWithJsqlparserAndVelocity(
                    getAndroidManifest(),
                    getSqlResource(),
                    new File(basedir, coreSrcDir),
                    (androidSrcDir == null) ?
                        null :
                        new File(basedir, androidSrcDir),
                    (jdbcSrcDir == null) ?
                        null:
                        new File(basedir, jdbcSrcDir),
                    "gen",
                    getConfigResource(),
                    defaultProperties
            );
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(e.getLocalizedMessage());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage());
        } catch (GeradorException e) {
            throw new MojoExecutionException(e.getLocalizedMessage());
        }
        log.info("finalizando gerador");
        MavenLogAppender.endPluginLog(this);
    }

    // Para rodar apenas no projeto pai
    protected boolean isThisTheExecutionRoot()
    {
        Log log = getLog();
        log.debug("Root Folder:" + mavenSession.getExecutionRootDirectory());
        log.debug("Current Folder:"+ basedir );
        boolean result = mavenSession.getExecutionRootDirectory().equalsIgnoreCase(basedir.toString());
        if (result)
        {
            log.debug( "This is the execution root." );
        }
        else
        {
            log.debug( "This is NOT the execution root." );
        }
        return result;
    }

/*    private Properties getPropertiesFile() throws MojoFailureException{
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
*/

}
