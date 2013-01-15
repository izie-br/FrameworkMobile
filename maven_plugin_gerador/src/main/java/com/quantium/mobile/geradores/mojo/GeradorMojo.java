package com.quantium.mobile.geradores.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.pyx4j.log4j.MavenLogAppender;
import com.quantium.mobile.geradores.Generator;
import com.quantium.mobile.geradores.GeneratorConfig;
import com.quantium.mobile.geradores.GeradorException;
import com.quantium.mobile.geradores.util.Constants;


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
     * @parameter
     */
    private String migrationsDir;

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

    /**
     * @parameter
     */
    private String sqlResource;

    /**
     * @parameter
     */
    private String androidManifestFile;

    public File getAndroidManifest() throws MojoExecutionException {
        File manifest = null;
        if(androidManifestFile!=null)
            manifest = new File(androidManifestFile);
        else
            manifest = new File(basedir+"/AndroidManifest.xml");
        if (!manifest.exists()) {
            return null;
        }
        return manifest;
    }

    /**
     * @parameter
     */
    String basePackage;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
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
            defaultProperties.put(Constants.PROPERTIY_IGNORED,ignored);
        Map<String, String> aliases = getAliases();
        if (aliases != null)
            defaultProperties.put(Constants.PROPERTIY_SERIALIZATION_ALIAS,
                                  aliases);
        try {
            String androidManifestPath = (getAndroidManifest() != null) ?
                getAndroidManifest().getAbsolutePath():
                null;
            GeneratorConfig generatorConfig = GeneratorConfig.builder()
                .setBasePackage(basePackage)
                .setInputFile(sqlResource)
                .setBaseDirectory(basedir)
                .setCoreDirectory(coreSrcDir)
                .setAndroidDirectory(androidSrcDir)
                .setJdbcDirectory(jdbcSrcDir)
                .setPropertiesFile(config)
                .setAndroidManifest(androidManifestPath)
                .setMigrationsOutput (this.migrationsDir)
                .create();

            new Generator(generatorConfig)
                .generate(defaultProperties);
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

}
