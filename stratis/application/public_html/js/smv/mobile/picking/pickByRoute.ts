class PickingByRoute {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $("#btnSubmit").on("click", function () {
            let form = $("#pickByRouteForm")[0] as HTMLFormElement;
            self.smvApp.processPageSubmission(self.submitPath, $("#pickByRouteForm").serialize(), form);
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processDeassign("/main/picking");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/picking");
        });
    }
}
