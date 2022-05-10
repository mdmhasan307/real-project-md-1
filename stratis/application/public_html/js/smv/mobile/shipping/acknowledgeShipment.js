var AcknowledgeShipment = (function () {
    function AcknowledgeShipment(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
        this.doRemove = false;
    }
    AcknowledgeShipment.prototype.showConfirmDialog = function () {
        var self = this;
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
            var form = $("#acknowledgeShipmentForm")[0];
            var promise = self.smvApp.processPageSubmission(self.submitPath + "/handle", $("#acknowledgeShipmentForm").serialize(), form);
            promise.then(function (response) {
                if (response.result === "SUCCESS" && response.redirectUrl != null) {
                    $("#confirmShipment").remove();
                }
            });
        });
        $("#btnCancel").off("click").on("click", function () {
            $("#confirmShipment").dialog('close');
        });
        this.doRemove = true;
    };
    AcknowledgeShipment.prototype.configureButtons = function () {
        this.doRemove = false;
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#acknowledgeShipmentForm")[0];
            if (self.smvApp.formValidate(form))
                self.showConfirmDialog();
        });
        $("#btnMainMenu").on("click", function () {
            $("#confirmShipment").remove();
            self.smvApp.processGet("mobile/shipping");
        });
        $("#btnExit").on("click", function () {
            $("#confirmShipment").remove();
            self.smvApp.processDeassign("/exit/shipping");
        });
    };
    return AcknowledgeShipment;
}());
