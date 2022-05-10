<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="locationSurveyDetailsForm" modelAttribute="niinLocationInput" method="POST">
    <div>
        <jsp:include page="detailsInput.jsp">
            <jsp:param name="editable" value="${niinLocationInput.niinLocationId==null}"/>
        </jsp:include>
    </div>
    <!-- Scan Barcode -->
    <!-- Enter NIIN -->
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
          <button type="button" class="btn btn-primary" id="btnSubmit">Submit</button>
          <button type="button" class="btn btn-primary" id="btnRemove">Remove</button>
          <button type="button" class="btn btn-primary" id="btnBypass">Bypass</button>
          <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
        </div>
    </div>
</form:form>
<script>
    var locationSurveyDetail = new LocationSurveyDetail(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    locationSurveyDetail.configureButtons();

    if (${niinLocationInput.niinLocationId==null}) $("#btnRemove").hide();
</script>
