class Transshipment {
  private submitPath
  private smvApp

  constructor(smvApp, formPath) {
    this.smvApp = smvApp
    this.submitPath = formPath
  }

  configureButtons() {
    let self = this
    $('#btnSubmit').on('click', function () {
      let form = $('#transshipForm')[0] as HTMLFormElement
      self.smvApp.processPageSubmission(self.submitPath , $('#transshipForm').serialize(), form) })
    $('#btnMainMenu').on('click', function () { self.smvApp.processGet('mobile/shipping/transshipment') })
    $('#btnExit').on('click', function () { self.smvApp.processDeassign('/exit/shipping') })
  }
}
