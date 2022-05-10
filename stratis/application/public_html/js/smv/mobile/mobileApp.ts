class MobileApp {
    private mobileAppContent;
    private pageTitle;
    private pageTitleFooter;
    private rootUrl: string;
    private fullUrl: string;
    private notificationDetailsPopup;
    private notificationsTemplate;
    private notificationsIcon;
    private notificationsDialog;
    private notifications;
    private nullableProp: string | null;
    private fullScreenEnabled;
    private deassignPath;
    private devProfileActive = false;

    constructor(ctxPath) {
        let self = this;

        self.mobileAppContent = $("#mobileAppContent");
        self.pageTitle = $("#pageTitle");
        self.pageTitleFooter = $("#pageTitleFooter");
        self.rootUrl = ctxPath;
        self.fullUrl = self.rootUrl + "/app/";
        self.notificationDetailsPopup = $("#notificationsDetailsPopup");
        // @ts-ignore
        self.notificationsTemplate = Handlebars.compile($("#notifications-template").html());
        self.notificationsIcon = $("#notificationsIcon");
        self.notificationsDialog = self.notificationDetailsPopup.dialog({
            autoOpen: false,
            classes: {
                "ui-dialog": "smv-dialog",
                "ui-dialog-content": "smv-dialog-content",
                "ui-dialog-buttonpane": "smv-dialog-buttonpane"
            },
            modal: true,
            resizable: false,
            draggable: false,
            title: 'Notifications'
        });
        self.notifications = [];

        self.nullableProp = null;
        self.fullScreenEnabled = false;

        self.deassignPath = self.fullUrl + "mobile/deassign";
    }

    openNotifications() {
        let self = this;
        if (self.notifications.length > 0) {
            self.notificationsDialog.dialog('open');
        }
    }

    showLoader() {
        $('.loading-overlay').show();
        $('.loading-overlay-image-container').show();
    }

    hideLoader() {
        $('.loading-overlay').hide();
        $('.loading-overlay-image-container').hide();
    }

    flagNotifications(showFlag) {
        if (showFlag)
            $("#notificationWrapper").addClass("notificationsUnread");
        else
            $("#notificationWrapper").removeClass("notificationsUnread");
    }

    processGet(pageToLoad, appendMessages = false) {
        let self = this;
        self.showLoader();
        let pageToLoadUrl = self.fullUrl + pageToLoad;

        $.ajax({
            url: pageToLoadUrl,
            success: function (response) {
                self.hideLoader();
                self.processMessages(response, appendMessages);
                self.devProfileActive = response.devProfileActive;

                if (response.result === "SESSION_INVALID") {
                    self.handleSessionInvalid();
                } else if (response.result === "SUCCESS") {
                    if (response.responseBody) {
                        self.pageTitle.text(response.pageTitle);
                        self.pageTitleFooter.text(response.pageTitle);
                        self.mobileAppContent.html(response.responseBody);
                    }
                    self.openNotifications();
                } else if (response.result === "EXCEPTION") {
                    self.handleException();
                } else if (response.result === "REDIRECT_HOME") {
                    self.handleRedirectHome();
                }
            }
        });
    }

    processDeassign(path) {
        let self = this;
        self.processPageSubmission(self.deassignPath + path, null, null);
    }

    processPageSubmission(pageToProcess, submissionData, theForm) {
        let self = this;
        //if the form is passed in, run validation.  if failed, return without processing.
        if (theForm != null) {
            if (!self.formValidate(theForm)) return;
        }
        self.showLoader();

        return new Promise<any>(
            (resolve, reject) => {
                $.ajax({
                    url: pageToProcess,
                    type: 'POST',
                    data: submissionData,
                    success: function (response) {
                        self.hideLoader();
                        // @ts-ignore
                        self.processMessages(response);
                        self.devProfileActive = response.devProfileActive;

                        if (response.result === "SESSION_INVALID") {
                            self.handleSessionInvalid();
                        } else if (response.result === "SUCCESS") {
                            if (response.redirectUrl != null) {
                                self.processGet(response.redirectUrl, true);
                            }
                            if (response.userRole != null) {
                                $("#roleName").html(response.userRole);
                            }
                            if (response.userWorkstation != null) {
                                $("#workstationName").html(response.userWorkstation);
                            }
                        } else if (response.result === "VALIDATION_WARNINGS") {
                            self.handleValidationErrors();
                        } else if (response.result === "EXCEPTION") {
                            self.handleException();
                        } else if (response.result === "REDIRECT_HOME") {
                            self.handleRedirectHome();
                        }
                        resolve(response);
                    }
                });
            }
        );
    }

    processAjaxSubmission(pageToProcess, submissionData, theForm) {
        let self = this;
        //if the form is passed in, run validation.  if failed, return without processing.
        if (theForm != null) {
            if (!self.formValidate(theForm)) return;
        }
        self.showLoader();

        return new Promise<any>(
            (resolve, reject) => {
                $.ajax({
                    url: pageToProcess,
                    type: 'POST',
                    data: submissionData,
                    success: function (response) {
                        self.hideLoader();
                        resolve(response);
                    }
                });
            }
        );
    }

    formValidate(theForm) {
        if (!theForm.checkValidity()) {
            theForm.reportValidity();
            return false;
        }
        return true;
    }

    processMessages(response, appendMessages) {
        let devProfileActive = response.devProfileActive;
        this.setMessages(response.messages, appendMessages, devProfileActive);
    }

    isDevProfileActive() {
        let self = this;
        return this.devProfileActive;
    }

    setMessages(messages, appendMessages, devProfileActive = false) {
        if (appendMessages) this.notifications = this.notifications.concat(messages);
        else this.notifications = messages;
        this.flagNotifications(this.notifications != undefined && this.notifications.length > 0);

        if (devProfileActive === undefined) devProfileActive = false;

        this.notificationDetailsPopup.html(this.notificationsTemplate({
            "messages": this.notifications,
            "devProfileActive": devProfileActive
        }));
        this.notificationsDialog.scrollTop(0);
    }


    handleSessionInvalid() {
        let self = this;
        self.openNotifications();
        self.notificationsDialog.on('dialogclose', function (event) {
            location.replace(self.rootUrl);
        });
    }

    handleRedirectHome() {
        let self = this;
        self.openNotifications();
        self.notificationsDialog.on('dialogclose', function (event) {
            self.processGet("mobile/home");
        });
    }

    handleValidationErrors() {
        let self = this;
        self.openNotifications();
    }

    handleException() {
        let self = this;
        self.openNotifications();
    }

    openFullscreen() {
        let self = this;
        const docElm = document.body as HTMLElement & {
            //constant is purely a typescript intellisense error avoider.  Not required to compile or run.
            mozRequestFullScreen(): Promise<void>;
            webkitRequestFullScreen(): Promise<void>;
            msRequestFullscreen(): Promise<void>;
        };

        if (docElm.requestFullscreen) {
            docElm.requestFullscreen();
        } else if (docElm.mozRequestFullScreen) {
            docElm.mozRequestFullScreen();
        } else if (docElm.webkitRequestFullScreen) {
            docElm.webkitRequestFullScreen();
        } else if (docElm.msRequestFullscreen) {
            docElm.msRequestFullscreen();
        }
        self.fullScreenEnabled = true;
    }

    closeFullscreen() {
        let self = this;
        const docElm = document as Document & {
            //constant is purely a typescript intellisense error avoider.  Not required to compile or run.
            mozCancelFullScreen(): Promise<void>;
            webkitExitFullscreen(): Promise<void>;
            msExitFullscreen(): Promise<void>;
            exitFullscreen(): Promise<void>;
        };
        if (docElm.exitFullscreen) {
            docElm.exitFullscreen();
        } else if (docElm.mozCancelFullScreen) { /* Firefox */
            docElm.mozCancelFullScreen();
        } else if (docElm.webkitExitFullscreen) { /* Chrome, Safari and Opera */
            docElm.webkitExitFullscreen();
        } else if (docElm.msExitFullscreen) { /* IE/Edge */
            docElm.msExitFullscreen();
        }
        self.fullScreenEnabled = false;
    }

    toggleFullScreen() {
        let self = this;
        if (self.fullScreenEnabled) {
            self.closeFullscreen();
        } else {
            self.openFullscreen();
        }

    }

    goFullScreen() {
        let self = this;
        $("#fullScreenSelection").dialog({
            autoOpen: true,
            classes: {
                "ui-dialog": "smv-dialog",
                "ui-dialog-content": "smv-dialog-content",
                "ui-dialog-buttonpane": "smv-dialog-buttonpane"
            },
            modal: true,
            resizable: true,
            draggable: true,
            title: 'Fullscreen Mode'
        });

        $("#btnFullScreen").on("click", function () {
            self.openFullscreen();
            $("#fullScreenSelection").dialog('close');
        });
        $("#btnFullScreenClose").on("click", function () {
            $("#fullScreenSelection").dialog('close');
        });
    }

    //used for a hidden multiselect list, and ul list-group display.
    //Add an item to the hidden multi select: selectList
    //Display the item in the list-group ul: ulList.
    //removable of true will add an "X" to allow them to remove.
    additemToList(serial, selectList, ulList, removable, additionalProcessingForRemove) {
        let self = this;

        selectList.append('<option value="' + serial + '" selected>' + serial + '</option>');

        let li = $('<li>').attr('class', 'list-group-item custom');
        let sp = $('<span>').attr('class', 'float-left lgi-custom-left');
        sp.append(serial);
        li.append(sp);

        if (removable) {
            let bt = $('<button>').attr('type', 'button').attr('class', 'float-right remove-item lgi-custom-right btn-primary');
            let bti = $('<i>').attr('class', 'fas fa-times fa-2x');

            bt.on('click', function () {
                self.removeItemFromList(serial, selectList, additionalProcessingForRemove);
                $(this).parent().remove();
            })
            bt.append(bti);
            li.append(bt);
        }
        ulList.append(li);

    }

    removeItemFromList(serial, list, additionalProcessingForRemove) {
        list.children("option[value=" + serial + "]").remove();
        if (additionalProcessingForRemove) {
            additionalProcessingForRemove();
        }
    }

    escapeIllegalInputCharacters(rawString) {
        if (rawString == null)
            return rawString;
        return rawString.replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

}