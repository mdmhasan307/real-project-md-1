class ReviewLocationHomeMenu {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $("#btnContainer").on("click", function () {
            self.smvApp.processGet("mobile/shipping/reviewlocation/container");
        });
        $("#btnBarcode").on("click", function () {
            self.smvApp.processGet("mobile/shipping/reviewlocation/barcode");
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processGet("mobile/shipping");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/shipping");
        });
    }
}
