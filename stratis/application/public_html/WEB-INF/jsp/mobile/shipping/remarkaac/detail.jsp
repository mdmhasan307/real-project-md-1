<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form id="remarkAacForm" method="POST">
    <div class="flex-form-container">
        <div class="formItem">
            <label for="leadTcn">Lead TCN:</label><span class="required" title="Required Field">*</span>
            <input id="leadTcn" name="leadTcn" class="form-control form-control-sm" required="true"
                   maxlength="17" minlength="17" pattern="\w*" title="Lead TCN must be 17 alphanumeric characters" />
        </div>

        <div class="formItem"><label for="remarkAac">Remark AAC:</label><span class="required" title="Required Field">*</span>
            <input id="remarkAac" name="remarkAac" class="form-control form-control-sm" required="true"
                   maxlength="6" minlength="6" pattern="\w*" title="Remark AAC must be 6 alphanumeric characters or less" />
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
    var remarkAAC = new RemarkAAC(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    remarkAAC.configureButtons();
</script>