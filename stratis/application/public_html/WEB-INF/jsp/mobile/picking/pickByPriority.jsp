<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="pickByPriorityForm" modelAttribute="pickingCriteria" method="POST">
    <div class="flex-form-container">
        <div class="formItem">
            <label for="priority">Priority</label>
            <form:select path="priority" id="priority" class="form-control form-control-lg" required="true">
                <option value="" disabled selected>Select a Priority</option>
                <form:options items="${priorities}" itemValue="key" itemLabel="value"/>
            </form:select>
        </div>
    </div>
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
            <button type="button" class="btn btn-primary" id="btnSubmit">Submit</button>
            <button type="button" class="btn btn-primary" id="btnMainMenu">Main Menu</button>
            <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
        </div>
    </div>
</form:form>
<script>
    var pickingByPriority = new PickingByPriority(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    pickingByPriority.configureButtons();
</script>

