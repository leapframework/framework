<%@page import="leap.htpl.HtplContext"%>
<%
	HtplContext context = (HtplContext)request.getAttribute(HtplContext.class.getName());

    context.setLocalVariable("layout_var", "Jsp auto include layout " + 100);
%>