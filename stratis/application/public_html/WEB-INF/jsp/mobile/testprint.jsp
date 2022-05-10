<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form id="testPrintForm" method="POST">
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
            <button type="button" class="btn btn-primary" id="btnTestPrint">Test-Print</button>
            <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
        </div>
    </div>
</form>

<div width="100%" style="text-align:center;">
Print test submitted.
</div>

<script>
    var menu = new TestPrintMenu(smvApp);
    menu.configureButtons();
</script>

<jsp:include page="/WEB-INF/jsp/global/print.jsp"/>