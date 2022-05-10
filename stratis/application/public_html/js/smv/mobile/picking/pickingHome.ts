class PickingHomeMenu {
    private smvApp;

    constructor(smvApp) {
        this.smvApp = smvApp;
    }

    configureButtons() {
        let self = this;
        $("#btnAac").on("click", function () {
            self.smvApp.processGet("mobile/picking/byAac");
        });
        $("#btnNormal").on("click", function () {
            let normalSearch = self.smvApp.fullUrl + "/mobile/picking/byNormal";
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
    }
}
