package br.com.cds.mobile.framework;

import android.content.Context;
import br.com.cds.mobile.framework.utils.AndroidUtils;

public class FrameworkException extends Exception{

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
			return String.format(
					ERRO_SEM_MENSAGEM_DETALHADA_FORMAT,
					code.toString()
			);
		}
		return context.getString(id);
	}

}
