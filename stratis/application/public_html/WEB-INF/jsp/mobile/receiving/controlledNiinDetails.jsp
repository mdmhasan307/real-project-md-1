<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="receivingControlledNiinDetailsForm" modelAttribute="receivingControlledNiinDetailsInput">
  <div class="flex-form-container">
    <jsp:include page="controlledNiinDetailsDataDisplay.jsp">
      <jsp:param name="serialsEditable" value="true"/>
    </jsp:include>
  </div>
</form:form>
<div class="flex-button-container">
  <div class="flex-button-group frmBtns">
    <button type="button" class="btn btn-primary" id="btnSubmit">Submit</button>
    <button type="button" class="btn btn-primary" id="btnMainMenu">Main Menu</button>
    <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
  </div>
</div>
<script>
  var controlledNiinDetails = new ReceivingControlledNiinDetails(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
  controlledNiinDetails.configureButtons();
</script>
