class PickRefuse {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $(".btnSubmit").on("click", function () {
            let form = $("#pickRefuseForm")[0] as HTMLFormElement;
            self.smvApp.processPageSubmission(self.submitPath, $("#pickRefuseForm").serialize(), form);
        });
        $(".btnReturn").on("click", function () {
            self.smvApp.processGet("mobile/picking/detail");
        });
        $(".btnExit").on("click", function () {
            self.smvApp.processGet("mobile/home");
        });
    }
}
