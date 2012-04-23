package br.com.cds.mobile.framework;

// criar gerador de resource com strings de erro padrao para todos
public enum ErrorCode{

	UNKNOWN_EXCEPTION, SD_CARD_NOT_FOUND, LOG_FILE_NOT_FOUND;

	public String toString() {
		return super.toString().toLowerCase();
	}

}
