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
					"Constraint Type can't be null");
		}
		if (args == null) {
			args = new Object [0];
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode ());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass () != obj.getClass ())
			return false;
		Constraint other = (Constraint) obj;
		if (type != other.type)
			return false;
		if (args.length > 0 || other.args.length > 0){
			if (args.length != other.args.length)
				return false;
			for (int i=0; i< args.length ; i++) {
				if ( !args[i].equals (other.args[i]))
					return false;
			}
		}
		return true;
	}

	

}
