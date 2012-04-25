package br.com.cds.mobile.framework;

// criar gerador de resource com strings de erro padrao para todos
public enum ErrorCode{

	UNKNOWN_EXCEPTION, SD_CARD_NOT_FOUND, LOG_FILE_REMOVED_BEFORE_COMPLETE_READ;

	public String toString() {
		return super.toString().toLowerCase();
	}

}
