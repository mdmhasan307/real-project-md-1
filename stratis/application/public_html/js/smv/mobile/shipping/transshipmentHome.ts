class TransshipmentHome {
  private submitPath
  private smvApp

  constructor(smvApp, formPath) {
    this.smvApp = smvApp
    this.submitPath = formPath
  }

  configureButtons() {
    let self = this
    $('#btnTcn').on('click', function () { self.smvApp.processGet('/mobile/shipping/transshipment/tcn') })
    $('#btnDocument').on('click', function () { self.smvApp.processGet('/mobile/shipping/transshipment/document') })
    $('#btnContract').on('click', function () { self.smvApp.processGet('/mobile/shipping/transshipment/contract') })
    $('#btnMainMenu').on('click', function () { self.smvApp.processGet('mobile/shipping') })
    $('#btnExit').on('click', function () { self.smvApp.processDeassign('/exit/shipping') })
  }
}
