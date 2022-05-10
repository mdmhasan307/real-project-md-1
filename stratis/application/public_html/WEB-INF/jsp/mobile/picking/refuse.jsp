<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="pickRefuseForm" modelAttribute="pickingDetailInput" method="POST">
    <div class="flex-form-container">
        <div class="formItem">
            <label for="reason">Refusal Reason</label>
            <form:select path="refuseReason" id="reason" class="form-control form-control-lg" required="true">
                <option value="" disabled selected>Select a Reason</option>
                <form:options items="${reasons}" itemLabel="description" itemValue="id"/>
            </form:select>
        </div>
    </div>
    <div>
        <jsp:include page="detailsInput.jsp">
            <jsp:param name="editable" value="false"/>
            <jsp:param name="bypassOrRefuse" value="true"/>
        </jsp:include>
    </div>
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
            <button type="button" class="btn btn-primary btnSubmit">Submit</button>
            <button type="button" class="btn btn-primary btnReturn">Return</button>
            <button type="button" class="btn btn-primary btnExit">Exit</button>
        </div>
    </div>
</form:form>
<script>
    var pickRefuse = new PickRefuse(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    pickRefuse.configureButtons();
</script>

