<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<form:form id="reviewLocationForm" method="POST">
  <input type="hidden" id="floorLocationId" name="floorLocationId">
  <div class="flex-form-container">
    <div class="formItem">
      <label for="location">Location to Review</label><span class="required" title="Required Field">*</span>
      <input id="location" name="location" class="form-control form-control-sm" required="true"
             maxlength="5" pattern="\w*" title="Location to review must be 5 alphanumeric characters or less"/>
    </div>
  </div>
  <c:if test="${containers != null && containers.size() > 0}">
    <div class="flex-form-container">
      <div class="scrollable_tall">
        <div class="table-responsive table-wrapper">
          <table class="table table-sm table-bordered" id="barcodesTable">
            <tr>
              <th>List of barcodes found at ${floor}</th>
            </tr>
            <c:forEach items="${containers}" var="container">
              <tr>
                <td>${container}</td>
              </tr>
            </c:forEach>
          </table>
        </div>
      </div>
    </div>
  </c:if>
  <div class="flex-button-container">
    <div class="flex-button-group frmBtns">
      <button type="button" class="btn btn-primary" id="btnSubmit">Submit</button>
      <button type="button" class="btn btn-primary" id="btnMainMenu">Main Menu</button>
      <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
    </div>
  </div>
</form:form>
<script>
  var reviewLocationContainer = new ReviewLocationContainer(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
  reviewLocationContainer.configureButtons();
</script>