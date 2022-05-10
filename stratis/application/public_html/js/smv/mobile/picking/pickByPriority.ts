class PickingByPriority {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $("#btnSubmit").on("click", function () {
            let form = $("#pickByPriorityForm")[0] as HTMLFormElement;
            self.smvApp.processPageSubmission(self.submitPath, $("#pickByPriorityForm").serialize(), form);
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processDeassign("/main/picking");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/picking");
        });
    }
}
