<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="inventoryDetailForm" modelAttribute="inventoryItemInput" method="POST">
    <div>
        <jsp:include page="detailsInput.jsp">
            <jsp:param name="editable" value="true"/>
        </jsp:include>
    </div>
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
            <button type="button" class="btn btn-primary" id="btnSubmit">Submit</button>
            <button type="button" class="btn btn-primary" id="btnBypass">Bypass</button>
            <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
        </div>
    </div>
</form:form>
<script>
    var inventoryDetail = new InventoryDetail(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    inventoryDetail.configureButtons();

    $("#reconfirmQtyInput").hide();
</script>