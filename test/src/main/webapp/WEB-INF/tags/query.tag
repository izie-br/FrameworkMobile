<%@ attribute name="classname" required="true" %>
<%@ attribute name="keytoarray" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<jsp:useBean id="map" class="java.util.HashMap" />
<c:set target="${map}" property="classname" value="${classname}" />
<c:set target="${map}" property="keytoarray" value="${keytoarray}" />

<%
    String classname = map.get ("classname").toString ();
    Object keytoarray = map.get ("keytoarray");

    Object obj = application.getAttribute (classname);
    if (obj == null )
        obj = new org.json.JSONArray ();
    if (keytoarray != null) {
        org.json.JSONObject json = new org.json.JSONObject ();
        json.put (keytoarray.toString (), obj);
        json.put ("status", "success");
        out.println (json.toString ());
    } else {
        out.println (obj.toString ());
    }
%>
