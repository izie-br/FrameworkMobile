package com.quantium.mobile.geradores.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.quantium.mobile.geradores.GeradorDeBeans;
import com.quantium.mobile.geradores.GeradorException;


/**
 * @goal gerar
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
        try {
            new GeradorDeBeans().gerarBeansWithJsqlparserAndCodeModel(
                    getAndroidManifest(),
                    getSqlResource(),
                    basedir+getSrcFolder(),
                    "gen"
            );
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(e.getLocalizedMessage());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage());
        } catch (GeradorException e) {
            throw new MojoExecutionException(e.getLocalizedMessage());
        }
        getLog().info("finalizando gerador");
    }

    private String getSrcFolder() {
        return "/src/main/java";
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
