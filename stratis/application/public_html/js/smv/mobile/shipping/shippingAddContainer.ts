class ShippingAddContainer {
    private submitPath;
    private smvApp;

    constructor(smvApp, formPath) {
        this.smvApp = smvApp;
        this.submitPath = formPath;
    }

    configureButtons() {
        let self = this;
        $("#btnSubmit").on("click", function () {
            let form = $("#addContainerForm")[0] as HTMLFormElement;
            self.smvApp.processPageSubmission(self.submitPath, $("#addContainerForm").serialize(), form)
            .then((result:any) => {
                if (result.responseFlags.indexOf("recommend") != -1){
                  //call to recommend a location
                    self.smvApp.processAjaxSubmission(self.submitPath+"/recommend", $("#addContainerForm").serialize(), form)
                        .then((result:any) => {
                            $("#recommend").val(result.location);
                            $("#stageLoc").val(result.location).prop("required",true);
                            $("#requireLocation").show();
                        })
                 }
            });
        });
        $("#btnMainMenu").on("click", function () {
            self.smvApp.processGet("mobile/shipping");
        });
        $("#btnExit").on("click", function () {
            self.smvApp.processDeassign("/exit/shipping");
        });
        $("#requireLocation").hide();
    }
}
