package com.quantium.mobile.framework.libandroidtest.server;

public class RouterBean extends BaseServerBean {

	public static final String CLASSNAME_PARAM = "classname";
	public static final String METHOD_PARAM = "method";

	public static final BaseServerBean SERVER_BEANS [] = {
		new Echo(), new Clear(), new Insert(), new Query()
	};

	@Override
	public String getResponse() {
		String method = getParameter(METHOD_PARAM);
		for (BaseServerBean bean : SERVER_BEANS) {
			if (bean.getClass().getSimpleName().equalsIgnoreCase(method)){
				bean.setApplication(getApplication());
				bean.setMap(getMap());
				return bean.getResponse();
			}
		}
		return String.format("Metodo '%s' nao encontrado", method);
	}


}
