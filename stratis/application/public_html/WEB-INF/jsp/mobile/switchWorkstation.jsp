<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<form:form id="switchWorkstationForm" modelAttribute="switchWorkstationCriteria" method="POST">
    <div class="flex-form-container">
        <div class="formItem">
            <label for="userTypeId">Role</label>
            <form:select path="userTypeId"
                         id="userTypeId"
                         disabled="${!switchRolesEnabled}"
                         class="form-control form-control-lg">
                <form:options items="${userTypes}"
                              itemValue="userTypeId"
                              itemLabel="type"/>
            </form:select>

        </div>
        <div class="formItem">
            <label for="workstationId">Workstation</label>
            <form:select path="workstationId" id="workstationId" class="form-control form-control-lg">
                <form:options items="${workstations}" itemValue="equipmentNumber" itemLabel="name"/>
            </form:select>
            <form:hidden path="userTypeName" id="userTypeName"/>
        </div>
    </div>
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
            <button type="button" class="btn btn-primary" id="btnSubmit">Submit</button>
            <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
        </div>
    </div>
</form:form>

<script>
    var switchWorkstation = new SwitchWorkstation(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    switchWorkstation.configureButtons();
    switchWorkstation.configureUserTypeChangeProcessing();
</script>