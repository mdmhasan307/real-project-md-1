var PickingByAac = (function () {
    function PickingByAac(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    PickingByAac.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#pickByAacForm")[0];
            self.smvApp.processPageSubmission(self.submitPath, $("#pickByAacForm").serialize(), form);
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processDeassign("/main/picking");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/picking");
        });
    };
    return PickingByAac;
}());
