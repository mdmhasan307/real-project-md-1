var ReviewLocationBarcode = (function () {
    function ReviewLocationBarcode(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }
    ReviewLocationBarcode.prototype.configureButtons = function () {
        var self = this;
        $("#btnSubmit").on("click", function () {
            var form = $("#reviewLocationForm")[0];
            self.smvApp.processPageSubmission(self.submitPath + "/handle", $("#reviewLocationForm").serialize(), form)
                .then(function (result) {
                $("#overrideCommand").val("");
                if (result.responseFlags.indexOf("overrideAAC") !== -1) {
                    var yesButton = $("#btnOverrideAAC").unbind("click");
                    var noButton = $("#btnDontOverrideAAC").unbind("click");
                    yesButton.on("click", self.callOverride("overrideAAC")).show();
                    noButton.on("click", self.callOverride("overrideCommand")).show();
                    self.disableSubmit();
                }
                if (result.responseFlags.indexOf("overrideLoc") !== -1) {
                    var yesButton = $("#btnOverrideLoc").unbind("click");
                    var noButton = $("#btnDontOverrideLoc").unbind("click");
                    yesButton.on("click", self.callOverride("overrideLoc")).show();
                    noButton.on("click", self.callOverride("dontOverrideLoc")).show();
                    self.disableSubmit();
                }
                result.responseFlags.forEach(function (f) {
                    if (f.indexOf("aac:") !== -1) {
                        $("#aac").val(f.substr(f.indexOf(":") + 1));
                    }
                    else if (f.indexOf("scn:") !== -1) {
                        $("#scn").val(f.substr(f.indexOf(":") + 1));
                    }
                    else if (f.indexOf("shippingManifestId:") !== -1) {
                        $("#shippingManifestId").val(f.substr(f.indexOf(":") + 1));
                    }
                    else if (f.indexOf("shippingId:") !== -1) {
                        $("#shippingId").val(f.substr(f.indexOf(":") + 1));
                    }
                    else if (f.indexOf("floorLocation:") !== -1) {
                        $("#floorLocation").val(f.substr(f.indexOf(":") + 1));
                    }
                });
            });
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processGet("mobile/shipping/reviewlocation");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/shipping");
        });
        $("#btnOverrideAAC").hide();
        $("#btnDontOverrideAAC").hide();
        $("#btnOverrideLoc").hide();
        $("#btnDontOverrideLoc").hide();
    };
    ReviewLocationBarcode.prototype.callOverride = function (override) {
        var self = this;
        return function () {
            var form = $("#reviewLocationForm")[0];
            $("#overrideCommand").val(override);
            self.smvApp.processPageSubmission(self.submitPath + "/override", $("#reviewLocationForm").serialize(), form).then(self.resetOverride);
        };
    };
    ReviewLocationBarcode.prototype.resetOverride = function (result) {
        if (result.responseFlags.indexOf("resetOverride") !== -1) {
            $("#btnOverrideAAC").hide();
            $("#btnDontOverrideAAC").hide();
            $("#btnOverrideLoc").hide();
            $("#btnDontOverrideLoc").hide();
            $("#aac").val("");
            $("#scn").val("");
            $("#shippingManifestId").val("");
            $("#shippingId").val("");
            $("#floorLocation").val("");
            $("#btnSubmit").show();
            $("#location").prop("disabled", false);
            $("#barcode").prop("disabled", false);
        }
    };
    ReviewLocationBarcode.prototype.disableSubmit = function () {
        $("#btnSubmit").hide();
        $("#location").prop("disabled", true);
        $("#barcode").prop("disabled", true);
    };
    return ReviewLocationBarcode;
}());
