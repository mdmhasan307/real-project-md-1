var ReceivingDetailLocation = (function () {
    function ReceivingDetailLocation(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    ReceivingDetailLocation.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#stowDetailLocationForm")[0];
            self.smvApp.processPageSubmission(self.submitPath, $("#stowDetailLocationForm").serialize(), form);
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/receiving");
        });
        $("#wac").change(function () {
            $("#location")
                .find('option')
                .remove();
            $('#location').append($("<option></option>")
                .attr("value", "")
                .text("Select a Location"));
            self.smvApp.processAjaxSubmission(self.submitPath + "/list", $("#stowDetailLocationForm").serialize(), null)
                .then(function (result) {
                if (result.error) {
                    var validationMessages = [{
                            message: result.errorMessage,
                            type: "VALIDATION_WARNING"
                        }];
                    self.smvApp.setMessages(validationMessages, false);
                    self.smvApp.openNotifications();
                }
                else {
                    $.each(result.list, function (key, value) {
                        $('#location')
                            .append($("<option></option>")
                            .attr("value", value.locationId)
                            .text(value.locationOption));
                    });
                }
            });
        });
    };
    ReceivingDetailLocation.prototype.configureSerials = function (serials) {
        var self = this;
        serials.forEach(function (serial) {
            return self.smvApp.additemToList(serial, $('#serials'), $("#serialListGroup"), false);
        });
    };
    return ReceivingDetailLocation;
}());
