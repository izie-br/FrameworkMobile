package com.quantium.mobile.geradores.velocity;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoManyToMany;
import com.quantium.mobile.geradores.filters.associacao.AssociacaoOneToMany;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.Property;
import com.quantium.mobile.geradores.util.ColumnsUtils;
import com.quantium.mobile.geradores.util.Constants;
import com.quantium.mobile.geradores.velocity.helpers.AssociationHelper.AssociationHolder;
import com.quantium.mobile.geradores.velocity.helpers.GetterHelper;
import com.quantium.mobile.geradores.velocity.helpers.ManyToManyAssociationHelper;
import com.quantium.mobile.geradores.velocity.helpers.OneToManyAssociationHelper;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.quantium.mobile.geradores.velocity.helpers.AssociationHelper.findAssociations;
import static com.quantium.mobile.geradores.velocity.helpers.AssociationHelper.getAssociationsForFK;

public class VelocityObjcFactory {

    private Template template;
    private File targetDirectory;
    private VelocityContext parentCtx;
    private Map<String, String> aliases;

    public VelocityObjcFactory(VelocityEngine ve, File targetDirectory,
                               String basePackage, String genPackage,
                               Map<String, String> serializationAliases) {
        this.template = ve.getTemplate("VO.h");
        this.targetDirectory = targetDirectory;
        this.parentCtx = new VelocityContext();
        this.parentCtx.put("defaultId", Constants.DEFAULT_ID);
        this.parentCtx.put("package", CamelCaseUtils.toUpperCamelCase(genPackage.replace('.', '_')));
        this.parentCtx.put("basePackage", CamelCaseUtils.toUpperCamelCase(basePackage.replace('.', '_')));
        this.parentCtx.put("getter", new GetterHelper());
        this.parentCtx.put("Type", new ObjcTypes());
        this.parentCtx.put("J2ObjcType", new J2ObjcType());
        this.parentCtx.put("J2ObjcTypeName", new J2ObjcTypeNames());
        this.parentCtx.put("MapType", new MapTypes());
        this.parentCtx.put("MapKey", new MapKey());
        this.parentCtx.put("TypeName", new ObjcTypeNames());
        this.aliases = serializationAliases;
    }

    public static String getAlias(Map<String, String> aliases,
                                  String classname, String field) {
        String name = classname + '.' + field;
        if (aliases != null) {
            for (String k : aliases.keySet()) {
                if (CamelCaseUtils.camelEquals(name, k)) {
                    return aliases.get(k);
                }
            }
        }
        return field;
    }

    public void generateVO(
            JavaBeanSchema schema, Collection<JavaBeanSchema> allSchemas,
            VelocityObjcFactory.Type type)
            throws IOException {
        String classname = CamelCaseUtils.toUpperCamelCase(schema.getNome());
        String editableInterfaceName = classname + "Editable";
        String filename = null;
        String extension = ".h";
        VelocityContext ctx = new VelocityContext(parentCtx);
        switch (type) {
            case PROTOCOL:
                ctx.put("Protocol", true);
                filename = classname;
                break;
            case PROTOCOL_IMPL:
                ctx.put("ProtocolImpl", true);
                filename = classname;
                extension = ".m";
                break;
            case EDITABLE_PROTOCOL:
                ctx.put("EditableProtocol", true);
                filename = editableInterfaceName;
                break;
            case INTERFACE:
                ctx.put("Interface", true);
                filename = classname + "Impl";
                break;
            case IMPLEMENTATION:
                ctx.put("Implementation", true);
                filename = classname + "Impl";
                extension = ".m";
        }
        ctx.put("Filename", filename);
        ctx.put("EditableInterfaceName", editableInterfaceName);
        ctx.put("table", schema.getTabela().getName());
        ctx.put("Klass", classname);
        ctx.put("serialVersionUID", "" + generateSerialUID(schema) + "L");
        AssociationHolder holder = findAssociations(schema, allSchemas);
        ArrayList<OneToManyAssociationHelper> oneToMany = holder.getOneToMany();
        ArrayList<ManyToManyAssociationHelper> manyToMany = holder.getManyToMany();
        ArrayList<OneToManyAssociationHelper> manyToOne = holder.getManyToOne();
        ctx.put("manyToOneAssociations", manyToOne);
        ctx.put("oneToManyAssociations", oneToMany);
        ctx.put("manyToManyAssociations", manyToMany);
        List<Object> toMany = new ArrayList<Object>();
        toMany.addAll(oneToMany);
        toMany.addAll(manyToMany);
        ctx.put("toManyAssociations", toMany);
        List<Property> fields = new ArrayList<Property>();
        for (String col : ColumnsUtils.orderedColumnsFromJavaBeanSchema(schema)) {
            Property prop = schema.getPropriedade(col);
            if (prop.getLowerCamel().equals("id"))
                prop = new PropId(prop);
            prop = new Property(
                    prop.getNome(), prop.getPropertyClass(),
                    prop.isGet(), prop.isSet(),
                    getAlias(aliases, classname, col),
                    prop.getConstraints());

            fields.add(prop);
        }
        ctx.put("fields", fields);
        ctx.put("primaryKey", schema.getPrimaryKey());

        Map<Property, OneToManyAssociationHelper> associationsFromFK =
                getAssociationsForFK(fields, manyToOne);
        ctx.put("associationForField", associationsFromFK);

        File file = new File(targetDirectory, filename + extension);
        Writer w = new OutputStreamWriter(
                new FileOutputStream(file),
                "UTF-8");
        template.merge(ctx, w);
        w.close();
    }

    private long generateSerialUID(JavaBeanSchema schema) {
        long result = 1;
        Collection<String> columns = schema.getColunas();
        for (String key : columns) {
            Property prop = schema.getPropriedade(key);
            // este e o algoritmo de gerar o numero arbitrario
            // esta linha pode ser alterada com algo que faca sentido
            result += result * prop.getNome().hashCode() +
                    prop.getPropertyClass().getName().hashCode();
        }
        for (Associacao assoc : schema.getAssociacoes()) {
            String other = assoc.getTabelaA().getName();
            boolean hasmany = assoc instanceof AssociacaoManyToMany;
            if (other.equals(schema.getTabela().getName())) {
                other = assoc.getTabelaB().getName();
                if (assoc instanceof AssociacaoOneToMany)
                    hasmany = true;
            }
            // este e o algoritmo de gerar o numero arbitrario
            // esta linha pode ser alterada com algo que faca sentido
            result += other.hashCode() + (hasmany ? result : 0);
        }
        return result;
    }

    public enum Type {PROTOCOL, PROTOCOL_IMPL, EDITABLE_PROTOCOL, INTERFACE, IMPLEMENTATION}

    ;

    public static class ObjcTypes {
        public String get(Property prop) {
            String type = prop.getType();
            if (type.equals("String"))
                return "NSString";
            if (type.equals("boolean"))
                return "BOOL";
            if (type.equals("Date"))
                return "NSDate";
            if (type.equals("long"))
                return "long long";
            return type;
        }
    }

    public static class ObjcTypeNames extends ObjcTypes {

        @Override
        public String get(Property prop) {
            String type = prop.getType();
            if (type.equals("long"))
                return "LongInt";
            return super.get(prop);
        }
    }

    public static class J2ObjcType extends ObjcTypes {
        @Override
        public String get(Property prop) {
            String type = prop.getType();
            if (type.equals("Date"))
                return "JavaUtilDate";
            return super.get(prop);
        }
    }

    public static class J2ObjcTypeNames extends J2ObjcType {
        @Override
        public String get(Property prop) {
            String type = prop.getType();
            if (type.equals("long"))
                return new ObjcTypeNames().get(prop);
            return super.get(prop);
        }
    }

    public static class MapTypes {
        public String get(Property prop) {
            String type = prop.getType();
            if (type.equals("String"))
                return "NSString";
            if (type.equals("boolean"))
                return "JavaLangBoolean";
            if (type.equals("Date"))
                return "JavaLangDate";
            if (type.equals("long"))
                return "JavaLangLong";
            if (type.equals("double"))
                return "JavaLangDouble";
            Method method = new Object() {
            }.getClass().getEnclosingMethod();
            return "/*" + type + " not found in " +
                    method.getDeclaringClass().getName() + "::" +
                    method.getName() + "*/";
        }
    }

    public static class MapKey {
        public String get(Property prop) {
            String key = prop.getLowerAndUnderscores();
            if (key.equals("id_"))
                return "id";
            return key;
        }
    }

    /**
     * Classe para usada para adicionar um underscore '_' em LowerCamel;
     * Util para fazer o  escape de "id" para "id_"
     *
     * @author Igor Soares
     */
    public static class PropId extends Property {

        Property wrapped;

        public PropId(Property prop) {
            super(prop.getNome(), prop.getPropertyClass(), prop.isGet(),
                    prop.isSet(), prop.getConstraints());
            wrapped = prop;
        }

        @Override
        public String getLowerCamel() {
            return wrapped.getLowerCamel() + '_';
        }

        @Override
        public String getLowerAndUnderscores() {
            return wrapped.getLowerAndUnderscores() + '_';
        }

        @Override
        public String getUpperAndUnderscores() {
            return wrapped.getUpperAndUnderscores();
        }

        @Override
        public String getUpperCamel() {
            return wrapped.getUpperCamel();
        }

    }

}
