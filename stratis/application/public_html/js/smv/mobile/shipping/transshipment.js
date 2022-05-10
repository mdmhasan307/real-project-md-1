var Transshipment = (function () {
    function Transshipment(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    Transshipment.prototype.configureButtons = function () {
        var self = this;
        $('#btnSubmit').on('click', function () {
            var form = $('#transshipForm')[0];
            self.smvApp.processPageSubmission(self.submitPath, $('#transshipForm').serialize(), form);
        });
        $('#btnMainMenu').on('click', function () { self.smvApp.processGet('mobile/shipping/transshipment'); });
        $('#btnExit').on('click', function () { self.smvApp.processDeassign('/exit/shipping'); });
    };
    return Transshipment;
}());
