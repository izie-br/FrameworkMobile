<%@ attribute name="classname" required="true" %>
<%@ attribute name="dataparameter" required="true" %>
<%@ attribute name="keytoarray" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<jsp:useBean id="map" class="java.util.HashMap" />
<c:set target="${map}" property="classname" value="${classname}" />
<c:set target="${map}" property="dataparameter" value="${dataparameter}" />
<c:set target="${map}" property="keytoarray" value="${keytoarray}" />
<%
    String classname = map.get ("classname").toString ();
    String dataparameter = map.get ("dataparameter").toString ();
    String keytoarray = (String) map.get ("keytoarray");

    @SuppressWarnings ("unchecked")
    org.json.JSONArray savedObjects =
        (org.json.JSONArray) application.getAttribute (classname);
    if (savedObjects == null ) {
        savedObjects = new org.json.JSONArray ();
        application.setAttribute (classname, savedObjects);
    }

    String dataStr = request.getParameter (dataparameter);
    org.json.JSONArray jsonArray =
        (keytoarray != null) ?
             (org.json.JSONArray) (
                 new org.json.JSONObject (dataStr)
                 .get (keytoarray)
             ):
             new org.json.JSONArray (dataStr);

    for (int i = 0; i < jsonArray.length (); i++) {
        savedObjects.put (jsonArray.get (i));
    }
    if (keytoarray != null) {
        org.json.JSONObject json = new org.json.JSONObject ();
        json.put (keytoarray, jsonArray);
        out.println (json.toString ());
    } else {
        out.println (jsonArray.toString ());
    }
%>
