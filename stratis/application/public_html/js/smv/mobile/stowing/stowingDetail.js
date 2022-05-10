var StowingDetails = (function () {
    function StowingDetails(smvApp, formPath) {
        this.submitPath = formPath;
        this.smvApp = smvApp;
    }
    StowingDetails.prototype.configureButtons = function () {
        var self = this;
        var loss = $("#stowLoss");
        $("#btnSubmit").on("click", function () {
            self.destroyLocationDialog();
            self.smvApp.processPageSubmission("mobile/stowing/detail/submit", $("#stowDetailsForm").serialize())
                .then(function (result) {
                if (result.result === "VALIDATION_WARNINGS") {
                    $("#btnLoss").unbind("click");
                    $("#btnLoss").hide();
                    loss.val("");
                    if (result.responseFlags.indexOf("stowLoss") != -1) {
                        $("#btnLoss").on("click", function () {
                            loss.val("loss");
                            self.smvApp.processPageSubmission("mobile/stowing/detail/submit", $("#stowDetailsForm").serialize());
                        }).show();
                    }
                    if (result.responseFlags.indexOf("locationMisMatch") != -1) {
                        self.showLocationNotificationDialog();
                    }
                }
            });
        });
        $("#btnBypass").on("click", function () {
            self.smvApp.processPageSubmission("mobile/stowing/detail/bypass", $("#stowDetailsForm").serialize());
            self.destroyLocationDialog();
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processDeassign("/main/stowing");
            self.destroyLocationDialog();
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/stowing");
            self.destroyLocationDialog();
        });
        $("#btnLoss").hide();
    };
    StowingDetails.prototype.configureActions = function () {
        $(document).ready(function () {
        });
    };
    StowingDetails.prototype.showLocationNotificationDialog = function () {
        var self = this;
        $("#stowRelocate").val("");
        $("#enteredLoc").text($("#location").val());
        $("#stowLoc").text($("#loc").val());
        $("#newdLoc").text($("#location").val());
        $("#locationNotification").dialog({
            autoOpen: true,
            classes: {
                "ui-dialog": "smv-dialog",
                "ui-dialog-content": "smv-dialog-content",
                "ui-dialog-buttonpane": "smv-dialog-buttonpane"
            },
            modal: true,
            resizable: true,
            draggable: true,
            title: 'Relocate'
        });
        $("#btnYes").off("click").on("click", function () {
            $("#locationNotification").dialog('close');
            $("#stowRelocate").val("relocate");
            var promise = self.smvApp.processPageSubmission("mobile/stowing/detail/submit", $("#stowDetailsForm").serialize());
            promise.then(function (response) {
                if (response.result === "SUCCESS" && response.redirectUrl != null) {
                    self.destroyLocationDialog();
                }
            });
            $("#stowRelocate").val("");
        });
        $("#btnNo").off("click").on("click", function () {
            $("#locationNotification").dialog('close');
        });
    };
    StowingDetails.prototype.destroyLocationDialog = function () {
        if ($("#locationNotification").hasClass("ui-dialog-content")) {
            $("#locationNotification").dialog('destroy');
        }
    };
    return StowingDetails;
}());
