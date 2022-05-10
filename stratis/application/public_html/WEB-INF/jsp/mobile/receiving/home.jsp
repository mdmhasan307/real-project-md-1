<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="receivingHomeForm" modelAttribute="receivingHomeInput">
  <div class="h4">Scan or Enter Barcodes</div>
  <div class="flex-form-container">
    <jsp:include page="homeDataDisplay.jsp">
      <jsp:param name="homeEditable" value="true"/>
    </jsp:include>
  </div>
  <div class="flex-button-container">
    <div class="flex-button-group frmBtns">
      <button type="button" class="btn btn-primary" id="btnSubmit">Submit</button>
      <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
    </div>
  </div>
</form:form>
<script>
  var receivingHome = new ReceivingHome(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
  receivingHome.configureButtons();
</script>

<jsp:include page="/WEB-INF/jsp/global/print.jsp"/>