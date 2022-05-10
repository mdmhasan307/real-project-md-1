class InventoryHomeMenu {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $("#btnInventory").on("click", function () {
            let formWithOption = self.submitPath + "/selection/inventory";
            self.smvApp.processPageSubmission(formWithOption);
        });
        $("#btnLocationSurvey").on("click", function () {
            let formWithOption = self.submitPath + "/selection/locationSurvey";
            self.smvApp.processPageSubmission(formWithOption, $("#inventoryHomeForm").serialize());
        });
        $("#btnShelfLife").on("click", function () {
            let formWithOption = self.submitPath + "/selection/shelfLife";
            self.smvApp.processPageSubmission(formWithOption, $("#inventoryHomeForm").serialize());
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/inventory");
        });
    }
}
