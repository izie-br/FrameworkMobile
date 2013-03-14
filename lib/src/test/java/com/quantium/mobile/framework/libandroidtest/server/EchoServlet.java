package com.quantium.mobile.framework.libandroidtest.server;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EchoServlet extends HttpServlet {

	private static final long serialVersionUID = -5721166991478995320L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		execute(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		execute(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		execute(req, resp);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		execute(req, resp);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void execute(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		HashMap map = new HashMap(req.getParameterMap());
		map.put(RouterBean.METHOD_PARAM, new String[]{req.getMethod()} );

		Echo echoBean = new Echo();
		echoBean.setMap(map);
		echoBean.setApplication(getServletContext());

		resp.getWriter().write(echoBean.getResponse());
	}

}
