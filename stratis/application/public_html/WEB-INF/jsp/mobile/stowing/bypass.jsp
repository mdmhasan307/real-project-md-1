<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<form:form id="stowBypasssForm" method="POST">
    <div class="flex-form-container">
        <div class="formItem">
            <label for="error">Bypass Reason</label>
            <select id="error" name="error" class="form-control form-control-lg" required="true">
                <option value="" disabled selected>Select a Reason</option>
                <c:forEach items="${errors}" var="e">
                    <option value="${e.id}">${e.description}</option>
                </c:forEach>
            </select>
        </div>
    </div>
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
            <button type="button" class="btn btn-primary" id="btnSubmit">Submit</button>
            <button type="button" class="btn btn-primary" id="btnCancel">Cancel</button>
            <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
        </div>
    </div>
</form:form>

<script>
    var stowBypass = new StowBypass(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    stowBypass.configureButtons();
</script>