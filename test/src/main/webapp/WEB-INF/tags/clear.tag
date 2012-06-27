<%@ attribute name="classname" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<jsp:useBean id="map" class="java.util.HashMap" />
<c:set target="${map}" property="classname" value="${classname}" />

<%
    String classname = map.get ("classname").toString ();

    application.removeAttribute (classname);
%>
