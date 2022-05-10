var ReviewLocationHomeMenu = (function () {
    function ReviewLocationHomeMenu(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    ReviewLocationHomeMenu.prototype.configureButtons = function () {
        var self = this;
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
    };
    return ReviewLocationHomeMenu;
}());
