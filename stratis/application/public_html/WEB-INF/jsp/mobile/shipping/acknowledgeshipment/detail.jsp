<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="acknowledgeShipmentForm" method="POST">
    <div class="flex-form-container">
        <div class="formItem">
            <label for="manifestNumber">LD-CON:</label><span class="required" title="Required Field">*</span>
            <input id="manifestNumber"
                   name="manifestNumber"
                   class="form-control form-control-sm"
                   required="required"
                   maxlength="20"
                   pattern="\w*"
                   title="LD-CON must be less than 20 characters alphanumeric"/>
        </div>
        <div class="formItem">
            <label for="deliveryType">Delivery Type</label><span class="required" title="Required Field">*</span>
            <select id="deliveryType" name="deliveryType" class="form-control form-control-sm" required="required">
                <option value="" disabled selected>Select a Delivery Type</option>
                <option value="P">Pickup</option>
                <option value="D">Delivery</option>
            </select>
        </div>
        <div class="formItem">
            <label for="driverName">Driver:</label><span class="required" title="Required Field">*</span>
            <input id="driverName"
                   name="driverName"
                   class="form-control form-control-sm"
                   required="required"
                   maxlength="50"
                   pattern="[\w ]*"
                   title="Driver must be less than 50 characters alphanumeric"/>
        </div>
    </div>
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
            <button type="button" class="btn btn-primary" id="btnSubmit">Submit</button>
            <button type="button" class="btn btn-primary" id="btnMainMenu">Main Menu</button>
            <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
        </div>
    </div>
</form:form>

<div id="confirmShipment" style="display:none">
    <div>Acknowledge delivery will permanently kick this issue to History. Are you sure you want to continue? Click Confirm or Cancel.</div>
    <p>
    </p>
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns" style="justify-content:center">
            <button id="btnConfirm" class="btn btn-primary">Confirm</button>
            <button id="btnCancel" class="btn btn-primary">Cancel</button>
        </div>
    </div>
</div>
<script>
    var acknowledgeShipment = new AcknowledgeShipment(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    acknowledgeShipment.configureButtons();
</script>
