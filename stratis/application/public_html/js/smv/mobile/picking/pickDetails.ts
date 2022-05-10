class PickingDetails {
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
            self.smvApp.processPageSubmission(self.submitPath, $("#pickForm").serialize(), form).then((result: any) => {
                if (result.responseFlags.indexOf("PartialPickAttempted") != -1)
                    $("#partialPickAttempted").val("true");
            });
        });
        $(".btnBypassId").on("click", function () {
            let formWithOption = self.submitPath + "/bypass";
            self.smvApp.processPageSubmission(formWithOption, $("#pickForm").serialize(), null);
        });
        $(".btnRefuseId").on("click", function () {
            let formWithOption = self.submitPath + "/refuse";
            self.smvApp.processPageSubmission(formWithOption, $("#pickForm").serialize(), null);
        });
        $(".btnMainMenuId").on("click", function () {
            self.smvApp.processDeassign("/main/picking");
        });
        $(".btnExitId").on("click", function () {
            self.smvApp.processDeassign("/exit/picking");
        });
    }
}
