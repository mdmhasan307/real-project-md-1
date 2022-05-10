class PickingControlledNiinDetails {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $(".btnSubmitId").on("click", function () {
            let form = $("#pickForm")[0] as HTMLFormElement;
            self.smvApp.processPageSubmission(self.submitPath, $("#pickForm").serialize(), form);
        });
        $(".btnMainMenuId").on("click", function () {
            self.smvApp.processDeassign("/main/picking");
        });
        $(".btnExitId").on("click", function () {
            self.smvApp.processDeassign("/exit/picking");
        });

        $("#btnAdd").on("click", function () {
            let serial = $("#serial").val() as string;
            serial = self.smvApp.escapeIllegalInputCharacters(serial);

            if (serial != null && serial.trim().length > 0) {
                if ($("#serials option[value=" + serial + "]").length > 0) {
                    //serial already exists
                    let warningMessages = [{
                        message: "Serial Number was a duplicate of one already entered",
                        type: "VALIDATION_WARNING"
                    }];
                    self.smvApp.setMessages(warningMessages, false);
                    self.smvApp.openNotifications();
                } else {
                    self.smvApp.additemToList(serial, $('#serials'), $("#serialListGroup"), true);
                }
                $("#serial").val("");
            }
        })
    }

}
