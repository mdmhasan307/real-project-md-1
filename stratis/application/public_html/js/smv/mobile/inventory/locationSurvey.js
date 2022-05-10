var LocationSurvey = (function () {
    function LocationSurvey(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    LocationSurvey.prototype.configureButtons = function () {
        var self = this;
        $("#btnAdd").on("click", function () {
            var formWithOption = self.submitPath + "/addNiin";
            var form = $("#locationSurveyForm")[0];
            self.smvApp.processPageSubmission(formWithOption, $("#locationSurveyForm").serialize(), form).then(function (response) {
                if (response.result === "SUCCESS") {
                    var niin_1 = $("#niin").val();
                    if (niin_1 != null && niin_1.trim().length > 0) {
                        self.smvApp.additemToList(niin_1, $('#scannedNiins'), $("#niinListGroup"), true, function () {
                            $("#" + niin_1).removeClass("found");
                        });
                    }
                    response.responseFlags.forEach(function (f) {
                        $("#" + f).addClass("found");
                    });
                }
                $("#niin").val("");
            });
        });
        $("#btnSubmit").on("click", function () {
            var form = $("#locationSurveyForm")[0];
            self.smvApp.processPageSubmission(self.submitPath, $("#locationSurveyForm").serialize());
        });
        $("#btnBypass").on("click", function () {
            var formWithOption = self.submitPath + "/bypass";
            self.smvApp.processPageSubmission(formWithOption, $("#locationSurveyForm").serialize());
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processDeassign("/main/locationSurvey");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/locationSurvey");
        });
    };
    return LocationSurvey;
}());
