class SwitchWorkstation {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $("#btnSubmit").on("click", function () {
            // Add userTypeId to the form data if it is disabled since the element is disabled
            const isDisabled = $("#userTypeId").is(':disabled');
            let data;
            if (isDisabled) {
                const serializedUsertypeId = "&userTypeId=" + $("#userTypeId").val();
                data = $("#switchWorkstationForm").serialize() + serializedUsertypeId;
            }
            else {
                data = $("#switchWorkstationForm").serialize()
            }
            self.smvApp.processPageSubmission(self.submitPath, data);
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processGet("mobile/home");
        });
    }

    configureUserTypeChangeProcessing() {
        $("#userTypeId").change(function () {
            $("#userTypeName").val($("#userTypeId option:selected").text());
        });
    }
}