package com.quantium.mobile.framework.libandroidtest.server;

import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = {"/insert"})
public class InsertServlet extends ServerBeanServlet {

    private static final long serialVersionUID = 7341689353463181759L;

    @Override
    public BaseServerBean getServerBean() {
        return new Insert();
    }

}
