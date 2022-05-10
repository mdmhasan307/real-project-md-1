class PalletRelocation {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $("#btnSubmit").on("click", function () {
            let form = $("#palletRelocationForm")[0] as HTMLFormElement;
            self.smvApp.processPageSubmission(self.submitPath + "/handle", $("#palletRelocationForm").serialize(), form);
        });
        $("#btnSearch").on("click", function () {
            let oldLoc = $("#oldLocation").val();
            if (oldLoc.length === 0) {
                let warningMessages = [{
                    message: "Old Location is required for Search",
                    type: "VALIDATION_WARNING"
                }];
                self.smvApp.setMessages(warningMessages, false);
                self.smvApp.openNotifications();
                return;
            }
            self.smvApp.processPageSubmission(self.submitPath + "/search", $("#palletRelocationForm").serialize(), null).then((result:any) => {
                if (result.foundLocation) {
                    $("#aac").val(result.aac);
                    $("#floorLocation").val(result.floorLocation);
                    $("#leadTcn").val(result.leadTcn);
                } else {
                    let warningMessages = [{
                        message: "Old Location " + oldLoc + " not found",
                        type: "VALIDATION_WARNING"
                    }];
                    $("#aac").val("");
                    $("#floorLocation").val("");
                    $("#leadTcn").val("");
                    self.smvApp.setMessages(warningMessages, false);
                    self.smvApp.openNotifications();
                }
            });
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processGet("mobile/shipping");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/shipping");
        });
    }
}
