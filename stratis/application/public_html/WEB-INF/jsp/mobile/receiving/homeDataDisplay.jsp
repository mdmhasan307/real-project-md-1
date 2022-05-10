<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="formItem">
  <label for="docNumber">Doc Num</label>
  <span class="required requiredhome" title="Required Field">*</span>
  <form:input id="docNumber"
              path="docNumber"
              type="text"
              class="form-control form-control-md"
              title="Document Number must be 14 characters alphanumeric and cannot start with 'Y'"
              maxlength="14"
              pattern="(?!([yY]))(\w{14})"
              required="true"/>
</div>
<div class="formItem">
  <label for="nsn">NSN</label>
  <span class="required requiredhome" title="Required Field">*</span>
  <form:input id="nsn"
              path="nsn"
              type="text"
              class="form-control form-control-md"
              title="NSN must be 13 characters alphanumeric"
              maxlength="13"
              pattern="\w{13}"
              required="true"/>
</div>
<div class="formItem largeItem">
  <label for="barcode">RIC/UI/QTY/CC/UP</label>
  <span class="required requiredhome" title="Required Field">*</span>
  <form:input id="barcode"
              path="barcode"
              type="text"
              class="form-control form-control-md"
              title="Must be a valid barcode 20 to 24 characters long"
              minlength="20"
              maxlength="24"
              pattern="\w{3}\w{2}[0-9]{5}[afAF] {2}\d{7,11}"
              required="true"/>
</div>
<c:choose>
  <c:when test="${param.homeEditable}">
    <div class="formItem">
      <div style="height:24px;display:inline-block;margin-bottom:.1rem">&nbsp;</div>
      <label id="btnPartialShipment"
             class="btn btn-primary btn-checkbox">
        <input id="chkPartialShipment"
               name="chkPartialShipment"
               type="checkbox"
               class="form-check-input"
               autocomplete="off">
        Partial Shipment
      </label>
    </div>
  </c:when>
  <c:otherwise>
    <div class="formItem smallItem">
      <label for="partialShipment">Partial Ship</label>
      <form:input type="text"
                  id="chkPartialShipment"
                  path="chkPartialShipment"
                  class="form-control-plaintext"/>
    </div>
  </c:otherwise>
</c:choose>
<c:choose>
  <c:when test="!${param.editable}">
    <div class="formItem smallItem"><label for="qtyInvoiced">Qty Invoiced</label>
      <input type="text"
             id="qtyInvoiced"
             name="qtyInvoiced"
             readonly
             value="${receipt.quantityInvoiced}"
             class="form-control-plaintext"/>
    </div>
    <div class="formItem smallItem"><label for="niinUi">NIIN UI</label>
      <input type="text"
             id="niinUi"
             name="niinUi"
             readonly
             value="${receipt.ui}"
             class="form-control-plaintext"/>
    </div>
  </c:when>
</c:choose>


<script>
  $(document).ready(function () {

    if (${param.homeEditable}) {
      $(".requiredhome").show();
    } else {
      $(".requiredhome").hide();

      $("#docNumber").prop("readonly", true);
      $("#docNumber").removeClass();
      $("#docNumber").addClass("form-control-plaintext");

      $("#nsn").prop("readonly", true);
      $("#nsn").removeClass();
      $("#nsn").addClass("form-control-plaintext");

      $("#barcode").prop("readonly", true);
      $("#barcode").removeClass();
      $("#barcode").addClass("form-control-plaintext");

      $("#price").prop("readonly", true);
      $("#price").removeClass();
      $("#price").addClass("form-control-plaintext");

      $("#chkPartialShipment").prop("readonly", true);
      $("#chkPartialShipment").removeClass();
      $("#chkPartialShipment").addClass("form-control-plaintext");

      $("#qtyInvoiced").prop("readonly", true);
      $("#qtyInvoiced").removeClass();
      $("#qtyInvoiced").addClass("form-control-plaintext");

      $("#niinUi").prop("readonly", true);
      $("#niinUi").removeClass();
      $("#niinUi").addClass("form-control-plaintext");
    }
  });
</script>
