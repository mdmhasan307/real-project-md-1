var StowingHome = (function () {
    function StowingHome(smvApp, formPath) {
        this.submitPath = formPath;
        this.smvApp = smvApp;
    }
    StowingHome.prototype.configureButtons = function () {
        var self = this;
        var hasSids = $('#sids option').length > 0;
        $('#btnSubmit').prop('disabled', !hasSids);
        $('#btnSubmit').on("click", function () {
            self.smvApp.processGet('mobile/stowing/detail');
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/stowing");
        });
        $('#btnAdd').on("click", function () {
            var form = $("#stowingHomeForm")[0];
            self.smvApp.processPageSubmission(self.submitPath, $('#stowingHomeForm').serialize(), form);
            $('#sidText').val('');
        });
    };
    return StowingHome;
}());
