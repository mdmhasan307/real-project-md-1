<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="palletRelocationForm" method="POST">
    <div class="flex-form-container">
        <div class="formItem">
            <label for="oldLocation">Old Location:</label><span class="required" title="Required Field">*</span>
            <input type="text" id="oldLocation" name="oldLocation" class="form-control form-control-sm" placeholder="Old Location" required="true"
                   maxlength="5" pattern="\w*" title="Old location must be 5 alphanumeric characters or less"/>
            <div class="flex-button-container">
                <div class="flex-button-group singleButton">
                    <button type="button" class="btn btn-primary" id="btnSearch">Search</button>
                </div>
            </div>
        </div>
        <div class="formItem">
            <label for="newLocation">New Location:</label><span class="required" title="Required Field">*</span>
            <input type="text" id="newLocation" name="newLocation" class="form-control form-control-sm" placeholder="New Location" required="true"
                   maxlength="5" pattern="\w*" title="New location must be 5 alphanumeric characters or less"/>
        </div>
    </div>
    <div class="flex-form-container">
        <div class="formItem">
            <label for="aac">AAC</label>
            <input type="text" id="aac" readonly class="form-control-plaintext"/>
        </div>
        <div class="formItem">
            <label for="floorLocation">Floor Location</label>
            <input type="text" id="floorLocation" readonly class="form-control-plaintext"/>
        </div>
        <div class="formItem">
            <label for="leadTcn">LEAD TCN</label>
            <input type="text" id="leadTcn" readonly class="form-control-plaintext"/>
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
    var palletRelocation = new PalletRelocation(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    palletRelocation.configureButtons();
</script>