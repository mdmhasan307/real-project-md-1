class ShelfLifeDetail {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $("#btnConfirm").on("click", function () {
            let formWithOption = self.submitPath + "/detail/confirm";
            self.smvApp.processPageSubmission(formWithOption, $("#shelfLifeForm").serialize());
        });
        $("#btnCannotExtend").on("click", function () {
            let formWithOption = self.submitPath + "/detail/noextend";
            self.smvApp.processPageSubmission(formWithOption, $("#shelfLifeForm").serialize());
        });
        $("#btnSkip").on("click", function () {
            let formWithOption = self.submitPath + "/detail/skip";
            self.smvApp.processPageSubmission(formWithOption, $("#shelfLifeForm").serialize());
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/inventory");
        });
    }
}
