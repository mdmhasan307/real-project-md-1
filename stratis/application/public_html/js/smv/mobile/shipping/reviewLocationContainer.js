var ReviewLocationContainer = (function () {
    function ReviewLocationContainer(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    ReviewLocationContainer.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#reviewLocationForm")[0];
            self.smvApp.processPageSubmission(self.submitPath + "/handle", $("#reviewLocationForm").serialize(), form);
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processGet("mobile/shipping/reviewlocation");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/shipping");
        });
    };
    return ReviewLocationContainer;
}());
