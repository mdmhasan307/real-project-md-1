class AcknowledgeShipment {
    private submitPath;
    private smvApp;
    private doRemove;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
        this.doRemove = false;
    }

    showConfirmDialog() {
        let self = this;
        $("#confirmShipment").dialog({
            autoOpen: true,
            classes: {
                "ui-dialog": "smv-dialog",
                "ui-dialog-content": "smv-dialog-content",
                "ui-dialog-buttonpane": "smv-dialog-buttonpane"
            },
            modal: true,
            resizable: true,
            draggable: true,
            title: 'Confirm Acknowledge Shipment'
        });

        $("#btnConfirm").off("click").on("click", function () {
            $("#confirmShipment").dialog('close');
            let form = $("#acknowledgeShipmentForm")[0] as HTMLFormElement;
            let promise = self.smvApp.processPageSubmission(self.submitPath + "/handle", $("#acknowledgeShipmentForm").serialize(), form);
            promise.then((response:any) => {
              if (response.result === "SUCCESS" && response.redirectUrl != null) {
                  // going to get the page again remove dialog so that it is not left on the page when the page is reloaded
                  // otherwise the dialogs buttons won't work if you open/close the dialog navigate away and return
                  $("#confirmShipment").remove();
              }
            });
        });
        $("#btnCancel").off("click").on("click", function () {
            $("#confirmShipment").dialog('close');
        });
        this.doRemove = true;
    }

    configureButtons() {
        this.doRemove = false;
        let self = this;
        $("#btnSubmit").on("click", function () {
            let form = $("#acknowledgeShipmentForm")[0] as HTMLFormElement
            if (self.smvApp.formValidate(form)) self.showConfirmDialog()
        })
        $("#btnMainMenu").on("click", function () {
            $("#confirmShipment").remove();// need to remove dialog so that it is not left on the page when the page is reloaded
                                           //otherwise the dialogs buttons won't work if you open/close the dialog navigate away and return
            self.smvApp.processGet("mobile/shipping");
        });
        $("#btnExit").on("click", function () {
            $("#confirmShipment").remove();
            self.smvApp.processDeassign("/exit/shipping");
        });
    }
}
