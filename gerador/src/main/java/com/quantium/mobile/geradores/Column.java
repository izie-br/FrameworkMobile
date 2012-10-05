package com.quantium.mobile.geradores;

import com.quantium.mobile.framework.utils.CamelCaseUtils;

public class Column {
	private String klass;
	private String lowerAndUnderscores;

	public Column(String klass, String lowerAndUnderscores){
		this.klass = klass;
		this.lowerAndUnderscores = lowerAndUnderscores;
	}
	public String getKlass(){
		return klass;
	}
	public String getType(){
		if (klass.equals("Long")){
			return "long";
		} else if (klass.equals("Boolean")){
			return "boolean";
		} else if (klass.equals("Double")){
			return "double";
		} else {
			return klass;
		}
	}
	public String getLowerCamel(){
		return CamelCaseUtils.toLowerCamelCase(lowerAndUnderscores);
	}

	public String getLowerAndUnderscores(){
		return lowerAndUnderscores;
	}

	public String getUpperAndUnderscores(){
		return CamelCaseUtils.camelToUpper(getLowerCamel());
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((klass == null) ? 0 : klass.hashCode());
		result = prime
				* result
				+ ((lowerAndUnderscores == null) ? 0 : lowerAndUnderscores
						.hashCode());
		return result;
	}

	public String getUpperCamel(){
		return CamelCaseUtils.toUpperCamelCase(lowerAndUnderscores);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Column other = (Column) obj;
		if (klass == null) {
			if (other.klass != null)
				return false;
		} else if (!klass.equals(other.klass))
			return false;
		if (lowerAndUnderscores == null) {
			if (other.lowerAndUnderscores != null)
				return false;
		} else if (!lowerAndUnderscores.equals(other.lowerAndUnderscores))
			return false;
		return true;
	}

}
