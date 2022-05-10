var PickingByNormal = (function () {
    function PickingByNormal(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    PickingByNormal.prototype.processForm = function () {
        var self = this;
        self.smvApp.processPageSubmission(self.submitPath, $("#pickByNormalForm").serialize());
    };
    return PickingByNormal;
}());
