var PickingDetails = (function () {
    function PickingDetails(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    PickingDetails.prototype.configureButtons = function () {
        var self = this;
        $(".btnSubmitId").on("click", function () {
            var form = $("#pickForm")[0];
            self.smvApp.processPageSubmission(self.submitPath, $("#pickForm").serialize(), form).then(function (result) {
                if (result.responseFlags.indexOf("PartialPickAttempted") != -1)
                    $("#partialPickAttempted").val("true");
            });
        });
        $(".btnBypassId").on("click", function () {
            var formWithOption = self.submitPath + "/bypass";
            self.smvApp.processPageSubmission(formWithOption, $("#pickForm").serialize(), null);
        });
        $(".btnRefuseId").on("click", function () {
            var formWithOption = self.submitPath + "/refuse";
            self.smvApp.processPageSubmission(formWithOption, $("#pickForm").serialize(), null);
        });
        $(".btnMainMenuId").on("click", function () {
            self.smvApp.processDeassign("/main/picking");
        });
        $(".btnExitId").on("click", function () {
            self.smvApp.processDeassign("/exit/picking");
        });
    };
    return PickingDetails;
}());
