<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/plain; charset=UTF-8" %>

<% pageContext.setAttribute("applicationObj", application); %>
<% pageContext.setAttribute("requestObj", request.getParameterMap()); %>

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
    <jsp:useBean id="clear"
        class="com.quantium.mobile.framework.test.server.Clear"
        scope="page">
      <c:set target="${clear}" property="application" value="${applicationObj}" />
      <c:set target="${clear}" property="classname" value="${param.classname}" />
    </jsp:useBean>
    <c:out value="${clear.response}" escapeXml="false" />
  </c:when>

  <c:when test="${param.method == 'echo'}">
    <jsp:useBean id="echo"
        class="com.quantium.mobile.framework.test.server.Echo"
        scope="page">
      <c:set target="${echo}" property="application" value="${applicationObj}" />
      <c:set target="${echo}" property="map" value="${requestObj}" />
    </jsp:useBean>
    <c:out value="${echo.response}" escapeXml="false" />
  </c:when>

  <c:otherwise>
    <c:out value="&quot;${request.method}&quot; metodo nao encontrado" />
  </c:otherwise>

</c:choose>

