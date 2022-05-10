<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="/WEB-INF/jsp/global/head.jsp"/>
<html>
<head>
  <title>No Sites Configured</title>
</head>
<body>
<div id="bodyWrapper">
  <div id="securityBannerTop" class="securitybanner header green">UNCLASSIFIED / FOUO</div>
  <div class="page-wrapper chiller-theme">
    <main class="page-content" style="margin:20px 0 0 0;">
      <img src="${pageContext.request.contextPath}/resources/images/banner/TopBar_Left_long.jpg"/>
      <div class="flex-form-container">
        <hr>
        Currently there are no STRATIS Sites configured. Please contact a MLS2 Portal Administrator.
        <hr>
      </div>
    </main>
  </div>
  <div id="securityBannerBottom" class="securitybanner footer green">UNCLASSIFIED / FOUO</div>
</div>


</body>
</html>
