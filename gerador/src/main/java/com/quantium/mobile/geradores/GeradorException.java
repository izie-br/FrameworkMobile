package com.quantium.mobile.geradores;

public class GeradorException extends Exception {

	private static final long serialVersionUID = 2522368303772466189L;

	public GeradorException(String message, Throwable cause) {
		super(message, cause);
	}

	public GeradorException(String message) {
		super(message);
	}

	public GeradorException(Throwable cause) {
		super(cause);
	}

}
