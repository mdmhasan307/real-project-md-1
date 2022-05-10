<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
  <title>Mobile App</title>
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="templatename" content="internal"/>
  <style>
      .ui-dialog-titlebar-close {
          width: 45px !important;
          height: 45px !important;
          top: 24% !important;
      }

      .ui-dialog .ui-dialog-titlebar, .ui-dialog .ui-dialog-buttonpane {
          font-size: 1.25em;
      }

  </style>
  <jsp:include page="/WEB-INF/jsp/global/head.jsp"/>
  <script id="notifications-template" type="text/x-handlebars-template">
    {{#each messages}}
    <div class="row">
      <div class="col">
        {{#ifeq this.type "EVENT_MESSAGE"}}
        <i class="fab fa-algolia" style="color:blue"></i>
        {{this.message}}
        {{/ifeq}}
        {{#ifeq this.type "NOTIFICATION"}}
        <i class="far fa-check-square" style="color:darkgreen"></i>
        {{this.message}}
        {{/ifeq}}
        {{#ifeq this.type "VALIDATION_WARNING"}}
        <i class="far fa-check-square" style="color:darkred"></i>
        {{this.message}}
        {{/ifeq}}
        {{#ifeq this.type "EXCEPTION"}}
        <i class="fas fa-exclamation-triangle" style="color:darkred"></i>
        An Exception occurred. If the error continues please use the pages Exit button to go back to the home
        screen and start over.
        {{#if ../devProfileActive}}
        <div class="scrollable">{{this.message}}</div>
        {{/if}}
        {{/ifeq}}
      </div>
    </div>
    {{/each}}
  </script>
</head>
<body>
<div id="bodyWrapper">
  <div id="securityBannerTop" class="securitybanner header green">UNCLASSIFIED / FOUO</div>
  <div class="page-wrapper chiller-theme">
    <!-- Page Content  -->
    <main class="page-content">
      <div id="header">
        <div id="header-container">
          <div style="text-align:center">
            <div style="display:inline;">STRATIS Mobile View:</div>
            <div id="pageTitle" style="display:inline;"></div>
          </div>
        </div>
      </div>
      <div class="container-fluid mobile_visible" id="mobileAppContent">
      </div>
      <div class="mobile_unused text-center">
        <%-- Tablet Keyboard is visible here, so don't use this section. --%>
        *** RESERVED SPACE (tablet keyboard) ***
      </div>
      <div id="header-in-footer">
        <div id="header-in-footer-container">
          <div id="footerPageTitle" class="float-left">STRATIS Mobile View: <span id="pageTitleFooter"></span></div>
          <div style="text-align:center">
            <div class="float-sm-right">
                        <span id="notificationWrapper">
                            <i class=" fa fa-envelope" id="notificationsIcon"></i>
                        </span>
              <c:if test="${multiTenancyEnabled}">
                Site: ${siteName}
              </c:if>
              <span style="margin:0 5px">|</span>
              Role: <span id="roleName"><c:out value="${userbean.usertypestring}"/></span>
              <span style="margin:0 5px">|</span>
              Workstation: <span id="workstationName"><c:out value="${userbean.workstationName}"/></span>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
  <div id="securityBannerBottom" class="securitybanner footer green">UNCLASSIFIED / FOUO</div>
</div>

<div id="notificationsDetailsPopup">

</div>

<div class="loading-overlay"></div>
<div class="loading-overlay-image-container">
  <img src='<c:url value="/resources/images/loading.gif"/>' class="loading-overlay-img"/>
</div>


<div id="fullScreenSelection" style="display:none">
  <div>This site is best viewed in Full Screen. Would you like to switch to full screen mode now?</div>
  <p>
  </p>
  <div class="flex-button-container">
    <div class="flex-button-group frmBtns" style="justify-content:center">
      <button id="btnFullScreen" class="btn btn-primary">Yes</button>
      <button id="btnFullScreenClose" class="btn btn-primary">No</button>
    </div>
  </div>
</div>

</body>
</html>

<script>
  var smvApp;
  $(document).ready(function () {
    $.ajaxSetup({cache: false});

    Handlebars.registerHelper('ifeq', function (a, b, options) {
      if (a === b) {
        return options.fn(this);
      }
      return options.inverse(this);
    });

    smvApp = new MobileApp("${pageContext.request.contextPath}");
    smvApp.processGet("mobile/home");
    smvApp.goFullScreen();

    $("#notificationWrapper").on("click", function () {
      smvApp.openNotifications();
    });
  });

  cssVars({
    // Treat all browsers as legacy
    onlyLegacy: false,
  });

  if (${multiTenancyEnabled}) {
    $("#header-in-footer").addClass("increasedFooterHeight");
    $("#header-in-footer-container").addClass("increasedFooterHeight");
  }
</script>
