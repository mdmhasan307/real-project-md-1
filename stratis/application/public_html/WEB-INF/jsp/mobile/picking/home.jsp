<div class="flex-button-container">
    <div class="flex-button-group frmBtns">
        <button type="button" class="btn btn-primary" id="btnNormal">Pick By Normal</button>
        <button type="button" class="btn btn-primary" id="btnAac">Pick By AAC</button>
        <button type="button" class="btn btn-primary" id="btnPriority">Pick By Priority</button>
        <button type="button" class="btn btn-primary" id="btnRoute">Pick By Route</button>
        <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
    </div>
</div>
<script>
    var pickingHome = new PickingHomeMenu(smvApp);
    pickingHome.configureButtons();
</script>

<jsp:include page="/WEB-INF/jsp/global/print.jsp"/>
