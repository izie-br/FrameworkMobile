package com.quantium.mobile.framework.libandroidtest.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class ServerBeanServlet extends HttpServlet {

    private static final long serialVersionUID = 4590855990098864098L;

    public abstract BaseServerBean getServerBean();

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

    public void execute(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        BaseServerBean bean = getServerBean();
        bean.setMap(req.getParameterMap());
        bean.setApplication(getServletContext());

        resp.setContentType("application/json");
        resp.getWriter().write(bean.getResponse());
    }

}
