<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
  <c:when test="${param.serialsEditable}">
    <div class="formItem">
      <label for="serial">Serial</label>
      <form:input type="text" path="serial" placeholder="serial" maxlength="30" class="form-control form-control-sm"/>
      <div class="flex-button-container">
        <div class="flex-button-group singleButton">
          <button type="button" class="btn btn-primary" id="btnAdd">Add Serial</button>
        </div>
      </div>
    </div>
  </c:when>
</c:choose>
<div class="formItem">
  <label for="serials">Serial List</label>
  <select id="serials" name="serials" size="10" multiple="multiple" style="display:none"
          class="input_and_button_height w-100"> </select>
  <div>
    <ul class="list-group custom" id="serialListGroup">
    </ul>
  </div>
</div>
</div>
<div class="flex-form-container">

  <jsp:include page="detailDataDisplay.jsp">
    <jsp:param name="detailEditable" value="false"/>
      <jsp:param name="domRequired" value="false"/>
  </jsp:include>
