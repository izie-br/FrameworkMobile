package com.quantium.mobile.framework.libandroidtest.server;

import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns={"/array"})
public class ArrayServlet extends ServerBeanServlet {

	private static final long serialVersionUID = 5653223812303538298L;

	@Override
	public BaseServerBean getServerBean() {
		return new Array();
	}

}
