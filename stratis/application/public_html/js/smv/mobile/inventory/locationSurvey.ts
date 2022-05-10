class LocationSurvey {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        // Buttons from survey screen
        $("#btnAdd").on("click", function () {
            let formWithOption = self.submitPath + "/addNiin";
            let form = $("#locationSurveyForm")[0] as HTMLFormElement;
            self.smvApp.processPageSubmission(formWithOption, $("#locationSurveyForm").serialize(), form).then((response: any) => {
                if (response.result === "SUCCESS") {
                    let niin = $("#niin").val() as string;
                    if (niin != null && niin.trim().length > 0) {

                        self.smvApp.additemToList(niin, $('#scannedNiins'), $("#niinListGroup"), true, function () {
                            $("#" + niin).removeClass("found");
                        });
                    }
                    response.responseFlags.forEach((f) => {
                        $("#" + f).addClass("found");
                    });
                }
                $("#niin").val("");
            });
        });
        $("#btnSubmit").on("click", function () {
            let form = $("#locationSurveyForm")[0] as HTMLFormElement;
            self.smvApp.processPageSubmission(self.submitPath, $("#locationSurveyForm").serialize());
        });
        $("#btnBypass").on("click", function () {
            let formWithOption = self.submitPath + "/bypass";
            self.smvApp.processPageSubmission(formWithOption, $("#locationSurveyForm").serialize());
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processDeassign("/main/locationSurvey");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/locationSurvey");
        });
    }
}
