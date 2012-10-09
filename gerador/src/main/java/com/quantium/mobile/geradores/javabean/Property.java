package com.quantium.mobile.geradores.javabean;

import com.quantium.mobile.framework.utils.CamelCaseUtils;

public final class Property {

	private String name;
	private Class<?> klass;
	private String alias;
	private boolean get;
	private boolean set;

	public Property(String name, Class<?> klass,
	                   boolean get, boolean set)
	{
		this.name = name;
		this.klass = klass;
		this.get = get;
		this.set = set;
	}

	public Property(String name, Class<?> klass,
	                   boolean get, boolean set, String alias)
	{
		this(name, klass, get, set);
		this.alias = alias;
	}

	public String getNome() {
		return name;
	}

	public String getLowerCamel(){
		return CamelCaseUtils.toLowerCamelCase(name);
	}

	public String getLowerAndUnderscores(){
		return CamelCaseUtils.camelToLowerAndUnderscores(getLowerCamel());
	}

	public String getUpperAndUnderscores(){
		return CamelCaseUtils.camelToUpper(getLowerCamel());
	}

	public String getUpperCamel(){
		return CamelCaseUtils.toUpperCamelCase(name);
	}

	public String getSerializationAlias(){
		return alias;
	}


	public String getAlias(){
		return alias;
	}

	public void setAlias(String alias){
		this.alias = alias;
	}

	public Class<?> getPropertyClass() {
		return klass;
	}

	public String getKlass(){
		return klass.getSimpleName();
	}

	public String getType(){
		if (Long.class.getName().equals(klass.getName()))
			return "long";
		if (Double.class.getName().equals(klass.getName()))
			return "double";
		if (Boolean.class.getName().equals(klass.getName()))
			return "boolean";
		return klass.getSimpleName();
	}

	public boolean isGet() {
		return get;
	}

	public boolean isSet() {
		return set;
	}

	public void setGet(boolean get) {
		this.get = get;
	}

	public void setSet(boolean set) {
		this.set = set;
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
