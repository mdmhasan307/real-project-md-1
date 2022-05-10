<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/global/head.jsp"/>
<html>
<head>
    <title>No User</title>
</head>
<body>
<div id="bodyWrapper">
    <div id="securityBannerTop" class="securitybanner header green">UNCLASSIFIED / FOUO</div>
    <div class="page-wrapper chiller-theme">
        <main class="page-content" style="margin:20px 0 0 0;">
            <img src="${pageContext.request.contextPath}/resources/images/banner/TopBar_Left_long.jpg"/>
            <div class="flex-form-container">
                <div><h3>Current STRATIS Sites Available</h3></div>
                <div class="table-responsive table-wrapper">
                    <table class="table table-sm table-bordered">
                        <tr>
                            <th>Site Name</th>
                            <th>Description</th>
                            <th>Status</th>
                        </tr>
                        <c:forEach items="${sitesAvailable}" var="site">
                            <tr>
                                <td>${site.siteName}</td>
                                <td>${site.description}</td>
                                <td>${site.status}</td>
                            </tr>
                        </c:forEach>
                    </table>
                </div>

            </div>
            <hr>
            <div class="flex-form-container">
                You do not have an account in any of the current STRATIS sites available.
            </div>
        </main>
    </div>
    <div id="securityBannerBottom" class="securitybanner footer green">UNCLASSIFIED / FOUO</div>
</div>
</body>
</html>
