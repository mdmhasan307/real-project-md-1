var InventoryDetail = (function () {
    function InventoryDetail(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    InventoryDetail.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#inventoryDetailForm")[0];
            $('#serials option').prop('selected', true);
            self.smvApp.processPageSubmission(self.submitPath, $("#inventoryDetailForm").serialize(), form).then(function (result) {
                if (result.responseFlags.indexOf("quantityMisMatch") != -1) {
                    $("#reconfirmQtyInput").show();
                    $("#reconfirmQtyRequired").val("true");
                    $("#reconfirmLocationQty").attr("required", "true");
                }
            });
        });
        $("#btnBypass").on("click", function () {
            var formWithOption = self.submitPath + "/bypass";
            self.smvApp.processPageSubmission(formWithOption, $("#inventoryDetailForm").serialize());
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/inventory");
        });
        $("#btnAdd").on("click", function () {
            var serial = $("#serial").val();
            serial = self.smvApp.escapeIllegalInputCharacters(serial);
            if (serial != null && serial.trim().length > 0) {
                if ($("#serials option[value='" + serial + "']").length == 0) {
                    var formWithOption = self.submitPath + "/addSerial";
                    var form = $("#inventoryDetailForm")[0];
                    self.smvApp.processPageSubmission(formWithOption, $("#inventoryDetailForm").serialize()).then(function (response) {
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
        });
    };
    return InventoryDetail;
}());
