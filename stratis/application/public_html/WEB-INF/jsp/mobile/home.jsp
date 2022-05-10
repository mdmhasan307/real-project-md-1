<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="flex-button-container">
    <div class="flex-button-group fbg-left">
        <button type="button" class="btn btn-primary" id="btnSwitch">Switch Workstations</button>
        <button type="button" class="btn btn-primary" id="btnDesktop">Desktop View</button>
        <button type="button" class="btn btn-primary" id="btnToggleFullscreen">Toggle Fullscreen</button>
    </div>
    <div class="flex-button-group fbg-right">
        <button type="button" class="btn btn-primary" id="btnReceiving" disabled>RECEIVING</button>
        <button type="button" class="btn btn-primary" id="btnStowing" disabled>STOWING</button>
        <button type="button" class="btn btn-primary" id="btnPicking" disabled>PICKING</button>
        <button type="button" class="btn btn-primary" id="btnShipping" disabled>SHIPPING</button>
        <button type="button" class="btn btn-primary" id="btnInventory" disabled>INVENTORY</button>
        <button type="button" class="btn btn-primary" id="btnTestPrint" style="display:none;">Print Test</button>
        <button type="button" class="btn fillerButton" id="btnFiller" disabled></button>
    </div>
</div>
<div class="flex-display-group">
    <c:choose>
        <c:when test="${empty wac}">
            <div class="flex-form-container">
                WAC not assigned to workstation
            </div>
        </c:when>
        <c:otherwise>
            <div class="flex-form-container">
                <div class="formItem smallItem">
                    <label for="wac">WAC</label>
                    <input type="text" id="wac" value='<c:out value="${wac}"/>' readonly class="form-control-plaintext"/>
                </div>
                <div class="formItem smallItem">
                    <label for="stowCount">Stow</label>
                    <input type="text" id="stowCount" value='<c:out value="${stowCount}"/>' readonly class="form-control-plaintext"/>
                </div>
                <div class="formItem smallItem">
                    <label for="pickCount">Pick</label>
                    <input type="text" id="pickCount" value='<c:out value="${pickCount}"/>' readonly class="form-control-plaintext"/>
                </div>
                <div class="formItem smallItem">
                    <label for="inventoryCount">Inventory</label>
                    <input type="text" id="inventoryCount" value='<c:out value="${inventoryCount}"/>' readonly class="form-control-plaintext"/>
                </div>
                <div class="formItem smallItem">
                    <label for="locationSurveyCount">Loc Survey</label>
                    <input type="text" id="locationSurveyCount" value='<c:out value="${locationSurveyCount}"/>' readonly class="form-control-plaintext"/>
                </div>
                <div class="formItem smallItem">
                    <label for="niinUpdateCount">NIIN Update</label>
                    <input type="text" id="niinUpdateCount" value='<c:out value="${niinUpdateCount}"/>' readonly class="form-control-plaintext"/>
                </div>
                <div class="formItem smallItem">
                    <label for="shelfLifeCount">Item Exp</label>
                    <input type="text" id="shelfLifeCount" value='<c:out value="${shelfLifeCount}"/>' readonly class="form-control-plaintext"/>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>


<script>
    var homeMenu = new HomeMenu(smvApp);
    homeMenu.configureButtons();
    if (${receivingEnabled}) $("#btnReceiving").prop("disabled", false);
    if (${stowEnabled}) $("#btnStowing").prop("disabled", false);
    if (${pickEnabled}) $("#btnPicking").prop("disabled", false);
    if (${shippingEnabled}) $("#btnShipping").prop("disabled", false);
    if (${inventoryEnabled}) $("#btnInventory").prop("disabled", false);

    //only show the Test Print button if the feature is enabled
    if (${printTestEnabled}) {
        $("#btnTestPrint").show();
        $("#btnFiller").hide();
    }
</script>

<jsp:include page="/WEB-INF/jsp/global/print.jsp"/>
