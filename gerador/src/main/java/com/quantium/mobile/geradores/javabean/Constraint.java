package com.quantium.mobile.geradores.javabean;

public class Constraint {

	public static enum Type {
		PRIMARY_KEY, UNIQUE, FOREIGN_KEY, NOT_NULL, DEFAULT
	}

	private Type type;
	private Object[] args;

	public Constraint(Type type, Object...args){
		this.type = type;
		this.args = args;
	}

	public Type getType() {
		return type;
	}

	public Object[] getArgs() {
		return args;
	}

}
