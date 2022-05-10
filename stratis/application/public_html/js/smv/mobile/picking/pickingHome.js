var PickingHomeMenu = (function () {
    function PickingHomeMenu(smvApp) {
        this.smvApp = smvApp;
    }
    PickingHomeMenu.prototype.configureButtons = function () {
        var self = this;
        $("#btnAac").on("click", function () {
            self.smvApp.processGet("mobile/picking/byAac");
        });
        $("#btnNormal").on("click", function () {
            var normalSearch = self.smvApp.fullUrl + "/mobile/picking/byNormal";
            self.smvApp.processPageSubmission(normalSearch, null, null);
        });
        $("#btnPriority").on("click", function () {
            self.smvApp.processGet("mobile/picking/byPriority");
        });
        $("#btnRoute").on("click", function () {
            self.smvApp.processGet("mobile/picking/byRoute");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/picking");
        });
    };
    return PickingHomeMenu;
}());
