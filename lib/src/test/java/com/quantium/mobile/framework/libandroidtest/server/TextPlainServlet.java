package com.quantium.mobile.framework.libandroidtest.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class TextPlainServlet extends GenericServlet {

	private static final long serialVersionUID = 7068861441208302980L;

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		Reader r = arg0.getReader();
		PrintWriter out = arg1.getWriter();
		int c = r.read();
		while (c > 0){
			out.print((char)c);
			c = r.read();
		}

	}

	
}
