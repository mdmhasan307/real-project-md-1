<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="receivingDetailForm"
           modelAttribute="receivingDetailInput">
  <div class="flex-form-container">
    <jsp:include page="detailDataDisplay.jsp">
      <jsp:param name="detailEditable" value="true"/>
      <jsp:param name="domRequired" value="${domRequired}"/>
    </jsp:include>
    <input type="hidden" name="dasfQtyOverride" id="dasfQtyOverride"/>
  </div>
  <div class="flex-button-container">
    <div class="flex-button-group frmBtns">
      <button type="button"
              class="btn btn-primary"
              id="btnSubmit">Submit
      </button>
      <button type="button"
              class="btn btn-primary"
              id="btnMainMenu">Main Menu
      </button>
      <button type="button"
              class="btn btn-primary"
              id="btnExit">Exit
      </button>
    </div>
  </div>
  <div id="dasfQtySelection" style="display:none">
    <div>The quantity being received is greater than the quantity on the DASF - Would you like to receive excess qty as a D6A</div>
    <p>
    </p>
    <div class="flex-button-container">
      <div class="flex-button-group frmBtns" style="justify-content:center">
        <button id="btnGenerate" class="btn btn-primary">Yes</button>
        <button id="btnDontGenerate" class="btn btn-primary">No</button>
      </div>
    </div>
  </div>
</form:form>
<script>
  $(document).ready(function () {
    var receivingDetail = new ReceivingDetail(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    receivingDetail.configureButtons();
  });
</script>

<jsp:include page="/WEB-INF/jsp/global/print.jsp"/>