class ReceivingDetail {
  private submitPath
  private smvApp

  constructor(smvApp, formPath) {
    this.submitPath = formPath
    this.smvApp = smvApp
  }

  configureButtons() {
    let self = this

    $("#btnSubmit").on("click", function () {
      let form = $("#receivingDetailForm")[0] as HTMLFormElement
      self.smvApp.processPageSubmission(
          "mobile/receiving/detail",
          $("#receivingDetailForm").serialize(),
          form).then((result: any) => {
             if (result.responseFlags.indexOf("dasfQty") != -1) {
               self.showDASFDialog();
             }
          });
    })

    $("#btnMainMenu").on("click", function () { self.smvApp.processDeassign("/main/receiving");  })

    $("#btnExit").on("click", function () { self.smvApp.processDeassign("/exit/receiving"); })
  }

  showDASFDialog() {
    let self = this;
    $("#dasfQtySelection").dialog({
      autoOpen: true,
      classes: {
        "ui-dialog": "smv-dialog",
        "ui-dialog-content": "smv-dialog-content",
        "ui-dialog-buttonpane": "smv-dialog-buttonpane"
      },
      modal: true,
      resizable: true,
      draggable: true,
      title: 'DASF Quantity Overage'
    });
    $("#btnGenerate").off("click").on("click", function () {
      $("#dasfQtySelection").dialog('close');
      $("#dasfQtyOverride").val("Override");
      let form = $("#receivingDetailForm")[0] as HTMLFormElement
      self.smvApp.processPageSubmission(
          "mobile/receiving/detail",
          $("#receivingDetailForm").serialize(),
          form)
    });
    $("#btnDontGenerate").off("click").on("click", function () {
      $("#dasfQtySelection").dialog('close');
    });
  }
}
