class StowingHome {
    private submitPath
    private smvApp

    constructor(smvApp, formPath) {
        this.submitPath = formPath
        this.smvApp = smvApp
    }

    configureButtons() {
        let self = this
        let hasSids = $('#sids option').length > 0


        $('#btnSubmit').prop('disabled', !hasSids)
        $('#btnSubmit').on("click", function () {
            self.smvApp.processGet('mobile/stowing/detail')
        });

        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/stowing");
        });

        $('#btnAdd').on("click", function () {
            let form = $("#stowingHomeForm")[0] as HTMLFormElement;
            self.smvApp.processPageSubmission(self.submitPath, $('#stowingHomeForm').serialize(), form)
            $('#sidText').val('')
        })

    }
}
