package br.com.cds.mobile.framework.query;

public class QueryParseException extends RuntimeException {

	private static final long serialVersionUID = 5513926680817791128L;

	public QueryParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public QueryParseException(String message) {
		super(message);
	}

	public QueryParseException(Throwable cause) {
		super(cause);
	}

}
