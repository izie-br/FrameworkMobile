package com.quantium.mobile.framework;

// criar gerador de resource com strings de erro padrao para todos
public enum ErrorCode{

	UNKNOWN_EXCEPTION,
	SD_CARD_NOT_FOUND,
	UNABLE_TO_CREATE_FILE,
	UNABLE_TO_CREATE_EXISTING_FILE,
	LOG_FILE_REMOVED_BEFORE_COMPLETE_READ,
	NETWORK_COMMUNICATION_ERROR;

	public String toString() {
		return super.toString().toLowerCase();
	}

}
