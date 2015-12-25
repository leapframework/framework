<%@taglib prefix="htpl" uri="/WEB-INF/htpl.tld"%>
<%
	request.setAttribute("message", "Hello world!");
%>
<htpl:include file="include.html"/>