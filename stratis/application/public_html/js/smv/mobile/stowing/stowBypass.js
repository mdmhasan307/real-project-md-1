var StowBypass = (function () {
    function StowBypass(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    StowBypass.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#stowBypasssForm")[0];
            self.smvApp.processPageSubmission(self.submitPath, $("#stowBypasssForm").serialize(), form);
        });
        $("#btnCancel").on("click", function () {
            self.smvApp.processGet("mobile/stowing/detail");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/stowing");
        });
    };
    return StowBypass;
}());
