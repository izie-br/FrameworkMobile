package com.quantium.mobile.geradores.javabean;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.framework.validation.Constraint;

/**
 * Propriedades que compoem um {@link JavaBeanSchema} ou um
 * {@link ModelSchema}
 *
 * @author Igor Soares
 */
public class Property {

    private final String name;
    private final String alias;
    private final boolean get;
    private final boolean set;
    private final Constraint constraints[];
    private Class<?> klass;
//

    /**
     * @param name
     * @param klass
     * @param get         tem permissao de leitura (getter)
     * @param set         tem permissao de escrita (getter)
     * @param constraints
     */
    public Property(String name, Class<?> klass,
                    boolean get, boolean set, Constraint... constraints) {
        this(name, klass, get, set, null, constraints);
    }

    /**
     * @param name
     * @param klass
     * @param get         tem permissao de leitura (getter)
     * @param set         tem permissao de escrita (setter)
     * @param alias       alias de serializacao (em Map)
     * @param constraints
     */
    public Property(String name, Class<?> klass, boolean get,
                    boolean set, String alias, Constraint... constraints) {
        if (constraints == null)
            constraints = new Constraint[0];
        this.name = name;
        this.klass = klass;
        this.get = get;
        this.set = set;
        this.constraints = constraints;
        this.alias = alias;
    }

    public String getNome() {
        return name;
    }

    public String getLowerCamel() {
        return CamelCaseUtils.toLowerCamelCase(name);
    }

    public String getLowerAndUnderscores() {
        return CamelCaseUtils.camelToLowerAndUnderscores(getLowerCamel());
    }

    public String getUpperAndUnderscores() {
        return CamelCaseUtils.camelToUpper(getLowerCamel());
    }

    public String getUpperCamel() {
        return CamelCaseUtils.toUpperCamelCase(name);
    }

    /**
     * Alias de serializacao
     *
     * @return alias de serializacao
     */
    public String getSerializationAlias() {
        return alias;
    }

    /**
     * Alias de serializacao
     *
     * @return alias de serializacao
     */
    public String getAlias() {
        return alias;
    }

    public Class<?> getPropertyClass() {
        return klass;
    }

    public String getKlass() {
        return klass.getSimpleName();
    }

    public void setKlass(Class<?> klass) {
        this.klass = klass;
    }

    public String getType() {
        if (Long.class.getName().equals(klass.getName()))
            return "long";
        if (Double.class.getName().equals(klass.getName()))
            return "double";
        if (Boolean.class.getName().equals(klass.getName()))
            return "boolean";
        return klass.getSimpleName();
    }

    /**
     * Boolean de permissao de leitura (getter)
     *
     * @return true se tem permissao de leitura (getter)
     */
    public boolean isGet() {
        return get;
    }

    /**
     * Boolean de permissao de escrita (setter)
     *
     * @return true se tem permissao de escrita (setter)
     */
    public boolean isSet() {
        return set;
    }

    public Constraint[] getConstraints() {
        return constraints;
    }

    public boolean isPrimaryKey() {
        for (Constraint constraint : constraints)
            if (constraint instanceof Constraint.PrimaryKey)
                return true;
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (get ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (set ? 1231 : 1237);
        result = prime * result + ((klass == null) ? 0 : klass.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Property other = (Property) obj;
        if (get != other.get)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (set != other.set)
            return false;
        if (klass == null) {
            if (other.klass != null)
                return false;
        } else if (!klass.getName().equals(other.klass.getName()))
            return false;
        return true;
    }

}
