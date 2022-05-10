class ReceivingControlledNiinDetails {
  private submitPath;
  private smvApp;

  constructor(smvApp, formPath) {
    this.smvApp = smvApp;
    this.submitPath = formPath;
  }

  configureButtons() {
    let self = this;

    $("#btnSubmit").on("click", function () {
      let form = $("#receivingControlledNiinDetailsForm")[0] as HTMLFormElement;
      let qtyReceived = parseInt($("#qtyReceived").val() as string);
      let serialCount = $("#serials option").length;
      if (serialCount != qtyReceived) {
        let warningMessages = [{
          message: qtyReceived + " Serial Number(s) must be entered to submit",
          type: "NOTIFICATION"
        }];
        self.smvApp.setMessages(warningMessages, false);
        self.smvApp.openNotifications();
      }
      else {
        $('#serials option').prop('selected', true);
        self.smvApp.processPageSubmission(self.submitPath, $("#receivingControlledNiinDetailsForm").serialize(), form);
      }
    });

    $("#btnMainMenu").on("click", function () {
      self.smvApp.processDeassign("/main/receiving");
    });
    $("#btnExit").on("click", function () {
      self.smvApp.processDeassign("/exit/receiving");
    });

    $("#btnAdd").on("click", function () {
      let serial = $("#serial").val() as string;
      serial = self.smvApp.escapeIllegalInputCharacters(serial);

      let qtyReceived = parseInt($("#qtyReceived").val() as string);
      let serialCount = $("#serials option").length;
      if (serialCount == qtyReceived) {
        let warningMessages = [{
          message: "Already entered " + qtyReceived + " Serial Number(s)",
          type: "NOTIFICATION"
        }];
        self.smvApp.setMessages(warningMessages, false);
        self.smvApp.openNotifications();
        $("#serial").val("");
      }
      else {
        if (serial != null && serial.trim().length > 0) {
          if ($("#serials option[value='" + serial + "']").length == 0) {
            let formWithOption = self.submitPath + "/addSerial";
            let form = $("#receivingControlledNiinDetailsForm")[0] as HTMLFormElement;
            self.smvApp.processPageSubmission(formWithOption, $("#receivingControlledNiinDetailsForm").serialize()).then((response: any) => {
              if (response.result === "SUCCESS") {
                //this will open the notifications if a warning only message was returned.
                self.smvApp.openNotifications();
                self.smvApp.additemToList(serial, $('#serials'), $("#serialListGroup"), true);
              }
              $("#serial").val("");
            });
          }
          else {
            let warningMessages = [{
              message: "Serial Number was a duplicate of one already entered",
              type: "NOTIFICATION"
            }];
            self.smvApp.setMessages(warningMessages, false);
            self.smvApp.openNotifications();
            $("#serial").val("");
          }
        }
        else {
          let warningMessages = [{
            message: "Serial Number must be entered.",
            type: "NOTIFICATION"
          }];
          self.smvApp.setMessages(warningMessages, false);
          self.smvApp.openNotifications();
        }
      }

    });
  }
}