var SwitchWorkstation = (function () {
    function SwitchWorkstation(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    SwitchWorkstation.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var isDisabled = $("#userTypeId").is(':disabled');
            var data;
            if (isDisabled) {
                var serializedUsertypeId = "&userTypeId=" + $("#userTypeId").val();
                data = $("#switchWorkstationForm").serialize() + serializedUsertypeId;
            }
            else {
                data = $("#switchWorkstationForm").serialize();
            }
            self.smvApp.processPageSubmission(self.submitPath, data);
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processGet("mobile/home");
        });
    };
    SwitchWorkstation.prototype.configureUserTypeChangeProcessing = function () {
        $("#userTypeId").change(function () {
            $("#userTypeName").val($("#userTypeId option:selected").text());
        });
    };
    return SwitchWorkstation;
}());
