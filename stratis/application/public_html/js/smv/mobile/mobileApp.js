var MobileApp = (function () {
    function MobileApp(ctxPath) {
        this.devProfileActive = false;
        var self = this;
        self.mobileAppContent = $("#mobileAppContent");
        self.pageTitle = $("#pageTitle");
        self.pageTitleFooter = $("#pageTitleFooter");
        self.rootUrl = ctxPath;
        self.fullUrl = self.rootUrl + "/app/";
        self.notificationDetailsPopup = $("#notificationsDetailsPopup");
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
    MobileApp.prototype.openNotifications = function () {
        var self = this;
        if (self.notifications.length > 0) {
            self.notificationsDialog.dialog('open');
        }
    };
    MobileApp.prototype.showLoader = function () {
        $('.loading-overlay').show();
        $('.loading-overlay-image-container').show();
    };
    MobileApp.prototype.hideLoader = function () {
        $('.loading-overlay').hide();
        $('.loading-overlay-image-container').hide();
    };
    MobileApp.prototype.flagNotifications = function (showFlag) {
        if (showFlag)
            $("#notificationWrapper").addClass("notificationsUnread");
        else
            $("#notificationWrapper").removeClass("notificationsUnread");
    };
    MobileApp.prototype.processGet = function (pageToLoad, appendMessages) {
        if (appendMessages === void 0) { appendMessages = false; }
        var self = this;
        self.showLoader();
        var pageToLoadUrl = self.fullUrl + pageToLoad;
        $.ajax({
            url: pageToLoadUrl,
            success: function (response) {
                self.hideLoader();
                self.processMessages(response, appendMessages);
                self.devProfileActive = response.devProfileActive;
                if (response.result === "SESSION_INVALID") {
                    self.handleSessionInvalid();
                }
                else if (response.result === "SUCCESS") {
                    if (response.responseBody) {
                        self.pageTitle.text(response.pageTitle);
                        self.pageTitleFooter.text(response.pageTitle);
                        self.mobileAppContent.html(response.responseBody);
                    }
                    self.openNotifications();
                }
                else if (response.result === "EXCEPTION") {
                    self.handleException();
                }
                else if (response.result === "REDIRECT_HOME") {
                    self.handleRedirectHome();
                }
            }
        });
    };
    MobileApp.prototype.processDeassign = function (path) {
        var self = this;
        self.processPageSubmission(self.deassignPath + path, null, null);
    };
    MobileApp.prototype.processPageSubmission = function (pageToProcess, submissionData, theForm) {
        var self = this;
        if (theForm != null) {
            if (!self.formValidate(theForm))
                return;
        }
        self.showLoader();
        return new Promise(function (resolve, reject) {
            $.ajax({
                url: pageToProcess,
                type: 'POST',
                data: submissionData,
                success: function (response) {
                    self.hideLoader();
                    self.processMessages(response);
                    self.devProfileActive = response.devProfileActive;
                    if (response.result === "SESSION_INVALID") {
                        self.handleSessionInvalid();
                    }
                    else if (response.result === "SUCCESS") {
                        if (response.redirectUrl != null) {
                            self.processGet(response.redirectUrl, true);
                        }
                        if (response.userRole != null) {
                            $("#roleName").html(response.userRole);
                        }
                        if (response.userWorkstation != null) {
                            $("#workstationName").html(response.userWorkstation);
                        }
                    }
                    else if (response.result === "VALIDATION_WARNINGS") {
                        self.handleValidationErrors();
                    }
                    else if (response.result === "EXCEPTION") {
                        self.handleException();
                    }
                    else if (response.result === "REDIRECT_HOME") {
                        self.handleRedirectHome();
                    }
                    resolve(response);
                }
            });
        });
    };
    MobileApp.prototype.processAjaxSubmission = function (pageToProcess, submissionData, theForm) {
        var self = this;
        if (theForm != null) {
            if (!self.formValidate(theForm))
                return;
        }
        self.showLoader();
        return new Promise(function (resolve, reject) {
            $.ajax({
                url: pageToProcess,
                type: 'POST',
                data: submissionData,
                success: function (response) {
                    self.hideLoader();
                    resolve(response);
                }
            });
        });
    };
    MobileApp.prototype.formValidate = function (theForm) {
        if (!theForm.checkValidity()) {
            theForm.reportValidity();
            return false;
        }
        return true;
    };
    MobileApp.prototype.processMessages = function (response, appendMessages) {
        var devProfileActive = response.devProfileActive;
        this.setMessages(response.messages, appendMessages, devProfileActive);
    };
    MobileApp.prototype.isDevProfileActive = function () {
        var self = this;
        return this.devProfileActive;
    };
    MobileApp.prototype.setMessages = function (messages, appendMessages, devProfileActive) {
        if (devProfileActive === void 0) { devProfileActive = false; }
        if (appendMessages)
            this.notifications = this.notifications.concat(messages);
        else
            this.notifications = messages;
        this.flagNotifications(this.notifications != undefined && this.notifications.length > 0);
        if (devProfileActive === undefined)
            devProfileActive = false;
        this.notificationDetailsPopup.html(this.notificationsTemplate({
            "messages": this.notifications,
            "devProfileActive": devProfileActive
        }));
        this.notificationsDialog.scrollTop(0);
    };
    MobileApp.prototype.handleSessionInvalid = function () {
        var self = this;
        self.openNotifications();
        self.notificationsDialog.on('dialogclose', function (event) {
            location.replace(self.rootUrl);
        });
    };
    MobileApp.prototype.handleRedirectHome = function () {
        var self = this;
        self.openNotifications();
        self.notificationsDialog.on('dialogclose', function (event) {
            self.processGet("mobile/home");
        });
    };
    MobileApp.prototype.handleValidationErrors = function () {
        var self = this;
        self.openNotifications();
    };
    MobileApp.prototype.handleException = function () {
        var self = this;
        self.openNotifications();
    };
    MobileApp.prototype.openFullscreen = function () {
        var self = this;
        var docElm = document.body;
        if (docElm.requestFullscreen) {
            docElm.requestFullscreen();
        }
        else if (docElm.mozRequestFullScreen) {
            docElm.mozRequestFullScreen();
        }
        else if (docElm.webkitRequestFullScreen) {
            docElm.webkitRequestFullScreen();
        }
        else if (docElm.msRequestFullscreen) {
            docElm.msRequestFullscreen();
        }
        self.fullScreenEnabled = true;
    };
    MobileApp.prototype.closeFullscreen = function () {
        var self = this;
        var docElm = document;
        if (docElm.exitFullscreen) {
            docElm.exitFullscreen();
        }
        else if (docElm.mozCancelFullScreen) {
            docElm.mozCancelFullScreen();
        }
        else if (docElm.webkitExitFullscreen) {
            docElm.webkitExitFullscreen();
        }
        else if (docElm.msExitFullscreen) {
            docElm.msExitFullscreen();
        }
        self.fullScreenEnabled = false;
    };
    MobileApp.prototype.toggleFullScreen = function () {
        var self = this;
        if (self.fullScreenEnabled) {
            self.closeFullscreen();
        }
        else {
            self.openFullscreen();
        }
    };
    MobileApp.prototype.goFullScreen = function () {
        var self = this;
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
    };
    MobileApp.prototype.additemToList = function (serial, selectList, ulList, removable, additionalProcessingForRemove) {
        var self = this;
        selectList.append('<option value="' + serial + '" selected>' + serial + '</option>');
        var li = $('<li>').attr('class', 'list-group-item custom');
        var sp = $('<span>').attr('class', 'float-left lgi-custom-left');
        sp.append(serial);
        li.append(sp);
        if (removable) {
            var bt = $('<button>').attr('type', 'button').attr('class', 'float-right remove-item lgi-custom-right btn-primary');
            var bti = $('<i>').attr('class', 'fas fa-times fa-2x');
            bt.on('click', function () {
                self.removeItemFromList(serial, selectList, additionalProcessingForRemove);
                $(this).parent().remove();
            });
            bt.append(bti);
            li.append(bt);
        }
        ulList.append(li);
    };
    MobileApp.prototype.removeItemFromList = function (serial, list, additionalProcessingForRemove) {
        list.children("option[value=" + serial + "]").remove();
        if (additionalProcessingForRemove) {
            additionalProcessingForRemove();
        }
    };
    MobileApp.prototype.escapeIllegalInputCharacters = function (rawString) {
        if (rawString == null)
            return rawString;
        return rawString.replace(/</g, '&lt;').replace(/>/g, '&gt;');
    };
    return MobileApp;
}());
