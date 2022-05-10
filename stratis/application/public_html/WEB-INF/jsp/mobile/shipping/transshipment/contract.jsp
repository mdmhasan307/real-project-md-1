<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="transshipForm" modelAttribute="transshipmentInput">
    <jsp:include page="transshipmentInput.jsp">
        <jsp:param name="tcn" value="false"/>
        <jsp:param name="document" value="false"/>
        <jsp:param name="contract" value="true"/>
    </jsp:include>
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
            <button id="btnSubmit" type="button" class="btn btn-primary">Submit</button>
            <button id="btnMainMenu" type="button" class="btn btn-primary">Main Menu</button>
            <button id="btnExit" type="button" class="btn btn-primary">Exit</button>
        </div>
    </div>
</form:form>
<script>
    var transshipment = new Transshipment(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    transshipment.configureButtons();
</script>
