var ReceivingDetail = (function () {
    function ReceivingDetail(smvApp, formPath) {
        this.submitPath = formPath;
        this.smvApp = smvApp;
    }
    ReceivingDetail.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#receivingDetailForm")[0];
            self.smvApp.processPageSubmission("mobile/receiving/detail", $("#receivingDetailForm").serialize(), form).then(function (result) {
                if (result.responseFlags.indexOf("dasfQty") != -1) {
                    self.showDASFDialog();
                }
            });
        });
        $("#btnMainMenu").on("click", function () { self.smvApp.processDeassign("/main/receiving"); });
        $("#btnExit").on("click", function () { self.smvApp.processDeassign("/exit/receiving"); });
    };
    ReceivingDetail.prototype.showDASFDialog = function () {
        var self = this;
        $("#dasfQtySelection").dialog({
            autoOpen: true,
            classes: {
                "ui-dialog": "smv-dialog",
                "ui-dialog-content": "smv-dialog-content",
                "ui-dialog-buttonpane": "smv-dialog-buttonpane"
            },
            modal: true,
            resizable: true,
            draggable: true,
            title: 'DASF Quantity Overage'
        });
        $("#btnGenerate").off("click").on("click", function () {
            $("#dasfQtySelection").dialog('close');
            $("#dasfQtyOverride").val("Override");
            var form = $("#receivingDetailForm")[0];
            self.smvApp.processPageSubmission("mobile/receiving/detail", $("#receivingDetailForm").serialize(), form);
        });
        $("#btnDontGenerate").off("click").on("click", function () {
            $("#dasfQtySelection").dialog('close');
        });
    };
    return ReceivingDetail;
}());
