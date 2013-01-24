package com.quantium.mobile.framework.validation;

public class Constraint {

	// apenas para facilitar
	public static final Type PRIMARY_KEY = Type.PRIMARY_KEY;
	public static final Type UNIQUE      = Type.UNIQUE;
	public static final Type FOREIGN_KEY = Type.FOREIGN_KEY;
	public static final Type NOT_NULL    = Type.NOT_NULL;
	public static final Type DEFAULT     = Type.DEFAULT;

	public static enum Type {
		PRIMARY_KEY, UNIQUE, FOREIGN_KEY, NOT_NULL, DEFAULT
	}

	private Type type;
	private Object[] args;

	public Constraint(Type type, Object...args){
		if (type == null) {
			throw new IllegalArgumentException (
					"Constraint.Type can't be null");
		}
		this.type = type;
		this.args = args;
	}

	public Type getType() {
		return type;
	}

	public boolean isTypeOf (Type type) {
		return this.type.equals (type);
	}

	public Object[] getArgs() {
		return args;
	}

}
