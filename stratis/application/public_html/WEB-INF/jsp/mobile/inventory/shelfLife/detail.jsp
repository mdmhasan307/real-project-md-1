<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
    <form:form id="shelfLifeForm" modelAttribute="shelfLifeDetailInput" method="POST">
        <div>
            <jsp:include page="detailsInput.jsp">
                <jsp:param name="editable" value="false"/>
            </jsp:include>
        </div>
        <div class="flex-button-container">
            <div class="flex-button-group frmBtns">
                <button type="button" class="btn btn-primary" id="btnConfirm">Confirm</button>
                <button type="button" class="btn btn-primary" id="btnCannotExtend">Cannot Extend</button>
                <button type="button" class="btn btn-primary" id="btnSkip">Skip</button>
                <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
            </div>
        </div>
    </form:form>
<script>
    var shelfLifeDetail = new ShelfLifeDetail(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    shelfLifeDetail.configureButtons();
</script>