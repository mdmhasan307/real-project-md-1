var TestPrintMenu = (function () {
    function TestPrintMenu(smvApp) {
        this.smvApp = smvApp;
    }
    TestPrintMenu.prototype.configureButtons = function () {
        var self = this;
        $("#btnTestPrint").on("click", function () {
            self.smvApp.processGet("mobile/testprint");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processGet("mobile/home");
        });
    };
    return TestPrintMenu;
}());
