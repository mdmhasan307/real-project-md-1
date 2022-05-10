var PickBypass = (function () {
    function PickBypass(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    PickBypass.prototype.configureButtons = function () {
        var self = this;
        $(".btnSubmit").on("click", function () {
            var form = $("#pickBypassForm")[0];
            self.smvApp.processPageSubmission(self.submitPath, $("#pickBypassForm").serialize(), form);
        });
        $(".btnReturn").on("click", function () {
            self.smvApp.processGet("mobile/picking/detail");
        });
        $(".btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/picking");
        });
    };
    return PickBypass;
}());
