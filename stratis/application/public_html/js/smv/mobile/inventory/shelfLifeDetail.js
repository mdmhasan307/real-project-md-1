var ShelfLifeDetail = (function () {
    function ShelfLifeDetail(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    ShelfLifeDetail.prototype.configureButtons = function () {
        var self = this;
        $("#btnConfirm").on("click", function () {
            var formWithOption = self.submitPath + "/detail/confirm";
            self.smvApp.processPageSubmission(formWithOption, $("#shelfLifeForm").serialize());
        });
        $("#btnCannotExtend").on("click", function () {
            var formWithOption = self.submitPath + "/detail/noextend";
            self.smvApp.processPageSubmission(formWithOption, $("#shelfLifeForm").serialize());
        });
        $("#btnSkip").on("click", function () {
            var formWithOption = self.submitPath + "/detail/skip";
            self.smvApp.processPageSubmission(formWithOption, $("#shelfLifeForm").serialize());
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/inventory");
        });
    };
    return ShelfLifeDetail;
}());
