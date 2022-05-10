function customHandler(downloadId){
    var exportCmd = AdfPage.PAGE.findComponentByAbsoluteId(downloadId);
    var actionEvent = new AdfActionEvent(exportCmd);
    actionEvent.forceFullSubmit();
    actionEvent.noResponseExpected();
    actionEvent.queue();
}