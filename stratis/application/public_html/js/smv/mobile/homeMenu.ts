class HomeMenu {
    private smvApp;

    constructor(smvApp) {
        this.smvApp = smvApp;
    }

    configureButtons() {
        let self = this;

        $("#btnSwitch").on("click", function () {
            self.smvApp.processGet("mobile/switch");
        });
        $("#btnDesktop").on("click", function () {
            location.href = self.smvApp.rootUrl + "/faces/WarehouseHome";
        });
        $("#btnToggleFullscreen").on("click", function () {
            self.smvApp.toggleFullScreen();
        });
        $("#btnReceiving").on("click", function () {
            self.smvApp.processGet("mobile/receiving");
        });
        $("#btnStowing").on("click", function () {
            self.smvApp.processGet("mobile/stowing");
        });
        $("#btnPicking").on("click", function () {
            self.smvApp.processGet("mobile/picking");
        });
        $("#btnShipping").on("click", function () {
            self.smvApp.processGet("mobile/shipping");
        });
        $("#btnInventory").on("click", function () {
            self.smvApp.processGet("mobile/inventory");
        });
        $("#btnTestPrint").on("click", function () {
            self.smvApp.processGet("mobile/testprint");
        });
    }
}