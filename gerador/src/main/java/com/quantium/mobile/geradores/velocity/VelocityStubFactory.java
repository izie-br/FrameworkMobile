package com.quantium.mobile.geradores.velocity;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.framework.utils.StringUtil;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;
import com.quantium.mobile.geradores.util.ColumnsUtils;
import com.quantium.mobile.geradores.util.Constants;
import com.quantium.mobile.geradores.util.LoggerUtil;
import com.quantium.mobile.geradores.velocity.helpers.*;
import com.quantium.mobile.geradores.velocity.helpers.AssociationHelper.AssociationHolder;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.quantium.mobile.geradores.velocity.helpers.AssociationHelper.findAssociations;

public class VelocityStubFactory {

    private StubType type;
    private String basePackage;
    private String genPackage;
    private String voPackage;
    private Collection<JavaBeanSchema> allSchemas;
    private File targetDirectory;
    private Template template;
    public VelocityStubFactory(
            VelocityEngine ve, StubType type, String basePackage,
            String genPackage, String voPackage, File targetDirectory,
            Collection<JavaBeanSchema> allSchemas) {
        this.type = type;
        this.basePackage = basePackage;
        this.genPackage = genPackage;
        this.voPackage = voPackage;
        this.targetDirectory = targetDirectory;
        this.template = ve.getTemplate(type.getTemplateName());
        this.allSchemas = allSchemas;
    }

    public boolean isStubFound(JavaBeanSchema schema) {
        File classFile = getClassFileFor(schema);
        try {
            boolean stubExists = classFile.exists();
            if (!stubExists) {
                LoggerUtil.getLog().info(String.format(
                        "Arquivo stub %s nao encontrado.",
                        classFile.getPath()));
            }
            return stubExists;
        } catch (Exception e) {
            LoggerUtil.getLog().error(StringUtil.getStackTrace(e));
        }
        return false;
    }

    public void createStubFor(JavaBeanSchema schema) {
        File f = getClassFileFor(schema);
        VelocityContext ctx = new VelocityContext();
        ctx.put("package", getPackageFor(schema));
        ctx.put("Filename", this.type.getNameFor(schema));

        List<Property> fields = new ArrayList<Property>();
        Property primaryKey = schema.getPrimaryKey();
        for (String col : ColumnsUtils.orderedColumnsFromJavaBeanSchema(schema)) {
            Property prop = schema.getPropriedade(col);
            fields.add(prop);
        }
        ctx.put("fields", fields);
        ctx.put("primaryKey", primaryKey);
        AssociationHolder holder = findAssociations(schema, allSchemas);
        ArrayList<OneToManyAssociationHelper> oneToMany = holder.getOneToMany();
        ArrayList<ManyToManyAssociationHelper> manyToMany = holder.getManyToMany();
        ArrayList<OneToManyAssociationHelper> manyToOne = holder.getManyToOne();
        Map<Property, OneToManyAssociationHelper> associationsMap =
                AssociationHelper.getAssociationsForFK(fields, manyToOne);

        ConstructorArgsHelper argsHelper = new ConstructorArgsHelper(
                schema, fields, associationsMap, oneToMany, manyToMany);
        ctx.put("constructorArgs", argsHelper.getConstructorArguments());
//		ctx.put("constructorArgsDecl", argsHelper.getConstructorArgsDecl());
        LoggerUtil.getLog().info("Criando " + f.getPath());

        ctx.put("interface", (this.type == StubType.INTERFACE));
        ctx.put("implementation", (this.type == StubType.IMPLEMENTATION));
        ctx.put("GenImpl", VelocityVOFactory.Type.IMPLEMENTATION.getFilenameFor(schema));
        ctx.put("GenInterface", VelocityVOFactory.Type.INTERFACE.getFilenameFor(schema));

        String importsStr = getImportsFor(schema, oneToMany, manyToOne, manyToMany);
        ctx.put("Imports", importsStr);

        Writer writer;
        try {
            writer = new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(f)),
                    "UTF-8");
            this.template.merge(ctx, writer);
            writer.close();
        } catch (IOException e) {
            LoggerUtil.getLog().error(StringUtil.getStackTrace(e));
            return;
        }
    }

    private String getImportsFor(
            JavaBeanSchema schema,
            Collection<OneToManyAssociationHelper> oneToMany,
            Collection<OneToManyAssociationHelper> manyToOne,
            Collection<ManyToManyAssociationHelper> manyToMany) {
        ImportHelper helper = new ImportHelper(basePackage, genPackage, voPackage, null);

        StringBuilder sb = new StringBuilder();
        sb.append(helper.getImports(schema, allSchemas, oneToMany, manyToOne, manyToMany));
        sb.append("import ");
        sb.append(this.basePackage);
        sb.append('.');
        String module = schema.getModule();
        if (!Constants.DEFAULT_MODULE_NAME.equals(module)) {
            sb.append(module);
            sb.append('.');
        }
        sb.append("gen.");
        switch (this.type) {
            case INTERFACE:
                sb.append(VelocityVOFactory.Type.INTERFACE.getFilenameFor(schema));
                break;
            case IMPLEMENTATION:
                sb.append(VelocityVOFactory.Type.IMPLEMENTATION.getFilenameFor(schema));
                break;
            default:
                throw new RuntimeException();
        }
        sb.append(';');
        return sb.toString();
    }

    private File getClassFileFor(JavaBeanSchema schema) {
        String module = schema.getModule();
        File packageDir = targetDirectory;
        if (!module.equals(Constants.DEFAULT_MODULE_NAME)) {
            packageDir = new File(packageDir, module);
        }
        if (this.voPackage != null) {
            packageDir = new File(packageDir, this.voPackage);
        }
        if (!packageDir.exists()) {
            packageDir.mkdir();
        }
        String classFileName = this.type.getNameFor(schema) + ".java";
        File classFile = new File(packageDir, classFileName);
        return classFile;
    }

    private String getPackageFor(JavaBeanSchema schema) {
        StringBuilder baseGenPackage = new StringBuilder(this.basePackage);
        String module = schema.getModule();
        if (!module.equals(Constants.DEFAULT_MODULE_NAME)) {
            baseGenPackage.append(".");
            baseGenPackage.append(module);
        }
        if (this.voPackage != null) {
            baseGenPackage.append(".");
            baseGenPackage.append(this.voPackage);
        }
        return baseGenPackage.toString();
    }

    public static enum StubType {
        INTERFACE, IMPLEMENTATION, DAO;

        public String getTemplateName() {
            switch (this) {
                case INTERFACE:
                case IMPLEMENTATION:
                    return "VoStub.java";
                case DAO:
                    return "DaoStub.java";
                default:
                    throw new RuntimeException();
            }
        }

        public String getNameFor(JavaBeanSchema schema) {
            String nameUpper = CamelCaseUtils.toUpperCamelCase(schema.getNome());
            switch (this) {
                case INTERFACE:
                    return nameUpper;
                case IMPLEMENTATION:
                    return nameUpper + "Impl";
                case DAO:
                    //TODO
                default:
                    throw new RuntimeException();
            }
        }

    }

/*
    private String getBaseGenPackageFor (JavaBeanSchema schema) {
		StringBuilder baseGenPackage = new StringBuilder(this.basePackage);
		String module = schema.getModule();
		if (!module.equals(Constants.DEFAULT_MODULE_NAME)) {
			baseGenPackage.append(".");
			baseGenPackage.append(module);
		}
		if (this.genPackage != null) {
			baseGenPackage.append(".");
			baseGenPackage.append(this.genPackage);
		}
		return baseGenPackage.toString();
	}
*/
}
