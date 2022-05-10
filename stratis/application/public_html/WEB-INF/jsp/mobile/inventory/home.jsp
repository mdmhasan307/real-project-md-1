<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form id="inventoryHomeForm" method="POST">
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
            <button type="button" class="btn btn-primary" id="btnInventory" disabled>Inventory</button>
            <button type="button" class="btn btn-primary" id="btnLocationSurvey" disabled>Location Survey</button>
            <button type="button" class="btn btn-primary" id="btnShelfLife" disabled>Shelf Life</button>
            <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
        </div>
    </div>
</form>
<script>
    var inventoryHome = new InventoryHomeMenu(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    inventoryHome.configureButtons();

    if (${inventoryEnabled}) $("#btnInventory").prop("disabled", false);
    if (${locationSurveyEnabled}) $("#btnLocationSurvey").prop("disabled", false);
    if (${shelfLifeEnabled}) $("#btnShelfLife").prop("disabled", false);

</script>

