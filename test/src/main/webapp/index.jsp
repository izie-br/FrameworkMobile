<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="org.json.JSONObject" %>

<c:set var="query" value="query" scope="page" />
<c:set var="insert" value="insert" scope="page" />
<c:set var="clear" value="clear" scope="page" />

<c:choose>
  <c:when test="${param.method == query}">
    <tags:query keytoarray="list" classname="${param.classname}"/>
  </c:when>
  <c:when test="${param.method == insert}">
    <tags:insert keytoarray="list" dataparameter="json"  classname="${param.classname}"/>
  </c:when>
  <c:when test="${param.method == clear}">
    <tags:clear classname="${param.classname}"/>
  </c:when>
  <c:otherwise>
    <% System.out.println (request.getParameter("method")); %>
  </c:otherwise>
</c:choose>

