<%
    org.json.JSONObject json = new org.json.JSONObject();
    for (Object keyObj : request.getParameterMap().keySet()) {
        String key = keyObj.toString();
        json.put(key, request.getParameter(key));
    }
    out.println(json.toString());
%>
