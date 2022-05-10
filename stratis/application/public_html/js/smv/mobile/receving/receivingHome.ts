class ReceivingHome {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.submitPath = formPath;
        this.smvApp = smvApp;
    }

    configureButtons() {
        const self = this
        $("#btnSubmit").on("click", function () {
            let form = $("#receivingHomeForm")[0] as HTMLFormElement;
            self.smvApp.processPageSubmission("mobile/receiving", $("#receivingHomeForm").serialize(), form);
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/receiving");
        });
        $("#chkPartialShipment").on("click", function () {
            $("#btnPartialShipment").toggleClass("active");
        });
    }
}
