package com.quantium.mobile.geradores.velocity;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;
import com.quantium.mobile.geradores.util.ColumnsUtils;
import com.quantium.mobile.geradores.util.Constants;
import com.quantium.mobile.geradores.velocity.helpers.*;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.quantium.mobile.geradores.velocity.Utils.*;
import static com.quantium.mobile.geradores.velocity.helpers.AssociationHelper.*;

public class VelocityDaoFactory {

    private Type type;
    private String genPackage;
    private String voPackage;
    private String basePackage;
    private File targetDirectory;
    private Template template;
    private VelocityContext parentCtx;
    private Map<String, String> aliases;
    public VelocityDaoFactory(VelocityEngine ve, File targetDirectory, Type type, String basePackage,
                              String genPackage, String voPackage, Map<String, String> serializationAliases) {
        // this.ve = ve;
        this.type = type;
        this.targetDirectory = targetDirectory;
        template = ve.getTemplate(type.getTemplateName());
        this.basePackage = basePackage;
        this.genPackage = genPackage;
        this.voPackage = voPackage;
        parentCtx = new VelocityContext();
        parentCtx.put("defaultId", Constants.DEFAULT_ID);
        parentCtx.put("getter", new GetterHelper());
        this.aliases = serializationAliases;
        // parentCtx.put("basePackage", basePackage);
    }

    public void generate(JavaBeanSchema schema, Collection<JavaBeanSchema> allSchemas) throws IOException {
        if (schema.isNonEntityTable())
            return;
        String targetclass = CamelCaseUtils.toUpperCamelCase(schema.getNome());
        String classname = targetclass + this.type.getSuffix();
        String filename = classname + ".java";
        File file = new File(getPackageDir(targetDirectory, genPackage, schema.getModule()), filename);
        VelocityContext ctx = new VelocityContext(parentCtx);
        ctx.put("package", Utils.getPackageName(basePackage, genPackage, schema.getModule()));
        ctx.put("Klass", classname);
        ctx.put("EditableInterface", editableInterface(targetclass));
        ctx.put("KlassImpl", targetclass + "Impl");
        ctx.put("Target", targetclass);
        ctx.put("table", schema.getTabela().getName());

        String daoFactory = type.getDaoFactory();
        ctx.put("DaoFactory", daoFactory);

        List<Property> fields = new ArrayList<Property>();
        Property primaryKey = schema.getPrimaryKey();
        for (String col : ColumnsUtils.orderedColumnsFromJavaBeanSchema(schema)) {
            Property f = schema.getPropriedade(col);
            f = new Property(f.getNome(), f.getPropertyClass(), f.isGet(), f.isSet(), VelocityVOFactory.getAlias(
                    aliases, targetclass, col), f.getConstraints());

            fields.add(f);
        }
        ctx.put("fields", fields);
        ctx.put("primaryKey", primaryKey);
        AssociationHolder holder = findAssociations(schema, allSchemas);
        ArrayList<OneToManyAssociationHelper> oneToMany = holder.getOneToMany();
        ArrayList<ManyToManyAssociationHelper> manyToMany = holder.getManyToMany();
        ArrayList<OneToManyAssociationHelper> manyToOne = holder.getManyToOne();
        ctx.put("oneToManyAssociations", oneToMany);
        ctx.put("manyToManyAssociations", manyToMany);
        ctx.put("manyToOneAssociations", manyToOne);
        List<Object> toMany = new ArrayList<Object>();
        toMany.addAll(oneToMany);
        toMany.addAll(manyToMany);
        ctx.put("toManyAssociations", toMany);
        // ctx.put("ForeignKeys", getForeignKeys(schema));

        int options = getOptions(schema);
        ctx.put("hasNullableAssociation", (options & HAS_NULLABLE_ASSOCIATION) != 0);
        ctx.put("hasNotNullableAssociation", (options & HAS_NOT_NULLABLE_ASSOCIATION) != 0);
        ctx.put("hasDateField", (options & HAS_DATE_FIELD) != 0);

        Map<Property, OneToManyAssociationHelper> associationsFromFK = getAssociationsForFK(fields, manyToOne);

        ConstructorArgsHelper argsHelper = new ConstructorArgsHelper(schema, fields, associationsFromFK, oneToMany,
                manyToMany);

        ImportHelper importHelper = new ImportHelper(basePackage, genPackage, voPackage, daoFactory);
        String klassPackage = Utils.getPackageName(basePackage, voPackage, schema.getModule());
        String importsStr = importHelper.getImports(schema, allSchemas, oneToMany, manyToOne, manyToMany);
        importsStr += String.format("import %s.%s;\n", klassPackage, targetclass);
        importsStr += String.format("import %s.%sImpl;", klassPackage, targetclass);
        ctx.put("Imports", importsStr);

        ctx.put("associationForField", associationsFromFK);
        ctx.put("constructorArgs", argsHelper.getConstructorArgumentsForDAO());
        ctx.put("constructorArgsDecl", argsHelper.getConstructorArgsDecl());
        ctx.put("constructorSetters", argsHelper.getConstructorSetters());

        ValidateHelper vhelper = new ValidateHelper(schema, fields);
        ctx.put("Uniques", vhelper.getUniques());

        Writer w = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        template.merge(ctx, w);
        w.close();
    }

    public enum Type {

        ANDROID, JDBC;

        public String getTemplateName() {
            switch (this) {
                case ANDROID:
                    return "DAO.java";
                case JDBC:
                    return "JdbcDao.java";
                default:
                    throw new RuntimeException();
            }
        }

        public String getSuffix() {
            switch (this) {
                case ANDROID:
                    return "DAOSQLite";
                case JDBC:
                    return "JdbcDAO";
                default:
                    throw new RuntimeException();
            }
        }

        public String getDaoFactory() {
            switch (this) {
                case ANDROID:
                    return "SQLiteDAOFactory";
                case JDBC:
                    return "JdbcDAOFactory";
                default:
                    throw new RuntimeException();
            }
        }

    }

}
