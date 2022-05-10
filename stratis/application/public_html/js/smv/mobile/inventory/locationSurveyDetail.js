var LocationSurveyDetail = (function () {
    function LocationSurveyDetail(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    LocationSurveyDetail.prototype.configureButtons = function () {
        var self = this;
        $('#btnSubmit').on('click', function () {
            var form = $('#locationSurveyDetailsForm')[0];
            self.smvApp.processPageSubmission(self.submitPath, $('#locationSurveyDetailsForm').serialize(), form);
        });
        $('#btnRemove').on('click', function () {
            var path = self.submitPath + '/remove';
            self.smvApp.processPageSubmission(path, $('#locationSurveyDetailsForm').serialize());
        });
        $('#btnBypass').on('click', function () {
            self.smvApp.processPageSubmission(self.submitPath + '/bypass');
        });
        $('#btnExit').on('click', function () {
            self.smvApp.processPageSubmission(self.submitPath + '/exit');
        });
    };
    return LocationSurveyDetail;
}());
