<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"  %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TITLE</title>

    <c:set var="url">${pageContext.request.requestURL}</c:set>
    <base href="${fn:substring(url, 0, fn:length(url) - fn:length(pageContext.request.requestURI))}${pageContext.request.contextPath}/" />

    <link href="resources/css/bootstrap-3.0.0.min.css" rel="stylesheet" media="screen">
    <link href="resources/css/bootstrap-responsive.min.css" rel="stylesheet">
</head>
<body>

<div class="container">

    <div class="masthead">
        <ul class="nav nav-pills pull-right">
            <li class="active"><a href="#">Home</a></li>
            <li><a href="#">About</a></li>
            <li><a href="#">Contact</a></li>
            <li><a href="j_spring_cas_security_logout">Logout</a>
        </ul>
        <h3 class="muted">HEADING</h3>
    </div>

    <hr>

<h1>Error</h1>

	<p>
		<c:out value="${errorMessage}" />
	</p>

 <hr>

    <div class="footer">
        <p>&copy; Cell-Life (NPO) - 2013</p>
    </div>

</div>

</body>
</html>
