<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<div class="flex-button-container">
    <div class="flex-button-group frmBtns">
        <button type="button" class="btn btn-primary" id="btnAddContainer">Add Container</button>
        <button type="button" class="btn btn-primary" id="btnReviewLocation">Review Location</button>
        <button type="button" class="btn btn-primary" id="btnManifest">Manifest</button>
        <button type="button" class="btn btn-primary" id="btnAcknowledgeShipment">Acknowledge Shipment</button>
    </div>
</div>
<div class="flex-button-container">
    <div class="flex-button-group frmBtns">
        <button type="button" class="btn btn-primary" id="btnRemarkAac">Remark AAC</button>
        <button type="button" class="btn btn-primary" id="btnPalletRelocation">Pallet Relocation</button>
        <button type="button" class="btn btn-primary" id="btnTransshipment">Transshipment</button>
        <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
    </div>
</div>
<script>
    var shippingHome = new ShippingHomeMenu(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    shippingHome.configureButtons();
</script>
