<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
    <!-- Decorator: Internal -->
    <title><sitemesh:write property="title"/></title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="templatename" content="internal"/>
    <link rel="shortcut icon" href="<c:url value="/images/favicon.ico" />" type="image/x-icon"/>

    <sitemesh:write property="head"/>
</head>
<body>
<sitemesh:write property="body"/>
</body>
</html>