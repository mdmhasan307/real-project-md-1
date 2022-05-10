<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<form:form id="stowDetailLocationForm" modelAttribute="receivingStowDetailLocationInput">
  <div class="flex-form-container">
    <div class="formItem">
      <label for="location">Location Choice</label>
      <form:select id="location"
                   path="location"
                   class="form-control form-control-lg"
                   required="true">
        <option value="" selected>Select a Location</option>
        <form:options items="${locations}"
                      itemLabel="locationOption"
                      itemValue="LocationId"/>
      </form:select>
    </div>
    <div class="formItem smallItem">
      <label for="wac">WAC</label>
      <form:select id="wac"
                   path="wac"
                   class="form-control form-control-lg">
        <option value="" selected>None</option>
        <form:options items="${wacs}"
                      itemLabel="wacNumber"
                      itemValue="wacNumber"/>
      </form:select>
    </div>
    <c:choose>
      <c:when test="${serialControlFlag == 'Y'}">
        <jsp:include page="controlledNiinDetailsDataDisplay.jsp">
          <jsp:param name="serialsEditable" value="false"/>
        </jsp:include>
      </c:when>
      <c:otherwise>
        <jsp:include page="detailDataDisplay.jsp">
          <jsp:param name="detailEditable" value="false"/>
          <jsp:param name="domRequired" value="false"/>
        </jsp:include>
      </c:otherwise>
    </c:choose>
  </div>
  <div class="flex-button-container">
    <div class="flex-button-group frmBtns">
      <button type="button" class="btn btn-primary" id="btnSubmit">Submit</button>
      <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
    </div>
  </div>
  <script>
    var stowDetailLocation = new ReceivingDetailLocation(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    stowDetailLocation.configureButtons();
    var serialList = [];
    <c:forEach var="serial" items="${receivingStowDetailLocationInput.serials}">
    serialList.push("${serial}");
    </c:forEach>
    stowDetailLocation.configureSerials(serialList)
  </script>
</form:form>
