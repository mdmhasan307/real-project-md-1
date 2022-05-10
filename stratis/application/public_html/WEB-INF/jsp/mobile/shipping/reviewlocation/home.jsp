<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<div class="flex-button-container">
    <div class="flex-button-group frmBtns">
        <button type="button" class="btn btn-primary" id="btnBarcode">Review By Barcode</button>
        <button type="button" class="btn btn-primary" id="btnContainer">Review By Container</button>
        <button type="button" class="btn btn-primary" id="btnMainMenu">Main Menu</button>
        <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
    </div>
</div>
<script>
    var reviewLocationHome = new ReviewLocationHomeMenu(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    reviewLocationHome.configureButtons();
</script>