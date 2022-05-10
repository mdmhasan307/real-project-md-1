var ShippingAddContainer = (function () {
    function ShippingAddContainer(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    ShippingAddContainer.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#addContainerForm")[0];
            self.smvApp.processPageSubmission(self.submitPath, $("#addContainerForm").serialize(), form)
                .then(function (result) {
                if (result.responseFlags.indexOf("recommend") != -1) {
                    self.smvApp.processAjaxSubmission(self.submitPath + "/recommend", $("#addContainerForm").serialize(), form)
                        .then(function (result) {
                        $("#recommend").val(result.location);
                        $("#stageLoc").val(result.location).prop("required", true);
                        $("#requireLocation").show();
                    });
                }
            });
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processGet("mobile/shipping");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/shipping");
        });
        $("#requireLocation").hide();
    };
    return ShippingAddContainer;
}());
