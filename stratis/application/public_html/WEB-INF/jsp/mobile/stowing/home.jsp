<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<form:form id="stowingHomeForm" modelAttribute="stowSearchCriteria" method="POST">
    <div class="flex-form-container">
        <div class="formItem">
            <label for="sidText">SID</label>
            <form:input id="sidText" path="sid" type="text" name="sidText" placeholder="sid" class="form-control form-control-sm"
                        required="true" onkeydown="return event.key != 'Enter';"/>
            <div class="flex-button-container">
                <div class="flex-button-group singleButton">
                    <button type="button" class="btn btn-primary" id="btnAdd">Add SID</button>
                </div>
            </div>
        </div>
        <div class="formItem">
            <label for="sids">SIDS</label>
            <div>
                <ul class="list-group custom" id="sidList">
                    <c:forEach items="${stows}" var="stow">
                        <li class="list-group-item custom"><span class="float-left lgi-custom-left">${stow.sid}</span></li>
                    </c:forEach>
                </ul>
            </div>
            <select path="stowId" id="sids" name="sids" size="10" disabled="true" style="display:none"
                    class="input_and_button_height w-100">
                <c:forEach items="${stows}" var="stow">
                    <option value="${stow.stowId}">${stow.sid}</option>
                </c:forEach>
            </select>
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
    var stowingHome = new StowingHome(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    stowingHome.configureButtons();
</script>
