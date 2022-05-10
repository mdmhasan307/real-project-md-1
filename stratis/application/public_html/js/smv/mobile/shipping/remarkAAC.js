var RemarkAAC = (function () {
    function RemarkAAC(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    RemarkAAC.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#remarkAacForm")[0];
            self.smvApp.processPageSubmission(self.submitPath + "/handle", $("#remarkAacForm").serialize(), form);
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processGet("mobile/shipping");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/shipping");
        });
    };
    return RemarkAAC;
}());
