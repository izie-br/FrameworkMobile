<%@ page contentType="text/plain; charset=UTF-8" %>
<%
    com.quantium.mobile.framework.test.server.RouterBean bean =
        new com.quantium.mobile.framework.test.server.RouterBean();
    bean.setApplication(application);
    bean.setMap(request.getParameterMap());
    out.println(bean.getResponse());
%>
