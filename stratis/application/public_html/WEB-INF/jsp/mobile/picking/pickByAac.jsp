<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="pickByAacForm" modelAttribute="pickingCriteria" method="POST">
    <div class="flex-form-container">
        <div class="formItem">
            <label for="aac">AAC</label>
            <form:select path="aac" id="aac" class="form-control form-control-lg" required="true">
                <option value="" disabled selected>Select an AAC</option>
                <form:options items="${aacs}"/>
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
    var pickingByAac = new PickingByAac(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    pickingByAac.configureButtons();
</script>

