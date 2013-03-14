package com.quantium.mobile.framework.libandroidtest.server;

import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns={"/query"})
public class QueryServlet extends ServerBeanServlet {

	private static final long serialVersionUID = -2539850064913623978L;

	@Override
	public BaseServerBean getServerBean() {
		return new Query();
	}

}
