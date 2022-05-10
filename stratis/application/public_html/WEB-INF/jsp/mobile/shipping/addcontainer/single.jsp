<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="addContainerForm" method="POST">
    <div class="flex-form-container">
        <div class="formItem canbesmall">
            <label for="barcode">Barcode</label><span class="required" title="Required Field">*</span>
            <input id="barcode" name="barcode" class="form-control form-control-sm" required="true"
                   maxlength="17" pattern="\w*" title="Barcode must be 17 alphanumeric characters or less" />
        </div>
        <div class="formItem">
            <label for="stageLoc">Stage Location</label><span id="requireLocation" class="required" title="Required Field">*</span>
            <input id="stageLoc" name="stageLoc" class="form-control form-control-sm"
                   maxlength="5" pattern="\w*" title="Stage location must be 5 alphanumeric characters or less" />
        </div>
    </div>
    <div class="flex-form-container">
        <div class="formItem">
            <label for="recommend">First Stage Loc</label>
            <input type="text" id="recommend" name="recommendedLocation" value="" readonly class="form-control-plaintext" title="Submit a barcode to see a recommended location"/>
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
<script>
    var shippingAddContainer = new ShippingAddContainer(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    shippingAddContainer.configureButtons();
</script>