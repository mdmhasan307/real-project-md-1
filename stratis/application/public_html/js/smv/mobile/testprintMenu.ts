class TestPrintMenu {
    private smvApp;

    constructor(smvApp) {
        this.smvApp = smvApp;
    }

    configureButtons() {
        let self = this;
        $("#btnTestPrint").on("click", function () {
            self.smvApp.processGet("mobile/testprint");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processGet("mobile/home");
        });
    }
}