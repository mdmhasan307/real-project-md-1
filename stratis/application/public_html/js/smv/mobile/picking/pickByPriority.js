var PickingByPriority = (function () {
    function PickingByPriority(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    PickingByPriority.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#pickByPriorityForm")[0];
            self.smvApp.processPageSubmission(self.submitPath, $("#pickByPriorityForm").serialize(), form);
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processDeassign("/main/picking");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/picking");
        });
    };
    return PickingByPriority;
}());
