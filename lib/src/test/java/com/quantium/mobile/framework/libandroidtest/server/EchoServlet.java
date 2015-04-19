package com.quantium.mobile.framework.libandroidtest.server;

import com.quantium.mobile.framework.libandroidtest.communication.JsonCommunicationIT;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/echo"})
public class EchoServlet extends ServerBeanServlet {

    private static final long serialVersionUID = 383548545037711073L;

    @Override
    public BaseServerBean getServerBean() {
        return new Echo();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // Adiciona um o item ao mapa, com o nome do metodo HTTP,
        // ao mapa que sera retornado pelo bean ECHO
        HttpServletRequest wrapped = new HttpServletRequestWrapper(req) {
            @Override
            public Map getParameterMap() {
                Map map = new HashMap(super.getParameterMap());
                map.put(JsonCommunicationIT.METHOD_PARAM, super.getMethod());
                return map;
            }
        };
        super.execute(wrapped, resp);
    }

}
