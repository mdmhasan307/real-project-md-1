class PickingByNormal {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    processForm() {
        let self = this;
        self.smvApp.processPageSubmission(self.submitPath, $("#pickByNormalForm").serialize());
    }
}
