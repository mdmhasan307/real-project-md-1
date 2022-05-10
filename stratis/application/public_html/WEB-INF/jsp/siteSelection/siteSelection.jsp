<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="/WEB-INF/jsp/global/head.jsp"/>
<html>
<head>
    <title>Site Selection</title>
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
            <form id="siteSelection" method="POST">
                <div class="flex-form-container">
                    <select name="siteSelected" id="siteSelected" class="form-control form-control-lg" required="true">
                        <option value="" disabled selected>Select a Site</option>
                        <c:forEach items="${matchingAccounts}" var="matchingAccount">
                            <option <c:if test="${!matchingAccount.allowLogin}"> disabled </c:if>
                                    value="${matchingAccount.siteName}">
                                    ${matchingAccount.description}
                                <c:if test="${!matchingAccount.allowLogin}">: Account ${matchingAccount.disableReason}</c:if>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="flex-button-container">
                    <div class="flex-button-group frmBtns">
                        <button type="submit" class="btn btn-primary">Submit</button>
                    </div>
                </div>
            </form>
        </main>
    </div>
    <div id="securityBannerBottom" class="securitybanner footer green">UNCLASSIFIED / FOUO</div>
</div>


</body>
</html>
