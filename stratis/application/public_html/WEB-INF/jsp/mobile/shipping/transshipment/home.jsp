<div class="flex-button-container">
  <div class="flex-button-group frmBtns">
    <button id="btnTcn" type="button" class="btn btn-primary">TCN</button>
    <button id="btnDocument" type="button" class="btn btn-primary">Document Number</button>
    <button id="btnContract" type="button" class="btn btn-primary">Contract Number</button>
    <button id="btnMainMenu" type="button" class="btn btn-primary">Main Menu</button>
    <button id="btnExit" type="button" class="btn btn-primary">Exit</button>
  </div>
</div>
<script>
  var transshipmentHome = new TransshipmentHome(smvApp, "${requestScope['javax.servlet.forward.request_uri']}")
  transshipmentHome.configureButtons()
</script>
