<%@ page contentType="text/plain; charset=UTF-8" %>
<%
    com.quantium.mobile.framework.libandroidtest.server.RouterBean bean =
        new com.quantium.mobile.framework.libandroidtest.server.RouterBean();
    bean.setApplication(application);
    java.util.Map reqmap = request.getParameterMap();
    bean.setMap(reqmap);
    // for (Object key : reqmap.keySet()) {
    //     System.out.print( key.toString() + "{\n    ");
    //     String []values = (String[])reqmap.get(key);
    //     if (values == null)
    //       continue;
    //     for (String val : values)
    //       System.out.println(val);
    // }
    // System.out.println("}");

    String resp = bean.getResponse();
    // System.out.println(resp);
    out.println(resp);
%>
