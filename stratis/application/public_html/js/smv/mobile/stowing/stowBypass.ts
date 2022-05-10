class StowBypass {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $("#btnSubmit").on("click", function () {
            let form = $("#stowBypasssForm")[0] as HTMLFormElement;
            self.smvApp.processPageSubmission(self.submitPath, $("#stowBypasssForm").serialize(), form);
        });
        $("#btnCancel").on("click", function () {
            self.smvApp.processGet("mobile/stowing/detail");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/stowing");
        });
    }
}
