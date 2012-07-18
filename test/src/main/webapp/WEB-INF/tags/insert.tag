<%@ attribute name="classname" required="true" %>
<%@ attribute name="dataparameter" required="true" %>
<%@ attribute name="keystoarray" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<jsp:useBean id="map" class="java.util.HashMap" />
<c:set target="${map}" property="classname" value="${classname}" />
<c:set target="${map}" property="dataparameter" value="${dataparameter}" />
<c:set target="${map}" property="keystoarray" value="${keystoarray}" />
<%
    String classname = map.get ("classname").toString ();
    String dataparameter = map.get ("dataparameter").toString ();
    String keystoarray = (String) map.get ("keystoarray");

    @SuppressWarnings ("unchecked")
    org.json.JSONArray savedObjects =
        (org.json.JSONArray) application.getAttribute (classname);
    if (savedObjects == null ) {
        savedObjects = new org.json.JSONArray ();
        application.setAttribute (classname, savedObjects);
    }

    String dataStr = request.getParameter (dataparameter);
    org.json.JSONArray jsonArray = null;

    if (keystoarray == null)
             jsonArray = new org.json.JSONArray (dataStr);
    else if (keystoarray instanceof String ) {
        org.json.JSONObject jsonObject = new org.json.JSONObject(dataStr);
        for (String key : keystoarray.split("\\.") ) {
            Object obj = jsonObject.get (key);
            if (obj instanceof org.json.JSONArray ) {
                jsonArray = (org.json.JSONArray)obj;
                break;
            }
            jsonObject = (org.json.JSONObject)obj;
        }
    }
    for (int i = 0; i < jsonArray.length (); i++) {
        savedObjects.put (jsonArray.get (i));
    }
    if (keystoarray != null) {
        org.json.JSONObject json = new org.json.JSONObject ();
        json.put (keystoarray, jsonArray);
        out.println (json.toString ());
    } else {
        out.println (jsonArray.toString ());
    }
%>
