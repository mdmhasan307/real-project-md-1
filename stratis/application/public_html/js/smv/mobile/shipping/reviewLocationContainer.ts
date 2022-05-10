class ReviewLocationContainer {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $("#btnSubmit").on("click", function () {
            let form = $("#reviewLocationForm")[0] as HTMLFormElement;
            self.smvApp.processPageSubmission(self.submitPath + "/handle", $("#reviewLocationForm").serialize(), form);
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processGet("mobile/shipping/reviewlocation");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/shipping");
        });
    }
}
