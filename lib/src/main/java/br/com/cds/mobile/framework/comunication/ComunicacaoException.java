package br.com.cds.mobile.framework.comunication;

public class ComunicacaoException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3914090827538113074L;
	public static final String ERRO_CONEXAO = "Serviço temporariamente indisponível. Tente novamente.";

	public ComunicacaoException() {
		super();
	}

	public ComunicacaoException(String detailMessage) {
		super(detailMessage);
	}

	public ComunicacaoException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ComunicacaoException(Throwable e) {
		super(e);
	}

	public static boolean isComunicacaoException(Throwable t) {
		String message = "";
		String causeMessage = "";
		if (t.getMessage() != null) {
			message = t.getMessage();
		}
		if (t.getCause() != null) {
			if (t.getCause().getMessage() != null) {
				causeMessage = t.getCause().getMessage();
			}
		}

		if (message.contains("ComunicacaoException") || causeMessage.contains("ComunicacaoException")) {
			return true;
		}
		if (message.contains("HttpHostConnectException") || causeMessage.contains("HttpHostConnectException")) {
			return true;
		}
		if (message.contains("java.net.ConnectException") || causeMessage.contains("java.net.ConnectException")) {
			return true;
		}
		if (message.contains("java.net.UnknownHostException") || causeMessage.contains("java.net.UnknownHostException")) {
			return true;
		}
		if (message.contains("org.apache.http.conn.ConnectTimeoutException")
				|| causeMessage.contains("org.apache.http.conn.ConnectTimeoutException")) {
			return true;
		}
		if (message.contains("LeituraRespostaException") || causeMessage.contains("LeituraRespostaException")) {
			return true;
		}
		if (message.contains("java.net.SocketException") || causeMessage.contains("java.net.SocketException")) {
			return true;
		}
		if (message.contains("FileNotFoundException") || causeMessage.contains("FileNotFoundException")) {
			return true;
		}
		if (message.contains("NoHttpResponseException") || causeMessage.contains("NoHttpResponseException")) {
			return true;
		}
		return false;
	}
}
