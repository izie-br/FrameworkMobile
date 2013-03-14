package com.quantium.mobile.framework.libandroidtest.server;

import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns={"/clear"})
public class ClearServlet extends ServerBeanServlet {

	private static final long serialVersionUID = 6168664635758857901L;

	@Override
	public BaseServerBean getServerBean() {
		return new Clear();
	}

}
