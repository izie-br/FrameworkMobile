package br.com.cds.mobile.framework;

public class FrameworkException extends Exception{

	private static final long serialVersionUID = -3674527184457040180L;

	public static final int UNKNOWN_EXCEPTION = 0;
	public static final int SD_CARD_NOT_FOUND = 1;

	private int code;

	public FrameworkException(int code) {
		super();
		this.code = code;
	}

	public int getCode(){
		return code;
	}
}
