package com.quantium.mobile.framework;

import com.quantium.mobile.framework.logging.LogPadrao;
import com.quantium.mobile.framework.utils.AndroidUtils;

import android.content.Context;

public class FrameworkException extends RuntimeException{

	private static final long serialVersionUID = -3674527184457040180L;

	private static final String ERRO_SEM_MENSAGEM_DETALHADA_FORMAT =
			"Erro: %s\nSem mensagem detalhada.";

	private ErrorCode code;

	public FrameworkException(ErrorCode code,Throwable cause) {
		super(cause);
		this.code = code;
	}

	public FrameworkException(ErrorCode code) {
		super();
		this.code = code;
	}

	public ErrorCode getCode(){
		return code;
	}

	@Override
	public String getMessage() {
		Context context = BaseApplication.getContext();
		int id = AndroidUtils.getResourceByName(
				context,
				"string/"+code.toString()
		);
		if(id==0){
			String message = String.format(
					ERRO_SEM_MENSAGEM_DETALHADA_FORMAT,
					code.toString()
			);
			LogPadrao.e(message);
			return message;
		}
		return context.getString(id);
	}

}
