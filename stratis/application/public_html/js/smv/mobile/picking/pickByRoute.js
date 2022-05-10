var PickingByRoute = (function () {
    function PickingByRoute(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    PickingByRoute.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#pickByRouteForm")[0];
            self.smvApp.processPageSubmission(self.submitPath, $("#pickByRouteForm").serialize(), form);
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processDeassign("/main/picking");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/picking");
        });
    };
    return PickingByRoute;
}());
