var ReceivingHome = (function () {
    function ReceivingHome(smvApp, formPath) {
        this.submitPath = formPath;
        this.smvApp = smvApp;
    }
    ReceivingHome.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#receivingHomeForm")[0];
            self.smvApp.processPageSubmission("mobile/receiving", $("#receivingHomeForm").serialize(), form);
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/receiving");
        });
        $("#chkPartialShipment").on("click", function () {
            $("#btnPartialShipment").toggleClass("active");
        });
    };
    return ReceivingHome;
}());
