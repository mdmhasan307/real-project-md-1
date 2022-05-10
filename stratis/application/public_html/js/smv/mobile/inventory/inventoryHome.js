var InventoryHomeMenu = (function () {
    function InventoryHomeMenu(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    InventoryHomeMenu.prototype.configureButtons = function () {
        var self = this;
        $("#btnInventory").on("click", function () {
            var formWithOption = self.submitPath + "/selection/inventory";
            self.smvApp.processPageSubmission(formWithOption);
        });
        $("#btnLocationSurvey").on("click", function () {
            var formWithOption = self.submitPath + "/selection/locationSurvey";
            self.smvApp.processPageSubmission(formWithOption, $("#inventoryHomeForm").serialize());
        });
        $("#btnShelfLife").on("click", function () {
            var formWithOption = self.submitPath + "/selection/shelfLife";
            self.smvApp.processPageSubmission(formWithOption, $("#inventoryHomeForm").serialize());
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/inventory");
        });
    };
    return InventoryHomeMenu;
}());
