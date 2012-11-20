<%@ page contentType="text/plain; charset=UTF-8" %>
<%@ page import="java.io.Reader"%>
<%
	Reader r = request.getReader();
	int c = r.read();
	while (c > 0){
		out.print((char)c);
		c = r.read();
	}
%>