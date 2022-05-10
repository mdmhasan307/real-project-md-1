<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
  <title>Mobile App - Print</title>
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="templatename" content="internal"/>
  <style>
    .ui-dialog-titlebar-close {
      width: 45px !important;
      height: 45px !important;
      top: 24% !important;
    }

    .ui-dialog .ui-dialog-titlebar, .ui-dialog .ui-dialog-buttonpane {
      font-size: 1.25em;
    }

  </style>
  <jsp:include page="/WEB-INF/jsp/global/head.jsp"/>

  <script type="text/javascript">
    var selected_device;
    var devices = [];

    function setup() {
      //Get the default device from the application as a first step. Discovery takes longer to complete.
      BrowserPrint.getDefaultDevice("printer", function (device) {

        //Add device to list of devices and to html select element
        selected_device = device;
        devices.push(device);
        var html_select = document.getElementById("selected_device");
        var option = document.createElement("option");
        option.text = device.name;
        html_select.add(option);

        //Discover any other devices available to the application
        BrowserPrint.getLocalDevices(function (device_list) {
          for (var i = 0; i < device_list.length; i++) {
            //Add device to list of devices and to html select element
            var device = device_list[i];
            if (!selected_device || device.uid !== selected_device.uid) {
              devices.push(device);
              var option = document.createElement("option");
              option.text = device.name;
              option.value = device.uid;
              html_select.add(option);
            }
          }

        }, function () {
          $("#bpErrors").append("<li>Error getting local devices.</li>")
        }, "printer");

      }, function (error) {
        $("#bpErrors").append("<li>" + error + "</li>");
      })
    }

    function writeToSelectedPrinter(dataToWrite) {
      selected_device.send(dataToWrite, undefined, errorCallback);
    }

    var errorCallback = function (errorMessage) {
      $("#bpErrors").append("<li>Error: " + errorMessage + "</li>");
    }

    function onDeviceSelected(selected) {
      for (var i = 0; i < devices.length; ++i) {
        if (selected.value === devices[i].uid) {
          selected_device = devices[i];
          return;
        }
      }
    }
  </script>
</head>
<body>

<jsp:useBean id="userbean" class="mil.stratis.view.user.UserInfo" scope="session"/>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form id="testPrintForm" method="POST">
  <div class="flex-button-container">
    <div class="flex-button-group frmBtns">
      <button type="button" class="btn btn-primary" id="btnExit" onclick="window.close();">Close After Printing</button>
    </div>
  </div>
</form>


<%
  if (userbean.getUseprintcom().equals(1)) {
%>
<div width="100%" style="text-align:center;">
  <applet height="1" width="1" code="comapplet.COMApplet.class"
          archive="${userbean.url}/applet/SCOMApplet.jar">
    <param name="permissions" value="all-permissions"/>
    <param name="codebase_lookup" value="false"/>
    <param name="port" value="<c:out value='${userbean.returncomport}'/>"/>
    <param name="str" value="<c:out value='${userbean.printcomstring}'/>"/>
    <param name="debug" value="true"/>
    <param name="baud" value="<c:out value='${userbean.returncombaud}'/>"/>
    <param name="parity" value="<c:out value='${userbean.returncomparity}'/>"/>
    <param name="stopbits" value="<c:out value='${userbean.returncomstopbits}'/>"/>
    Sorry, your browser does not support applets - label printing via applets will not work.
  </applet>
</div>

<div id="javascriptPrintingDiv" style="display:none; width:100%; text-align:center">
  <div class="flex-form-container">
    <div class="scrollable_tall">
      <div class="table-responsive table-wrapper">
        <table class="table table-sm table-bordered" id="javascriptPrintingTable">
          <tr>
            <th colspan="3">Non-applet Print Options</th>
          </tr>
          <tr>
            <th>1) Print using raw text</th>
            <th>2) Print using BrowserPrint</th>
            <th>3) Print from text file</th>
          </tr>
          <tr>
            <td>
              Attempt to print raw text directly to your printer.
              <br/>
              This option requires that your printer is configured on your system to use the <b>Generic/Text Only printer driver</b>.
              <br/>
              <button type="button" class="btn btn-primary" id="printGeneric" onclick="printZpl();">Print via raw text.</button>
            </td>
            <td>
              Attempt to print directly from your browser using BrowserPrint.
              <br/>
              This option requires that (1) BrowserPrint is installed on your system; and (2) that your printer is configured on your system
              to use the <b>appropriate Zebra printer driver</b>.
              <br/>
              <button type="button" class="btn btn-primary" id="showBrowserPrint" onclick="showBrowserPrintDiv();">Print via BrowserPrint
              </button>

              <div id="printWithBrowserPrintDiv" width="100%" style="display:none;">
                Please select your printer, as configured in BrowserPrint:
                <select id="selected_device" onchange="onDeviceSelected(this);"></select>

                <br/><br/>
                <button type="button" class="btn btn-primary" id="printthis" onclick="printLabelUsingBrowserPrint();">Print to selected device
                </button>

                <br/>
                <span id="bpErrors" style="color:red;"></span>
              </div>
            </td>
            <td>Download the label text and print from Notepad.<br/>
              This option requires that your printer is configured on your system to use the <b>Generic/Text Only printer driver</b>.
              <a href="${userbean.url}/app/mobile/downloadzpl" download="label.txt">Download raw label</a>
              <br/>
              If you are using Internet Explorer, please right-click and select "Save target as..." to save the label.txt file to your computer.
            </td>
          </tr>
        </table>
      </div>
    </div>
  </div>
</div>
<%
  }
%>

<script>
  function showBrowserPrintDiv() {
    $("#printWithBrowserPrintDiv").show();
    setup();
  }

  function printLabelUsingBrowserPrint() {
    writeToSelectedPrinter("${userbean.printcomstring}");
  }

  function printZpl() {
    var printWindow = window.open();
    printWindow.document.open('text/plain')
    printWindow.document.write("${userbean.printcomstring}");
    printWindow.document.close();
    printWindow.focus();
    printWindow.print();
    printWindow.close();
  }

  if (${javascriptPrintingEnabled}) {
    $("#javascriptPrintingDiv").show();
  }
</script>

</body>
</html>
