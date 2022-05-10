var ReceivingControlledNiinDetails = (function () {
    function ReceivingControlledNiinDetails(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    ReceivingControlledNiinDetails.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#receivingControlledNiinDetailsForm")[0];
            var qtyReceived = parseInt($("#qtyReceived").val());
            var serialCount = $("#serials option").length;
            if (serialCount != qtyReceived) {
                var warningMessages = [{
                        message: qtyReceived + " Serial Number(s) must be entered to submit",
                        type: "NOTIFICATION"
                    }];
                self.smvApp.setMessages(warningMessages, false);
                self.smvApp.openNotifications();
            }
            else {
                $('#serials option').prop('selected', true);
                self.smvApp.processPageSubmission(self.submitPath, $("#receivingControlledNiinDetailsForm").serialize(), form);
            }
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processDeassign("/main/receiving");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/receiving");
        });
        $("#btnAdd").on("click", function () {
            var serial = $("#serial").val();
            serial = self.smvApp.escapeIllegalInputCharacters(serial);
            var qtyReceived = parseInt($("#qtyReceived").val());
            var serialCount = $("#serials option").length;
            if (serialCount == qtyReceived) {
                var warningMessages = [{
                        message: "Already entered " + qtyReceived + " Serial Number(s)",
                        type: "NOTIFICATION"
                    }];
                self.smvApp.setMessages(warningMessages, false);
                self.smvApp.openNotifications();
                $("#serial").val("");
            }
            else {
                if (serial != null && serial.trim().length > 0) {
                    if ($("#serials option[value='" + serial + "']").length == 0) {
                        var formWithOption = self.submitPath + "/addSerial";
                        var form = $("#receivingControlledNiinDetailsForm")[0];
                        self.smvApp.processPageSubmission(formWithOption, $("#receivingControlledNiinDetailsForm").serialize()).then(function (response) {
                            if (response.result === "SUCCESS") {
                                self.smvApp.openNotifications();
                                self.smvApp.additemToList(serial, $('#serials'), $("#serialListGroup"), true);
                            }
                            $("#serial").val("");
                        });
                    }
                    else {
                        var warningMessages = [{
                                message: "Serial Number was a duplicate of one already entered",
                                type: "NOTIFICATION"
                            }];
                        self.smvApp.setMessages(warningMessages, false);
                        self.smvApp.openNotifications();
                        $("#serial").val("");
                    }
                }
                else {
                    var warningMessages = [{
                            message: "Serial Number must be entered.",
                            type: "NOTIFICATION"
                        }];
                    self.smvApp.setMessages(warningMessages, false);
                    self.smvApp.openNotifications();
                }
            }
        });
    };
    return ReceivingControlledNiinDetails;
}());
