class LocationSurveyDetail {
  private submitPath
  private smvApp

  constructor(smvApp, formPath) {
    this.smvApp = smvApp
    this.submitPath = formPath
  }

  configureButtons() {
    let self = this

    $('#btnSubmit').on('click', function () {
      let form = $('#locationSurveyDetailsForm')[0] as HTMLFormElement
      self.smvApp.processPageSubmission(self.submitPath, $('#locationSurveyDetailsForm').serialize(), form)
    })
    $('#btnRemove').on('click', function () {
      let path = self.submitPath + '/remove'
      self.smvApp.processPageSubmission(path, $('#locationSurveyDetailsForm').serialize())
    })
    $('#btnBypass').on('click', function () {
      self.smvApp.processPageSubmission(self.submitPath + '/bypass')
    })
    $('#btnExit').on('click', function () {
      self.smvApp.processPageSubmission(self.submitPath + '/exit')
    })
  }
}
