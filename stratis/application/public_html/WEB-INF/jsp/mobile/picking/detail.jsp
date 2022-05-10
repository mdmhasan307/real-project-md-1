<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="pickForm" modelAttribute="pickingDetailInput">
    <jsp:include page="detailsInput.jsp">
        <jsp:param name="editable" value="true"/>
    </jsp:include>
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
            <button type="button" class="btn btn-primary btnSubmitId">Submit</button>
            <button type="button" class="btn btn-primary btnBypassId">Bypass</button>
            <button type="button" class="btn btn-primary btnRefuseId">Refuse</button>
            <button type="button" class="btn btn-primary btnMainMenuId">Main Menu</button>
            <button type="button" class="btn btn-primary btnExitId">Exit</button>
        </div>
    </div>
</form:form>
<script>
    var pickingDetails = new PickingDetails(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    pickingDetails.configureButtons();
</script>

<jsp:include page="/WEB-INF/jsp/global/print.jsp"/>