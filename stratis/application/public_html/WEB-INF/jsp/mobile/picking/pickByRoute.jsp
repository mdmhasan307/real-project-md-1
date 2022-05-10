<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="pickByRouteForm" modelAttribute="pickingCriteria" method="POST">
    <div class="flex-form-container">
        <div class="formItem">
            <label for="routeId">Route</label>
            <form:select path="routeId" id="routeId" class="form-control form-control-lg" required="true">
                <option value="" disabled selected>Select an Route</option>
                <form:options items="${routes}" itemValue="routeId" itemLabel="routeName"/>
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
    var pickingByRoute = new PickingByRoute(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    pickingByRoute.configureButtons();
</script>

