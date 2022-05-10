class ReviewLocationBarcode {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $("#btnSubmit").on("click", function () {
            let form = $("#reviewLocationForm")[0] as HTMLFormElement;
            self.smvApp.processPageSubmission(self.submitPath + "/handle", $("#reviewLocationForm").serialize(), form)
                .then((result:any) => {
                    $("#overrideCommand").val("");
                    if (result.responseFlags.indexOf("overrideAAC") !== -1) {
                        //reset the override button and hidden value in case its no longer a override.
                        let yesButton = $("#btnOverrideAAC").unbind("click");
                        let noButton = $("#btnDontOverrideAAC").unbind("click");
                        yesButton.on("click", self.callOverride("overrideAAC")).show();
                        noButton.on("click", self.callOverride("overrideCommand")).show();
                        self.disableSubmit();
                    }
                    if (result.responseFlags.indexOf("overrideLoc") !== -1) {
                        //reset the override button and hidden value in case its no longer a override.
                        let yesButton = $("#btnOverrideLoc").unbind("click");
                        let noButton = $("#btnDontOverrideLoc").unbind("click");
                        yesButton.on("click", self.callOverride("overrideLoc")).show();
                        noButton.on("click", self.callOverride("dontOverrideLoc")).show();
                        self.disableSubmit();
                    }

                    result.responseFlags.forEach((f:string) => {
                       if (f.indexOf("aac:") !== -1) {
                           $("#aac").val(f.substr(f.indexOf(":")+1));
                       } else if (f.indexOf("scn:") !== -1) {
                           $("#scn").val(f.substr(f.indexOf(":")+1));
                       } else if (f.indexOf("shippingManifestId:") !== -1) {
                           $("#shippingManifestId").val(f.substr(f.indexOf(":")+1));
                       } else if (f.indexOf("shippingId:") !== -1) {
                           $("#shippingId").val(f.substr(f.indexOf(":")+1));
                       } else if (f.indexOf("floorLocation:") !== -1) {
                           $("#floorLocation").val(f.substr(f.indexOf(":")+1));
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
        //default hide override buttons
        $("#btnOverrideAAC").hide();
        $("#btnDontOverrideAAC").hide();
        $("#btnOverrideLoc").hide();
        $("#btnDontOverrideLoc").hide();
    }

    callOverride(override:string) {
        //set that there was an override and process as an override
        let self = this;
        return function() {
            let form = $("#reviewLocationForm")[0] as HTMLFormElement;
            $("#overrideCommand").val(override);
            self.smvApp.processPageSubmission(self.submitPath + "/override", $("#reviewLocationForm").serialize(), form).then(self.resetOverride);
        }
    }

    resetOverride(result:any) {
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
    }

    disableSubmit() {
        $("#btnSubmit").hide();
        $("#location").prop("disabled", true);
        $("#barcode").prop("disabled", true);
    }
}
