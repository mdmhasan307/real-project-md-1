<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<div class="formItem canbesmall">
  <label for="qtyReceived">Qty Received</label>
  <span class="required requiredDetail" title="Required Field">*</span>
  <form:input id="qtyReceived"
              path="qtyReceived"
              type="text"
              class="form-control form-control-sm"
              title="Qty Received must be 1 to 5 characters numeric"
              maxlength="5"
              pattern="\d{1,5}"
              required="true"/>
</div>
<div class="formItem canbesmall">
  <label for="qtyInducted">Qty Inducted</label>
  <span class="required requiredDetail" title="Required Field">*</span>
  <form:input id="qtyInducted"
              type="text"
              path="qtyInducted"
              class="form-control form-control-sm"
              title="Qty Inducted must be 1 to 5 characters numeric"
              maxlength="5"
              pattern="\d{1,5}"
              required="true"/>
</div>
<div class="formItem smallItem">
  <label for="ri">RI</label>
  <span class="required requiredDetail" title="Required Field">*</span>
  <form:input id="ri"
              type="text"
              path="ri"
              class="form-control form-control-sm"
              title="RI must be 3 characters alphanumeric"
              maxlength="3"
              pattern="\w{3}"
              required="true"/>
</div>
<div class="formItem smallItem">
  <label for="cc">CC</label>
  <c:choose>
    <c:when test="${param.detailEditable}">
      <form:select id="cc"
                   path="cc"
                   class="form-control form-control-sm">
        <form:option value="A">A</form:option>
        <form:option value="F">F</form:option>
      </form:select>
    </c:when>
    <c:otherwise>
      <form:input type="text"
                  id="cc"
                  path="cc"
                  class="form-control-plaintext"/>
    </c:otherwise>
  </c:choose>
</div>
<div id="expDateContainer" class="formItem">
  <label for="expDate">EXP Date (yyyymmdd)</label>
  <form:input id="expDate"
              type="text"
              path="expDate"
              class="form-control form-control-sm"
              title="EXP Date must use date format yyyymmdd"
              maxlength="8"
              pattern="\d{8}"/>
</div>
<div class="formItem smallItem">
  <label for="bulk">Bulk Location</label>
  <c:choose>
    <c:when test="${param.detailEditable}">
      <form:select id="bulk"
                   path="bulk"
                   class="form-control form-control-sm">
        <option value="N">No</option>
        <option value="Y">Yes</option>
      </form:select>
    </c:when>
    <c:otherwise>
      <form:input type="text"
                  id="bulk"
                  path="bulk"
                  class="form-control-plaintext"/>
    </c:otherwise>
  </c:choose>
</div>
<div class="formItem">
  <label for="dom">DOM (yyyymmdd)</label>
  <c:if test="${param.domRequired}">
    <span class="required requiredDetail" title="Required Field">*</span>
  </c:if>
  <form:input type="text"
              id="dom"
              path="dom"
              class="form-control form-control-sm"
              title="DOM must use date format yyyymmdd"
              maxlength="8"
              pattern="\d{8}"/>
</div>


<script>
  $(document).ready(function () {

    if (${param.detailEditable}) {
      $(".requiredDetail").show();
      $("#expDateContainer").hide();
      if (${param.domRequired}) {
        $("#dom").prop("required", true);
      }
    } else {
      $(".requiredDetail").hide();
      $("#expDateContainer").show();
      $(".canbesmall").addClass("smallItem");

      $("#qtyReceived").prop("readonly", true);
      $("#qtyReceived").removeClass();
      $("#qtyReceived").addClass("form-control-plaintext");

      $("#qtyInducted").prop("readonly", true);
      $("#qtyInducted").removeClass();
      $("#qtyInducted").addClass("form-control-plaintext");

      $("#ri").prop("readonly", true);
      $("#ri").removeClass();
      $("#ri").addClass("form-control-plaintext");

      $("#cc").prop("readonly", true);
      $("#cc").removeClass();
      $("#cc").addClass("form-control-plaintext");

      $("#expDate").prop("readonly", true);
      $("#expDate").removeClass();
      $("#expDate").addClass("form-control-plaintext");

      $("#bulk").prop("readonly", true);
      $("#bulk").removeClass();
      $("#bulk").addClass("form-control-plaintext");

      $("#dom").prop("readonly", true);
      $("#dom").removeClass();
      $("#dom").addClass("form-control-plaintext");
    }
  })

</script>

<jsp:include page="homeDataDisplay.jsp">
  <jsp:param name="homeEditable" value="false"/>
</jsp:include>
