<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/plain; charset=UTF-8" %>

<% pageContext.setAttribute("applicationObj", application); %>

<c:choose>

  <c:when test="${param.method == 'query'}">
    <jsp:useBean id="query"
        class="com.quantium.mobile.framework.test.server.QueryServerBean"
        scope="page">
      <c:set target="${query}" property="application" value="${applicationObj}" />
      <c:set target="${query}" property="classname" value="${param.classname}" />
    </jsp:useBean>
    <c:out value="${query.response}" escapeXml="false" />
  </c:when>

  <c:when test="${param.method == 'insert'}">
    <jsp:useBean id="insert"
        class="com.quantium.mobile.framework.test.server.Insert"
        scope="page">
      <c:set target="${insert}" property="application" value="${applicationObj}" />
      <c:set target="${insert}" property="data" value="${param.json}" />
      <c:set target="${insert}" property="classname" value="${param.classname}" />
    </jsp:useBean>
    <c:out value="${insert.response}" escapeXml="false" />
  </c:when>

  <c:when test="${param.method == 'clear'}">
    <%
      String classname = request.getParameter("classname");
      application.removeAttribute (classname);
    %>
  </c:when>

  <c:when test="${param.method == 'echo'}">
    <%
      org.json.JSONObject json = new org.json.JSONObject();
      for (Object keyObj : request.getParameterMap().keySet()) {
          String key = keyObj.toString();
          json.put(key, request.getParameter(key));
      }
      out.println(json.toString());
    %>
  </c:when>

  <c:otherwise>
    <c:out value="&quot;${request.method}&quot; method nao encontrado" />
  </c:otherwise>

</c:choose>

