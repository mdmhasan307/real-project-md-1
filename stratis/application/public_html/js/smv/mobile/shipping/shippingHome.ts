class ShippingHomeMenu {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $("#btnAddContainer").on("click", function () {
            self.smvApp.processGet("mobile/shipping/addcontainer");
        });
        $("#btnReviewLocation").on("click", function () {
            self.smvApp.processGet("mobile/shipping/reviewlocation");
        });
        $("#btnManifest").on("click", function () {
            self.smvApp.processGet("mobile/shipping/manifest");
        });
        $("#btnAcknowledgeShipment").on("click", function () {
            self.smvApp.processGet("mobile/shipping/acknowledgeshipment");
        });
        $("#btnRemarkAac").on("click", function () {
            self.smvApp.processGet("mobile/shipping/remarkaac");
        });
        $("#btnPalletRelocation").on("click", function () {
            self.smvApp.processGet("mobile/shipping/palletrelocation");
        });
        $("#btnTransshipment").on("click", function () {
            self.smvApp.processGet("mobile/shipping/transshipment");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/shipping");
        });
    }
}
