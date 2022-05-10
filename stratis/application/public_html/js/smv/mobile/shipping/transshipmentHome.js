var TransshipmentHome = (function () {
    function TransshipmentHome(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    TransshipmentHome.prototype.configureButtons = function () {
        var self = this;
        $('#btnTcn').on('click', function () { self.smvApp.processGet('/mobile/shipping/transshipment/tcn'); });
        $('#btnDocument').on('click', function () { self.smvApp.processGet('/mobile/shipping/transshipment/document'); });
        $('#btnContract').on('click', function () { self.smvApp.processGet('/mobile/shipping/transshipment/contract'); });
        $('#btnMainMenu').on('click', function () { self.smvApp.processGet('mobile/shipping'); });
        $('#btnExit').on('click', function () { self.smvApp.processDeassign('/exit/shipping'); });
    };
    return TransshipmentHome;
}());
