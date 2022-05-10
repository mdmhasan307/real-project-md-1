<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="stowDetailsForm" method="POST">
    <input type="hidden" name="stowId" id="stowId" value="${stowId}"/>
    <input type="hidden" name="stowLoss" id="stowLoss" value="${stowLoss}"/>
    <input type="hidden" name="stowRelocate" id="stowRelocate" value="${stowRelocate}"/>
    <div class="flex-form-container">
        <div class="formItem">
            <label for="location">Location</label><span class="required" title="Required Field">*</span>
            <input type="text" name="location" id="location" placeholder="location" class="form-control form-control-lg"/>
        </div>
        <div class="formItem">
            <label for="niin">Last Two Digits of NIIN</label><span class="required" title="Required Field">*</span>
            <input type="text" name="lastNiin" id="niin" placeholder="niin" class="form-control form-control-lg"/>
        </div>
        <div class="formItem">
            <label for="qty">Stow Quantity</label><span class="required" title="Required Field">*</span>
            <input type="text" name="stowQty" id="qty" placeholder="qty" class="form-control form-control-lg"/>
        </div>
    </div>
    <div class="flex-form-container">
        <div class="formItem">
            <label for="loc">Location</label>
            <input type="text" id="loc" value='<c:out value="${location}" />' readonly class="form-control-plaintext"/>
        </div>
        <div class="formItem">
            <label for="ni">NIIN</label>
            <input type="text" id="ni" value='<c:out value="${niin}" />' readonly class="form-control-plaintext"/>
        </div>
        <div class="formItem">
            <label for="si">SID</label>
            <input type="text" id="si" value='<c:out value="${sid}" />' readonly class="form-control-plaintext"/>
        </div>
        <div class="formItem">
            <label for="nom">Nomenclature</label>
            <input type="text" id="nom" value='<c:out value="${Nomenclature}" />' readonly class="form-control-plaintext"/>
        </div>
        <div class="formItem">
            <label for="qty">Qty to Stow</label>
            <input type="text" id="qty" value='<c:out value="${qtyToStow}" />' readonly class="form-control-plaintext"/>
        </div>
    </div>
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
            <button type="button" class="btn btn-primary" id="btnSubmit">Submit</button>
            <button type="button" class="btn btn-primary" id="btnLoss">Loss</button>
            <button type="button" class="btn btn-primary" id="btnBypass">Bypass</button>
            <button type="button" class="btn btn-primary" id="btnMainMenu">Main Menu</button>
            <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
        </div>
    </div>
    <div id="locationNotification" style="display:none">
        <div>The location entered (<span id="enteredLoc"></span>) does not match the stow location (<span id="stowLoc"></span>). Would you like to stow to <span id="newdLoc"></span> instead?</div>
        <p>
        </p>
        <div class="flex-button-container">
            <div class="flex-button-group frmBtns" style="justify-content:center">
                <button id="btnYes" class="btn btn-primary">Yes</button>
                <button id="btnNo" class="btn btn-primary">No</button>
            </div>
        </div>
    </div>
</form:form>
<script>
    var stowingDetails = new StowingDetails(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    stowingDetails.configureButtons();
    stowingDetails.configureActions();
</script>
