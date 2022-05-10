class ReceivingDetailLocation {
  private submitPath;
  private smvApp;

  constructor(smvApp, formPath) {
    this.smvApp = smvApp;
    this.submitPath = formPath;
  }

  configureButtons() {
    let self = this;
    $("#btnSubmit").on("click", function () {
      let form = $("#stowDetailLocationForm")[0] as HTMLFormElement;
      self.smvApp.processPageSubmission(self.submitPath, $("#stowDetailLocationForm").serialize(), form);
    });
    $("#btnExit").on("click", function () {
      self.smvApp.processDeassign("/exit/receiving");
    });
    $("#wac").change(function() {
      $("#location")
          .find('option')
          .remove();
      $('#location').append($("<option></option>")
          .attr("value", "")
          .text("Select a Location"))
      self.smvApp.processAjaxSubmission(self.submitPath+"/list", $("#stowDetailLocationForm").serialize(), null)
          .then((result:any) => {
            if (result.error) {
              let validationMessages = [{
                message: result.errorMessage,
                type: "VALIDATION_WARNING"
              }];
              self.smvApp.setMessages(validationMessages, false);
              self.smvApp.openNotifications();
            } else {
              $.each(result.list, function(key, value) {
                $('#location')
                    .append($("<option></option>")
                        .attr("value", value.locationId)
                        .text(value.locationOption));
              });
            }
          });
    })

  }

  configureSerials(serials) {
    let self = this;
    serials.forEach(serial =>
        self.smvApp.additemToList(serial, $('#serials'), $("#serialListGroup"), false));
  }

}
