class InventoryDetail {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $("#btnSubmit").on("click", function () {
            let form = $("#inventoryDetailForm")[0] as HTMLFormElement;
            $('#serials option').prop('selected', true);
            self.smvApp.processPageSubmission(self.submitPath, $("#inventoryDetailForm").serialize(), form).then((result: any) => {
                if (result.responseFlags.indexOf("quantityMisMatch") != -1) {
                    $("#reconfirmQtyInput").show();
                    $("#reconfirmQtyRequired").val("true");
                    $("#reconfirmLocationQty").attr("required", "true");
                }
            });
        });
        $("#btnBypass").on("click", function () {
            let formWithOption = self.submitPath + "/bypass";
            self.smvApp.processPageSubmission(formWithOption, $("#inventoryDetailForm").serialize());
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/inventory");
        });

        $("#btnAdd").on("click", function () {
            let serial = $("#serial").val() as string;
            serial = self.smvApp.escapeIllegalInputCharacters(serial);

            if (serial != null && serial.trim().length > 0) {
                if ($("#serials option[value='" + serial + "']").length == 0) {
                    let formWithOption = self.submitPath + "/addSerial";
                    let form = $("#inventoryDetailForm")[0] as HTMLFormElement;
                    self.smvApp.processPageSubmission(formWithOption, $("#inventoryDetailForm").serialize()).then((response: any) => {
                        if (response.result === "SUCCESS") {
                            //this will open the notifications if a warning only message was returned.
                            self.smvApp.openNotifications();
                            self.smvApp.additemToList(serial, $('#serials'), $("#serialListGroup"), true);
                        }
                        $("#serial").val("");
                    });
                } else {
                    let warningMessages = [{
                        message: "Serial Number was a duplicate of one already entered",
                        type: "NOTIFICATION"
                    }];
                    self.smvApp.setMessages(warningMessages, false);
                    self.smvApp.openNotifications();
                    $("#serial").val("");
                }
            } else {
                let warningMessages = [{
                    message: "Serial Number must be entered.",
                    type: "NOTIFICATION"
                }];
                self.smvApp.setMessages(warningMessages, false);
                self.smvApp.openNotifications();
            }


        });
    }
}
