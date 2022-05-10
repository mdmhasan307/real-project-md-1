var PickRefuse = (function () {
    function PickRefuse(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    PickRefuse.prototype.configureButtons = function () {
        var self = this;
        $(".btnSubmit").on("click", function () {
            var form = $("#pickRefuseForm")[0];
            self.smvApp.processPageSubmission(self.submitPath, $("#pickRefuseForm").serialize(), form);
        });
        $(".btnReturn").on("click", function () {
            self.smvApp.processGet("mobile/picking/detail");
        });
        $(".btnExit").on("click", function () {
            self.smvApp.processGet("mobile/home");
        });
    };
    return PickRefuse;
}());
