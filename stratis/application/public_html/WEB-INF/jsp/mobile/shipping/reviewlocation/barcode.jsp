<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="reviewLocationForm" method="POST">
    <input type="hidden" id="overrideCommand" name="overrideCommand">
    <input type="hidden" id="aac" name="aac">
    <input type="hidden" id="scn" name="scn">
    <input type="hidden" id="shippingManifestId" name="shippingManifestId">
    <input type="hidden" id="shippingId" name="shippingId">
    <input type="hidden" id="floorLocation" name="floorLocation">
    <div class="flex-form-container">
        <div class="formItem">
            <label for="location">Location To Review</label><span class="required" title="Required Field">*</span>
            <input id="location" name="location" class="form-control form-control-sm" required="true"
                   maxlength="5" pattern="\w*" title="Location to review must be 5 alphanumeric characters or less"/>
        </div>
        <div class="formItem"><label for="barcode">Barcode</label><span class="required" title="Required Field">*</span>
            <input id="barcode" name="barcode" class="form-control form-control-sm" required="true"
                   maxlength="17" pattern="\w*" title="Barcode must be 17 alphanumeric characters or less"/>
        </div>
    </div>
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
            <button type="button" class="btn btn-primary" id="btnSubmit">Submit</button>

            <button type="button" class="btn btn-primary" id="btnOverrideAAC">Override AAC</button>
            <button type="button" class="btn btn-primary" id="btnDontOverrideAAC">Don't Override AAC</button>
            <button type="button" class="btn btn-primary" id="btnOverrideLoc">Override Location</button>
            <button type="button" class="btn btn-primary" id="btnDontOverrideLoc">Don't Override Location</button>

            <button type="button" class="btn btn-primary" id="btnMainMenu">Main Menu</button>
            <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
        </div>
    </div>
</form:form>

<script>
    var reviewLocationBarcode = new ReviewLocationBarcode(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    reviewLocationBarcode.configureButtons();
</script>