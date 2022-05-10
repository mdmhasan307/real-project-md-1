var ShippingManifest = (function () {
    function ShippingManifest(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    ShippingManifest.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#manifestForm")[0];
            self.smvApp.processPageSubmission(self.submitPath, $("#manifestForm").serialize(), form);
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processGet("mobile/shipping");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/shipping");
        });
    };
    return ShippingManifest;
}());
