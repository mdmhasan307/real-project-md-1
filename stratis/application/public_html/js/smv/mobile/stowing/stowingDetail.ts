class StowingDetails {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.submitPath = formPath;
        this.smvApp = smvApp;
    }

    configureButtons() {
        let self = this;
        let loss = $("#stowLoss");
        $("#btnSubmit").on("click", function () {
            self.destroyLocationDialog();
            self.smvApp.processPageSubmission("mobile/stowing/detail/submit", $("#stowDetailsForm").serialize())
                .then((result: any) => {
                  if (result.result === "VALIDATION_WARNINGS") {
                      //reset the loss button and hidden value in case its no longer a loss.
                      $("#btnLoss").unbind("click");
                      $("#btnLoss").hide();
                      loss.val("");
                      if (result.responseFlags.indexOf("stowLoss") != -1) {
                          $("#btnLoss").on("click", function () {
                              //set that there was a loss and process as a loss
                              loss.val("loss");
                              self.smvApp.processPageSubmission("mobile/stowing/detail/submit", $("#stowDetailsForm").serialize());
                          }).show();
                      }
                      if (result.responseFlags.indexOf("locationMisMatch") != -1) {
                          self.showLocationNotificationDialog();
                      }
                  }
                });
        });
        $("#btnBypass").on("click", function () {
            self.smvApp.processPageSubmission("mobile/stowing/detail/bypass", $("#stowDetailsForm").serialize());
            self.destroyLocationDialog();
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processDeassign("/main/stowing");
            self.destroyLocationDialog();
        });

        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/stowing");
            self.destroyLocationDialog();
        });

        //default hide stow button
        $("#btnLoss").hide();
    }

    configureActions() {
        $(document).ready(() => {
        });
    }

    showLocationNotificationDialog() {
        let self = this;
        $("#stowRelocate").val("");
        $("#enteredLoc").text($("#location").val() as string);
        $("#stowLoc").text($("#loc").val() as string);
        $("#newdLoc").text($("#location").val() as string);

        $("#locationNotification").dialog({
            autoOpen: true,
            classes: {
                "ui-dialog": "smv-dialog",
                "ui-dialog-content": "smv-dialog-content",
                "ui-dialog-buttonpane": "smv-dialog-buttonpane"
            },
            modal: true,
            resizable: true,
            draggable: true,
            title: 'Relocate'
        });
        $("#btnYes").off("click").on("click", function () {
            $("#locationNotification").dialog('close');
            $("#stowRelocate").val("relocate");
            let promise = self.smvApp.processPageSubmission("mobile/stowing/detail/submit",
                $("#stowDetailsForm").serialize());
            promise.then((response:any) => {
                if (response.result === "SUCCESS" && response.redirectUrl != null) {
                    // going to get the page again remove dialog so that it is not left on the page when the page is reloaded
                    // otherwise the dialogs buttons won't work if you open/close the dialog navigate away and return
                    self.destroyLocationDialog();
                }
            });
            $("#stowRelocate").val("");
        });
        $("#btnNo").off("click").on("click", function () {
            $("#locationNotification").dialog('close');
        });
    }

    destroyLocationDialog() {
        if ($("#locationNotification").hasClass("ui-dialog-content")) {
            $("#locationNotification").dialog('destroy');
        }
    }
}
