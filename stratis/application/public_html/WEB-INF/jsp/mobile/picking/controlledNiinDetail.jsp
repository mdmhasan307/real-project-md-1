<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="pickForm" modelAttribute="pickingDetailInput">
    <div class="flex-form-container">
        <div class="formItem">
            <label for="serial">Serial</label>
            <input type="text" id="serial" placeholder="serial" maxlength="30" class="form-control form-control-sm"/>
            <div class="flex-button-container">
                <div class="flex-button-group singleButton">
                    <button type="button" class="btn btn-primary" id="btnAdd">Add Serial</button>
                </div>
            </div>
        </div>
        <div class="formItem">
            <label for="serials">Serial List</label>
            <div>
                <ul class="list-group custom" id="serialListGroup">
                </ul>
            </div>
        </div>
    </div>
    <jsp:include page="detailsInput.jsp">
        <jsp:param name="editable" value="false"/>
    </jsp:include>
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
            <button type="button" class="btn btn-primary btnSubmitId">Submit</button>
            <button type="button" class="btn btn-primary btnMainMenuId">Main Menu</button>
            <button type="button" class="btn btn-primary btnExitId">Exit</button>
        </div>
    </div>
    <select id="serials" name="serials" size="10" multiple="multiple" style="display:none" class="input_and_button_height w-100"> </select>

</form:form>
<script>
    var pickingControlledNiinDetails = new PickingControlledNiinDetails(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    pickingControlledNiinDetails.configureButtons();
</script>
