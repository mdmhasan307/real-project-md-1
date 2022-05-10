var PickingControlledNiinDetails = (function () {
    function PickingControlledNiinDetails(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    PickingControlledNiinDetails.prototype.configureButtons = function () {
        var self = this;
        $(".btnSubmitId").on("click", function () {
            var form = $("#pickForm")[0];
            self.smvApp.processPageSubmission(self.submitPath, $("#pickForm").serialize(), form);
        });
        $(".btnMainMenuId").on("click", function () {
            self.smvApp.processDeassign("/main/picking");
        });
        $(".btnExitId").on("click", function () {
            self.smvApp.processDeassign("/exit/picking");
        });
        $("#btnAdd").on("click", function () {
            var serial = $("#serial").val();
            serial = self.smvApp.escapeIllegalInputCharacters(serial);
            if (serial != null && serial.trim().length > 0) {
                if ($("#serials option[value=" + serial + "]").length > 0) {
                    var warningMessages = [{
                            message: "Serial Number was a duplicate of one already entered",
                            type: "VALIDATION_WARNING"
                        }];
                    self.smvApp.setMessages(warningMessages, false);
                    self.smvApp.openNotifications();
                }
                else {
                    self.smvApp.additemToList(serial, $('#serials'), $("#serialListGroup"), true);
                }
                $("#serial").val("");
            }
        });
    };
    return PickingControlledNiinDetails;
}());
