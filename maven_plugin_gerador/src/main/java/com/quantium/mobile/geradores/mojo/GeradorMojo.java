package com.quantium.mobile.geradores.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.pyx4j.log4j.MavenLogAppender;
import com.quantium.mobile.geradores.Generator;
import com.quantium.mobile.geradores.GeneratorConfig;
import com.quantium.mobile.geradores.GeradorException;
import com.quantium.mobile.geradores.parsers.FileParserMapper;
import com.quantium.mobile.geradores.parsers.InputParser;
import com.quantium.mobile.geradores.parsers.InputParserRepository;
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
                Constants.DEFAULT_GENERATOR_CONFIG);
        return resource;
    }

    /**
     * @parameter
     */
    private String sqlResource;

    public File getSqlResource() throws MojoExecutionException{
        File resource = null;
        if(sqlResource!=null)
            resource = new File(basedir, sqlResource);
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
            return null;
        }
        return manifest;
    }

    /**
     * @parameter
     */
    String basePackage;
    
    private String getBasePackage()
        throws MojoExecutionException, GeradorException
    {
        File manifest = getAndroidManifest();
        if (manifest != null && manifest.exists())
            return getBasePackageFromManifest(manifest);
        return this.basePackage;
    }

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
            GeneratorConfig generatorConfig = new GeneratorConfig(
                               basePackage, sqlResource,basedir, coreSrcDir,
                               androidSrcDir, jdbcSrcDir, config, null);

            new Generator(generatorConfig)
                .generateBeansWithJsqlparserAndVelocity(defaultProperties);
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

	private String getBasePackageFromManifest(File androidManifest)
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

}
