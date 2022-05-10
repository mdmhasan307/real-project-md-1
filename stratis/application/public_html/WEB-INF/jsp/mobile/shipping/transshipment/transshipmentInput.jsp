<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${param.contract}">
  <div class="flex-form-container">
    <div class="formItem canbesmall">
      <label for="contractNumber">Contract Number</label>
      <span class="required"
            title="Required Field">*</span>
      <form:input id="contractNumber"
                  path="contractNumber"
                  class="form-control form-control-sm"
                  title="Contract Number must be less than or equal to 13 characters alphanumeric"
                  maxlength="13"
                  pattern="\w{1,13}"
                  required="true"/>
    </div>
    <div class="formItem canbesmall">
      <label for="quantityInvoiced">Quantity Invoiced</label>
      <span class="required"
            title="Required Field">*</span>
      <form:input id="quantityInvoiced"
                  path="quantityInvoiced"
                  class="form-control form-control-sm"
                  title="Quantity Invoiced must be a positive integer value."
                  maxlength="5"
                  pattern="\d{1,5}"
                  required="true"/>
    </div>
    <div class="formItem canbesmall">
      <label for="quantityReceived">Quantity Received</label>
      <span class="required"
            title="Required Field">*</span>
      <form:input id="quantityReceived"
                  path="quantityReceived"
                  class="form-control form-control-sm"
                  title="Quantity Received must be a positive integer value."
                  maxlength="5"
                  pattern="\d{1,5}"
                  required="true"/>
    </div>
    <div class="formItem canbesmall">
      <label for="shipmentNumber">Shipment Number</label>
      <span class="required"
            title="Required Field">*</span>
      <form:input id="shipmentNumber"
                  path="shipmentNumber"
                  class="form-control form-control-sm"
                  title="Shipment Number must be  7 characters alphanumeric"
                  maxlength="7"
                  pattern="\w{7}"
                  required="true"/>
    </div>
  </div>
</c:if>
<div class="flex-form-container">
  <c:if test="${param.tcn}">
    <div class="formItem canbesmall">
      <label for="tcn">TCN</label>
      <span class="required"
            title="Required Field">*</span>
      <form:input id="tcn"
                  path="tcn"
                  class="form-control form-control-sm"
                  title="TCN must be 17 characters alphanumeric and cannot start with 'Y'"
                  maxlength="17"
                  pattern="(?!([yY]))(\w{17})"
                  required="true"/>
    </div>
  </c:if>
  <div class="formItem canbesmall">
    <label for="nsn">NSN</label>
    <span class="required"
          title="Required Field">*</span>
    <form:input id="nsn"
                path="nsn"
                class="form-control form-control-sm"
                title="NSN must be 13 characters alphanumeric"
                maxlength="13"
                pattern="\w{13}"
                required="true"/>
  </div>
  <div class="formItem canbesmall">
    <label for="smic">SMIC</label>
    <span class="required"
          title="Required Field">*</span>
    <form:input id="smic"
                path="smic"
                class="form-control form-control-sm"
                title="SMIC must be 2 characters alphanumeric"
                maxlength="2"
                pattern="\w{2}"
                required="true"/>
  </div>
  <div class="formItem canbesmall">
    <label for="barcode">Barcode</label>
    <span class="required"
          title="Required Field">*</span>
    <form:input id="barcode"
                path="barcode"
                class="form-control form-control-sm"
                title="Barcode must be between 5 to 10 characters alphanumeric"
                maxlength="10"
                pattern="\w{5,10}"
                required="true"/>
  </div>
  <div class="formItem canbesmall">
    <label for="trackingNumber">Tracking Number</label>
    <span class="required"
          title="Required Field">*</span>
    <form:input id="trackingNumber"
                path="trackingNumber"
                class="form-control form-control-sm"
                title="Tracking Number must be between 10 to 18 characters alphanumeric"
                maxlength="18"
                pattern="\w{10,18}"
                required="true"/>
  </div>
</div>
<div class="flex-form-container">
  <c:choose>
    <c:when test="${param.contract}">
      <div class="formItem canbesmall">
        <label for="billAmount">Bill Amount</label>
        <form:input id="billAmount"
                    path="billAmount"
                    class="form-control form-control-sm"
                    title="Bill Amount must be a decimal value"
                    maxlength="5"
                    pattern="(\d*\.?\d*)"/>
      </div>
    </c:when>
    <c:otherwise>
      <div class="formItem canbesmall">
        <label for="barcodeToParse">RIC/UI/QTY/CC/UP</label>
        <form:input id="barcodeToParse"
                    path="barcodeToParse"
                    class="form-control form-control-sm"
                    title="RIC/UI/QTY/CC/UP invalid"
                    minlength="20"
                    maxlength="24"
                    pattern="\w{3}\w{2}[0-9]{5}[afAF] {2}\d{7,11}"/>

      </div>
    </c:otherwise>
  </c:choose>
  <div class="formItem canbesmall">
    <c:choose>
      <c:when test="${param.tcn}">
        <label for="aac">Consignee</label>
      </c:when>
      <c:when test="${param.document}">
        <label for="aac">Consignee</label>
      </c:when>
      <c:otherwise>
        <label for="aac">AAC</label>
        <span class="required"
              title="Required Field">*</span>
      </c:otherwise>
    </c:choose>
    <form:input id="aac"
                path="aac"
                class="form-control form-control-sm"
                title="Consignee must be 6 characters alphanumeric and cannot start with 'Y'"
                maxlength="6"
                pattern="(?!([yY]))(\w{6})"/>
  </div>
  <div class="formItem canbesmall">
    <label for="documentNumber">Document Number</label>
    <c:if test="${param.document}">
      <span class="required"
            title="Required Field">*</span>
    </c:if>
    <form:input id="documentNumber"
                path="documentNumber"
                class="form-control form-control-sm"
                title="Document Number must be 14 characters alphanumeric and cannot start with 'Y'"
                maxlength="14"
                pattern="(?!([yY]))(\w{14})"/>
  </div>
  <div class="formItem canbesmall">
    <label for="suffix">Suffix</label>
    <form:input id="suffix"
                path="suffix"
                class="form-control form-control-sm"
                title="Suffix must be a single characters alphanumeric"
                maxlength="1"
                pattern="\w"/>
  </div>
</div>
<c:if test="${param.contract}">
  <div class="flex-form-container">
    <div class="formItem smallItem">
      <label for="pc">PC</label>
      <form:input id="pc"
                  path="pc"
                  value="A"
                  class="form-control-plaintext"
                  readonly="true"/>
    </div>
    <div class="formItem smallItem">
      <label for="cc">CC</label>
      <form:select path="cc" id="cc" class="form-control form-control-lg" required="true">
        <option value="" disabled selected>Select a Condition Code</option>
        <form:options items="${conditionCodes}"
                      itemLabel="value"
                      itemValue="key"/>
      </form:select>
    </div>
    <div class="formItem canbesmall">
      <label for="callNumber">Call Number</label>
      <form:input id="callNumber"
                  path="callNumber"
                  class="form-control form-control-sm"
                  title="Call number must be 5 characters alphanumeric"
                  maxlength="5"
                  pattern="\w{5}"/>
    </div>
    <div class="formItem canbesmall">
      <label for="lineNumber">Line Number</label>
      <form:input id="lineNumber"
                  path="lineNumber"
                  class="form-control form-control-sm"
                  title="Line number must be 5 characters alphanumeric"
                  maxlength="5"
                  pattern="\w{5}"/>
    </div>
    <div class="formItem canbesmall">
      <label for="tailDate">Tail Date</label>
      <form:input id="tailDate"
                  path="tailDate"
                  type="date"
                  class="form-control form-control-sm"
                  placeholder="mm/dd/yyyy"
                  title="Tail Date must be a valid date"
                  maxlength="10"
                  pattern="\d{1,2}\/\d{1,2}\/\d{2,4}"/>
    </div>
  </div>
</c:if>

<script>
  $(document).ready(function () {
    if (${param.document}) $("#documentNumber").prop('required', true);
    if (${param.contract}) $("#aac").prop('required', true);
  });
</script>
