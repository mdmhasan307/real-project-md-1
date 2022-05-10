package mil.stratis.view.pack;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.InventoryModuleImpl;
import mil.stratis.model.services.PackingModuleImpl;
import mil.stratis.model.services.WorkLoadManagerImpl;
import mil.stratis.view.session.MdssBackingBean;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.nav.RichButton;
import oracle.jbo.Row;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This backing bean supports the Packing_Home, Packing_Normal and
 * Packing_CloseCarton web pages.
 */
@Slf4j
@NoArgsConstructor
public class ConsolBean extends MdssBackingBean {

  private transient Object stationState = 0; // default to consolidation
  private transient Object pinState = 0; //Default
  private transient Object singlePack = 0; //Default to false
  private transient Object lastPack = 0; //Default to false
  private transient ArrayList listScans = new ArrayList();
  private transient Object releaseState = 0;
  public transient InventoryModuleImpl inventoryModuleImpl;

  private RichInputText consolBarcode;
  private RichInputText locationBarcode;
  private RichInputText consolBarcode2;
  private RichInputText locationBarcode2;
  private String dispLocationBarcode;
  private String binLocation;
  private RichInputText pinVal;
  private transient RichButton pinListSubmit;
  private transient RichButton addPin;
  private transient RichButton pinListClear;
  private transient RichButton submitConsol;
  private transient RichButton submitConsol2;
  private transient RichButton submitLocation;
  private transient RichButton submitLocation2;
  private transient RichButton closeCartonButton;
  private transient RichButton cancelLocation;
  private transient RichButton cancelLocation2;
  private transient RichButton cancelConsol;
  private transient RichButton cancelConsol2;
  private String cartonoutputmessage = "";
  private transient RichTable cartonTable;

  // initial page loading function

  public void init() {
    stationState = 0; // default to consolidation
    pinState = 0; //Default
    singlePack = 0; //Default to false
    lastPack = 0; //Default to false
    if (addPin != null)
      addPin.setDisabled(false);
    if (pinListClear != null) {
      pinListClear.setDisabled(false);
    }
    if (pinListSubmit != null) {
      pinListSubmit.setDisabled(false);
    }
    if (pinVal != null) {
      pinVal.setValue("");
    }

    PackingModuleImpl service = getPackingModule();
    if (service != null) {
      int iWorkstationId = Util.cleanInt(JSFUtils.getManagedBeanValue("userbean.workstationId"));
      service.clearPackList(iWorkstationId, 1);
      service.clearPINList();
      inventoryModuleImpl = getInventoryAMService();
    }
  }

  /**
   * Set the station state variable.
   *
   * @param state - default 0
   */
  public void setStationState(Object state) {
    stationState = state;
  }

  /**
   * Set the pin state variable.
   *
   * @param state - default 0
   */
  public void setPinState(Object state) {
    pinState = state;
  }

  /**
   * Get the pin state variable.
   *
   * @return Object
   */
  public Object getPinState() {
    return pinState;
  }

  /**
   * Get the station state variable
   * 0 - Packing Station - Consolidation
   * 1 - any other workstation description.
   *
   * @return station state is always 0 or 1, default is 0
   */
  public Object getStationState() {
    //Get the workstationId from the UserBean
    int iWorkstationId = Util.cleanInt(JSFUtils.getManagedBeanValue("userbean.workstationId"));

    //Create a service to connect to the packing module application object
    PackingModuleImpl service = getPackingModule();

    //pass the converted workstationId int to the isConsolStation method in the packing station
    //application module to determine if the user is logged into a consolidation station or not.
    stationState = (service.isConsolStation(iWorkstationId)) ? 0 : 1;
    return stationState;
  }

  /**
   * Get the InventoryAMService for 1348
   *
   * @return InventoryAMService
   */
  public InventoryModuleImpl getInventoryModuleImpl() {
    return this.inventoryModuleImpl;
  }

  /**
   * This function takes the current PIN list and compares the
   * corresponding SCN for that PIN to the SCNs in the RejectList.
   * If there is a single SCN in the PINlist that isnt in the RejectList
   * then we are not finished processing it, and pinState should remain 1.
   */
  private void compareLists() {
    HashMap pinMap;
    Iterator itr = listScans.iterator();
    boolean bDone = false;

    int iWorkstationId = Util.cleanInt(JSFUtils.getManagedBeanValue("userbean.workstationId"));
    PackingModuleImpl service = getPackingModule();
    if (service.isPartialsPending(iWorkstationId)) {
      //There are partial refusals to be released
      pinState = 1;
      singlePack = 1;
      releaseState = 1;
    }
    else {
      if (itr.hasNext() && !bDone) {
        //There are still PINs to process

        //Check to see if this is a single pack item, if so it will not be stowed in a bin at a
        //MCPX station, and will immediately print a 1348.
        pinMap = (HashMap) itr.next();
        singlePack = (pinMap.get("pick").toString().equals("1 of 1")) ? 0 : 1;

        //Pins Found
        bDone = true;
      }

      pinState = (bDone) ? 1 : 0;
    }

    log.debug("compare packing station lists :: pinState {} :: singlePack :: {} ::  {}", pinState, singlePack, releaseState);
  }

  /**
   * This function is called from the close carton button and closes
   * the selected carton from the view.
   *
   * @param event - submit action from form Packing_Normal.jspx
   */
  public void closeCarton(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }

    DCIteratorBinding cartonIter = ADFUtils.findIterator("cartonView1Iterator");

    Object oldRowKey = cartonTable.getRowKey();
    Iterator selection = cartonTable.getSelectedRowKeys().iterator();
    int consolId = 0;
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        cartonTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) cartonTable.getRowData();
        Row r = rowData.getRow();
        if (r.getAttribute("PackingConsolidationId") != null)
          consolId = Util.cleanInt(r.getAttribute("PackingConsolidationId"));
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      cartonTable.setRowKey(oldRowKey);
    }

    //Convert the uid object to an int
    int iUserId = Util.cleanInt(JSFUtils.getManagedBeanValue("userbean.userId"));
    int iWorkstationId = Util.cleanInt(JSFUtils.getManagedBeanValue("userbean.workstationId"));

    PackingModuleImpl service = getPackingModule();
    WorkLoadManagerImpl serviceWL = getWorkloadManagerModule();

    //Call the application module function to close the carton
    if (service.manualCloseCarton(consolId, iUserId)) {
      //Carton was closed successfully, inform the user
      String consolBarcodeStr = service.getConsolidationBarcode(consolId);

      //Print Container Summary
      JSFUtils.setManagedBeanValue("userbean.winPrintContainerSummary", consolBarcodeStr);
      String url = JSFUtils.getManagedBeanValue("userbean.winPrintURL").toString();
      createPopUpWindows(url);
      //Send Items to shipping
      serviceWL.sendToShipping(consolId, iUserId);

      //Update the display
      service.displayCartons(iWorkstationId);

      //Change the display message on the front end if necessary
      cartonoutputmessage = "Carton Closed.";
    }
    else {
      JSFUtils.addFacesErrorMessage("Close Carton", "Error - Could not Manually Close Carton.");
    }
  }

  /**
   * This function is called when the "Submit" button is clicked
   * under the PIN List.   It takes the first issue available in the PIN list
   * and fills the packing list, and also populates the Rejection Set.
   *
   * @param event - submit action from form Packing_Normal.jspx
   */
  public void submitPINList(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    pinState = 1; //Set pinstate to false

    String strOfQuantities = "";
    listScans.clear();
    try {
      DCIteratorBinding dc = ADFUtils.findIterator("pinlist1Iterator");
      Row[] rows = dc.getAllRowsInRange();
      //Put all of the items in the PINlist into an arraylist of hashmaps
      HashMap pinMap;
      for (Row row : rows) {
        pinMap = new HashMap();
        pinMap.put("pin", Util.cleanString(row.getAttribute("PIN")));
        pinMap.put("doc", Util.cleanString(row.getAttribute("Document_Number")));
        pinMap.put("scn", Util.cleanString(row.getAttribute("SCN")));
        pinMap.put("qty", Util.cleanString(row.getAttribute("Qty")));
        pinMap.put("aac", Util.cleanString(row.getAttribute("AAC")));
        pinMap.put("pick", Util.cleanString(row.getAttribute("pickNo")));
        listScans.add(pinMap);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    //Compare listScans to determine single item pack or not, or if there is an item in the list to process
    compareLists();

    if (pinState.equals(1)) {
      //Disable buttons for previous steps until finished processing
      pinListSubmit.setDisabled(true);
      addPin.setDisabled(true);
      pinListClear.setDisabled(true);

      //Create a service to connect to the packing module application object
      PackingModuleImpl service = getPackingModule();
      WorkLoadManagerImpl serviceWL = getWorkloadManagerModule();

      //If there are items to process, fill the Packing List.
      //This takes the first hashmap off of the top of the iterator and puts it in pinlist2
      if (releaseState.equals(1)) {
        //reset release flag
        releaseState = 0;
      } // else {
      listScans = service.fillPackingList(listScans);
      //}

      //print out a 1348
      DCIteratorBinding dc2 = ADFUtils.findIterator("pinlist2Iterator");
      Row packRow = dc2.getRowAtRangeIndex(0);
      if (packRow == null) {
        JSFUtils.addFacesErrorMessage("SUBMIT PINS", "PIN list is empty.  No PINs to Pack.");
        pinState = 0; //This makes sure the Barcode Entry Panel doesn't display if there are no pins.
        //Re enable the buttons to allow for Adding and submitting Pins again.
        addPin.setDisabled(false);
        pinListClear.setDisabled(false);
        pinListSubmit.setDisabled(false);
      }
      else {
        log.debug("NOW packing  {}", Util.cleanString(packRow.getAttribute("SCN")));
        getStationState();

        //If stationstate == 0 Then it is a consolidation station
        if (stationState.equals(0)) {
          strOfQuantities = serviceWL.getSCNQuantitiesForReprint(packRow.getAttribute("SCN").toString());
          JSFUtils.setManagedBeanValue("userbean.winPrintSCN", strOfQuantities);
          String url = JSFUtils.getManagedBeanValue("userbean.winPrintURL").toString();
          createPopUpWindows(url);
          //get the packing consolidation id for the records in the packing list
          int consolId = service.getConsolIdByPin("packlist");

          if (consolId > 0) {
            //lookup the expected Bin location for that consolidation id
            binLocation = service.getBinColAndLev(consolId);
            //lookup the expected location barcode for that bin location / consolidation id
            dispLocationBarcode = service.getLocationBarcode(consolId);
          }
          else {
            //Did not get a correct consolId from getConsolIdByPin
            if (consolId == -2) JSFUtils.addFacesErrorMessage("SUBMIT PINS", "PIN list is empty.  No PINs to Pack.");
            binLocation = "Error";
            dispLocationBarcode = "Error";
          }
        }
        else {
          if (singlePack.equals(0)) {
            JSFUtils.setManagedBeanValue("userbean.winPrintSCN", packRow.getAttribute("SCN") + "," + packRow.getAttribute("Qty"));
            String url = JSFUtils.getManagedBeanValue("userbean.winPrintURL").toString();
            createPopUpWindows(url);
          }
          else {
            //Multiple-Pick Issue

            try {
              if (service.isLastPin(Util.cleanString(packRow.getAttribute("SCN")))) {
                //Last item to pack for this issue
                lastPack = 1;
                //Print 1348
                strOfQuantities = serviceWL.getSCNQuantitiesForReprint(packRow.getAttribute("SCN").toString());
                JSFUtils.setManagedBeanValue("userbean.winPrintSCN", strOfQuantities);
                String url = JSFUtils.getManagedBeanValue("userbean.winPrintURL").toString();
                createPopUpWindows(url);
              }
              else {
                //Still Items to Pack
                lastPack = 0;
              }
            }
            catch (Exception e) {
              AdfLogUtility.logException(e);
            }

            //get the packing consolidation id for the records in the packing list
            int consolId = service.getConsolIdByPin("packlist");

            if (consolId > 0) {
              //lookup the expected Bin location for that consolidation id
              binLocation = service.getBinColAndLev(consolId);
              //lookup the expected location barcode for that bin location / consolidation id
              dispLocationBarcode = service.getLocationBarcode(consolId);
            }
            else {
              //Did not get a correct consolId from getConsolIdByPin
              if (consolId == -2) JSFUtils.addFacesErrorMessage("SUBMIT PINS", "PIN list is empty.  No PINs to Pack.");
              binLocation = "Error";
              dispLocationBarcode = "Error";
            }
          }
        }
      }
    }
  }

  /**
   * This function takes the next available issue in the PIN
   * List and populates the Packing List in the Packing Module.
   */
  private void checkPINList() {
    String strOfQuantities = "";

    PackingModuleImpl service = getPackingModule();
    WorkLoadManagerImpl serviceWL = getWorkloadManagerModule();

    DCIteratorBinding dc = ADFUtils.findIterator("pinlist1Iterator");

    HashMap pinMap;
    Row[] rows = dc.getAllRowsInRange();

    listScans.clear();

    //Put all of the items in the PINlist into an arraylist of hashmaps
    for (int i = 0; i < rows.length; i++) {
      pinMap = new HashMap();
      pinMap.put("pin", rows[i].getAttribute("PIN").toString());
      pinMap.put("doc", rows[i].getAttribute("Document_Number").toString());
      pinMap.put("scn", rows[i].getAttribute("SCN").toString());
      pinMap.put("qty", rows[i].getAttribute("Qty").toString());
      pinMap.put("aac", rows[i].getAttribute("AAC").toString());
      pinMap.put("pick", rows[i].getAttribute("pickNo").toString());
      listScans.add(pinMap);
    }

    //Compare listScans to determine single item pack or not, or if there is an item in the list to process
    compareLists();

    if (pinState.equals(1)) {
      //Disable buttons for previous steps until finished processing
      pinListSubmit.setDisabled(true);
      addPin.setDisabled(true);
      pinListClear.setDisabled(true);

      //If there are items to process, fill the Packing List.
      //This takes the first hashmap off of the top of the iterator and puts it in pinlist2
      if (releaseState.equals(1)) {
        //reset release flag
        releaseState = 0;
      }
      listScans = service.fillPackingList(listScans);

      //either we need to print out a 1348 here from the bean or in the fillPackingList function
      DCIteratorBinding dc2 = ADFUtils.findIterator("pinlist2Iterator");
      Row packRow = dc2.getRowAtRangeIndex(0);

      getStationState();
      //If stationstate == 0 Then it is a consolidation station
      if (stationState.equals(0)) {
        strOfQuantities = serviceWL.getSCNQuantitiesForReprint(packRow.getAttribute("SCN").toString());
        //JSFUtils.setManagedBeanValue("userbean.useprintframe", "1");
        JSFUtils.setManagedBeanValue("userbean.winPrintSCN", strOfQuantities);
        String url = JSFUtils.getManagedBeanValue("userbean.winPrintURL").toString();
        createPopUpWindows(url);
        //get the packing consolidation id for the records in the packing list
        int consolId = service.getConsolIdByPin("packlist");

        if (consolId > 0) {
          //lookup the expected Bin location for that consolidation id
          binLocation = service.getBinColAndLev(consolId);
          //lookup the expected location barcode for that bin location / consolidation id
          dispLocationBarcode = service.getLocationBarcode(consolId);
        }
        else {
          //Did not get a correct consolId from getConsolIdByPin
          if (consolId == -2)
            JSFUtils.addFacesErrorMessage("SUBMIT PINS", "Consolodation list is empty.  No PINs to Pack.");
          binLocation = "Error";
          dispLocationBarcode = "Error";
        }
      }
      else {
        String url = JSFUtils.getManagedBeanValue("userbean.winPrintURL").toString();
        if (singlePack.equals(0)) {
          JSFUtils.setManagedBeanValue("userbean.winPrintSCN", packRow.getAttribute("SCN") + "," + packRow.getAttribute("Qty"));
          createPopUpWindows(url);
        }
        else {
          try {
            //Multiple-Pick Issue
            if (service.isLastPin(packRow.getAttribute("SCN").toString())) {
              //Last item to pack for this issue
              lastPack = 1;
              //Print 1348
              strOfQuantities = serviceWL.getSCNQuantitiesForReprint(packRow.getAttribute("SCN").toString());
              JSFUtils.setManagedBeanValue("userbean.winPrintSCN", strOfQuantities);
              createPopUpWindows(url);
            }
            else {
              //Still Items to Pack
              lastPack = 0;
            }
            //get the packing consolidation id for the records in the packing list
            int consolId = service.getConsolIdByPin("packlist");

            if (consolId > 0) {
              //lookup the expected Bin location for that consolidation id
              binLocation = service.getBinColAndLev(consolId);
              //lookup the expected location barcode for that bin location / consolidation id
              dispLocationBarcode = service.getLocationBarcode(consolId);
            }
            else {
              //Did not get a correct consolId from getConsolIdByPin
              if (consolId == -2)
                JSFUtils.addFacesErrorMessage("SUBMIT PINS", "PIN list is empty.  No PINs to Pack.");
              binLocation = "Error";
              dispLocationBarcode = "Error";
            }
          }
          catch (Exception e) {
            AdfLogUtility.logException(e);
          }
        }
      }
    }
  }

  /**
   * This function is called when the "Cancel" button is clicked
   * after a consolidation location barcode is entered.  This will reset
   * the packing window to its state prior to the submit action on the pinlist.
   *
   * @param event - submit action from form Packing_Normal.jspx
   */
  public void cancelLocationBarcode(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    submitLocation.setDisabled(false);

    Object wid = JSFUtils.getManagedBeanValue("userbean.workstationId");

    //Convert the workstationId object to an int
    int iWorkstationId = 0;
    if (wid != null) {
      iWorkstationId = Integer.parseInt(wid.toString());
    }

    // Reset window
    dispLocationBarcode = "";
    binLocation = "";

    pinListSubmit.setDisabled(false);
    addPin.setDisabled(false);
    pinListClear.setDisabled(false);
    //Clear the consolidation barcode value
    consolBarcode.setValue("");
    consolBarcode2.setValue("");
    //Clear the Packing List
    //Get the packing module service so that we can use the Packing Application Module
    PackingModuleImpl service = getPackingModule();

    service.clearPackList(iWorkstationId, 1);
    //Clear the right side of the screen
    pinState = 0;
  }

  public void verifyLocation1(ActionEvent event) {
    verifyLocationBarcode(locationBarcode.getValue().toString());
  }

  public void verifyLocation2(ActionEvent event) {
    verifyLocationBarcode(locationBarcode2.getValue().toString());
  }

  /**
   * This function is called when the "Submit" button is clicked
   * after a location barcode is canned at at CPCX station.   This will verify
   * that the consolidation bin matches the bin expected for the consolidation
   * and then continue the consolidation process or complete the issue process.
   */
  public void verifyLocationBarcode(String locationBar) {
    int iUserId = Util.cleanInt(JSFUtils.getManagedBeanValue("userbean.userId"));
    int iWorkstationId = Util.cleanInt(JSFUtils.getManagedBeanValue("userbean.workstationId"));

    locationBar = locationBar.toUpperCase();

    //Get the packing module service so that we can use the Packing Application Module
    PackingModuleImpl service = getPackingModule();
    WorkLoadManagerImpl serviceWL = getWorkloadManagerModule();

    if (binLocation.equalsIgnoreCase(locationBar)) {
      //The scanned barcode matches the expected barcode, pack it!

      //Get the consolidation id for the issue in the packing list
      int consolId = service.getConsolIdByPin("packlist");

      //pack the consolidation
      if (service.packConsolidatedIssue(consolId, iUserId)) {
        //Status changed to packed, continue.

        //Call the service to check if the carton should be closed
        int retVal = 1;

        if (stationState.equals(0)) {
          retVal = service.autoCloseCarton(consolId, iUserId);
          log.debug("autoclosedcarton retval is  {}", retVal);
        }

        if (retVal == 0) {
          //Carton was closed successfully, inform the user
          String consolBarcodeStr = service.getConsolidationBarcode(consolId);

          //Print Container Summary
          JSFUtils.setManagedBeanValue("userbean.winPrintContainerSummary", consolBarcodeStr);
          String url = JSFUtils.getManagedBeanValue("userbean.winPrintURL").toString();
          createPopUpWindows(url);
          //Send Item to Shipping
          serviceWL.sendToShipping(consolId, iUserId);

          //Reset Buttons and / or continue with PinList
          dispLocationBarcode = "";
          binLocation = "";
          consolBarcode.setValue("");
          consolBarcode2.setValue("");
          service.clearPackList(iWorkstationId, 0);

          pinListSubmit.setDisabled(false);
          addPin.setDisabled(false);
          pinListClear.setDisabled(false);

          //Clear the right side of the screen
          pinState = 0;

          checkPINList();
        }
        else if (retVal < 0) {
          //Error Message
          switch (retVal) {
            case -1:
              JSFUtils.addFacesErrorMessage("Pack Container", "Unknown Error, Function: autoCloseContainer()");
              break;
            case -2:
              JSFUtils.addFacesErrorMessage("Pack Container", "Error determining container contents.");
              break;
            case -3:
              JSFUtils.addFacesErrorMessage("Pack Container", "Unknown Error, Function: closeContainer()");
              break;
            case -4:
              JSFUtils.addFacesErrorMessage("Pack Container", "This container could not be found in the database.");
              break;
            case -5:
              JSFUtils.addFacesErrorMessage("Pack Container", "No packed issues found for this container.");
              break;
          }
          // Reset window
          dispLocationBarcode = "";
          binLocation = "";

          pinListSubmit.setDisabled(false);
          addPin.setDisabled(false);
          pinListClear.setDisabled(false);
          //Clear the consolidation barcode value
          consolBarcode.setValue("");
          consolBarcode2.setValue("");
          //Clear the Packing List
          service.clearPackList(iWorkstationId, 1);
          //Clear the right side of the screen
          pinState = 0;
        }
        else {
          //Still pending PINS for this carton, carton was not automatically closed.
          dispLocationBarcode = "";
          binLocation = "";
          consolBarcode.setValue("");
          consolBarcode2.setValue("");

          service.clearPackList(iWorkstationId, 0);

          pinListSubmit.setDisabled(false);
          addPin.setDisabled(false);
          pinListClear.setDisabled(false);

          //Clear the right side of the screen
          pinState = 0;

          checkPINList();
        }

        //Re-Enable the command button
        cancelLocation.setDisabled(false);
        submitLocation.setDisabled(false);
        submitLocation2.setDisabled(false);
        cancelLocation2.setDisabled(false);
      }
      else {
        dispLocationBarcode = "";
        binLocation = "";
        consolBarcode.setValue("");
        consolBarcode2.setValue("");

        service.clearPackList(iWorkstationId, 1);

        pinListSubmit.setDisabled(false);
        addPin.setDisabled(false);
        pinListClear.setDisabled(false);

        //Clear the right side of the screen
        pinState = 0;

        checkPINList();
      }
    }
    else {
      JSFUtils.addFacesErrorMessage("Incorrect Location", "Please check consolidation location.  Scanned Barcode was : " + locationBar + ", expected location is " + binLocation.toUpperCase());
    }

    //Re-Enable the command button
    locationBarcode.setValue("");
    locationBarcode2.setValue("");
    submitLocation.setDisabled(false);
    cancelLocation.setDisabled(false);
    submitLocation2.setDisabled(false);
    cancelLocation2.setDisabled(false);
  }

  /**
   * This function is called when the "Cancel" button is clicked
   * after a third party consolidation barcode is entered.  This will reset
   * the packing window to its state prior to the submit action on the pinlist.
   *
   * @param event - submit action from Packing_Normal.jspx
   */
  public void cancelConsolBarcode(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    submitConsol.setDisabled(false);
    submitConsol2.setDisabled(false);

    int iWorkstationId = Util.cleanInt(JSFUtils.getManagedBeanValue("userbean.workstationId"));

    // Reset window
    pinListSubmit.setDisabled(false);
    addPin.setDisabled(false);
    pinListClear.setDisabled(false);
    //Clear the consolidation barcode value
    consolBarcode.setValue("");
    consolBarcode2.setValue("");
    //Clear the Packing List
    PackingModuleImpl service = getPackingModule();

    service.clearPackList(iWorkstationId, 1);
    //Clear the right side of the screen
    pinState = 0;
  }

  public void addScannedConsol1(ActionEvent event) {
    addScannedConsolBarcode(consolBarcode.getValue().toString());
  }

  public void addScannedConsol2(ActionEvent event) {
    addScannedConsolBarcode(consolBarcode2.getValue().toString());
  }

  /**
   * This function is called when the "Submit" button is clicked
   * after a third party consolidation barcode is entered.  This will add
   * The barcode into the database and complete the packing process for the
   * current issue.
   * <p>
   * aka Enter Shipping Barcode
   */
  public void addScannedConsolBarcode(String consolStr) {

    int iUserId = Util.cleanInt(JSFUtils.getManagedBeanValue("userbean.userId"));
    int iWorkstationId = Util.cleanInt(JSFUtils.getManagedBeanValue("userbean.workstationId"));

    PackingModuleImpl service = getPackingModule();
    WorkLoadManagerImpl serviceWL = getWorkloadManagerModule();
    int retVal = 0;

    if (consolStr.length() > 4)
      consolStr = consolStr.toUpperCase().trim();
    else
      retVal = -5;

    if (retVal == 0)
      retVal = service.addConsolBarcode(consolStr, iUserId);
    if (retVal == 0) {
      //Barcode was added, item status changed, move on to next item
      //There is no need to print out a Consolidation List because this is a single packing station function
      //Re-Enable any buttons switchers here as needed.
      int iRet = 0;
      //Insert the appropriate records in the SHIPPING table for the barcode
      int consolId = service.getConsolIdByPin("packlist");

      iRet = serviceWL.sendToShipping(consolId, iUserId);

      if (iRet < 0) {
        JSFUtils.addFacesErrorMessage("Scan Barcode", "Error : Method sendToShipping()");
      }

      pinListSubmit.setDisabled(false);
      addPin.setDisabled(false);
      pinListClear.setDisabled(false);
      //Clear the consolidation barcode value
      consolBarcode.setValue("");
      consolBarcode2.setValue("");
      //Clear the Packing List
      service.clearPackList(iWorkstationId, 0);
      //Clear the right side of the screen
      pinState = 0;
    }
    else {
      switch (retVal) {
        case -1:
          JSFUtils.addFacesErrorMessage("Scan Barcode", "No barcode was entered.  Please enter a barcode.");
          break;
        case -2:
          JSFUtils.addFacesErrorMessage("Scan Barcode", "Error adding the shipping barcode to the database.  PIN could not be found.");
          break;
        case -3:
          JSFUtils.addFacesErrorMessage("Scan Barcode", "Error adding the shipping barcode.  Could not obtain a Packing_Consolidaton row.");
          break;
        case -4:
          JSFUtils.addFacesErrorMessage("Scan Barcode", "Shipping Barcode already exists in the database.  Shipping Barcodes must be unique.");
          break;
        case -5:
          JSFUtils.addFacesErrorMessage("Scan Barcode", "Shipping Barcode cannot be blank.  Shipping Barcodes cannot be blank and must consist of at least 5 characters.");
          break;
        case -6:
          JSFUtils.addFacesErrorMessage("Scan Barcode", "An error has occured.  Please try again.");
          break;
      }
    }

    //Re-Enable the command button
    submitConsol.setDisabled(false);
    cancelConsol.setDisabled(false);
    submitConsol2.setDisabled(false);
    cancelConsol2.setDisabled(false);
  }

  /**
   * This function is called when the "Add Pin" button is clicked.
   * This will add the current scanned PIN label to the PIN List.
   *
   * @param event - submit action from Packing_Normal.jspx
   */
  public void addPin(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    //Disable the command button
    addPin.setDisabled(true);
    String PIN = (String) pinVal.getValue();
    //* input validation
    if (Util.isEmpty(PIN)) {
      JSFUtils.addFacesErrorMessage("ADD PIN", "PIN required field. Please enter a valid PIN.");
      addPin.setDisabled(false);
      return;
    }

    PIN = Util.trimUpperCaseClean(PIN);
    int iWorkstationId = Util.cleanInt(JSFUtils.getManagedBeanValue("userbean.workstationId"));
    PackingModuleImpl service = getPackingModule();
    int retVal = service.addPIN(PIN, iWorkstationId);
    if (retVal != 0) {
      //Any value other than 0 is an error message
      switch (retVal) {
        case -1:
          JSFUtils.addFacesErrorMessage("ADD PIN", "PIN " + PIN + " is not a valid PIN.");
          break;
        case -2:
          JSFUtils.addFacesErrorMessage("ADD PIN", "PIN " + PIN + " already exists in the PIN List.");
          break;
        case -3:
          JSFUtils.addFacesErrorMessage("ADD PIN", "PIN " + PIN + " has already been Packed.  Send to Shipping or find proper Packing bin.");
          break;
        case -4:
          JSFUtils.addFacesErrorMessage("ADD PIN", "PIN " + PIN + " has not been recorded as Picked yet.  Make sure PIN is valid.");
          break;
        default: {

          //A non-negative number is a workstation id and would display the workstation to route the item to
          String wsName = service.getWorkstationName(retVal);
          if (wsName == null)
            wsName = "that is on PIN label";
          JSFUtils.addFacesErrorMessage("Incorrect Packing Station", "PIN " + PIN + " is not assigned to the current workstation. PIN can only be packed at " + wsName);
        }
        break;
      }
    }

    pinVal.setValue("");
    //Re-Enable the command button
    addPin.setDisabled(false);
  }

  /**
   * This function is called when the "Clear" button is clicked.
   * This will clear out the list of PIN labels that are currently displayed
   * in the PIN List for processing
   *
   * @param event - submit action from Packing_Normal.jspx
   */
  public void clearPinList(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    PackingModuleImpl service = getPackingModule();

    service.clearPINList();
  }

  /**
   * This function is called when the "Packing" tab is clicked.
   * This will determine whether or not the tables on the Packing_Home
   * page will be displayed.
   */
  public void onPageLoad() {
    //Get the workstationId from the userbean
    cartonoutputmessage = "";

    //Convert the workstationId object to an int
    int iWorkstationId = Util.cleanInt(JSFUtils.getManagedBeanValue("userbean.workstationId"));

    PackingModuleImpl service = getPackingModule();

    //send the workstation_id to the packing application module to load the pinload detail report on the
    //PACKING_HOME page
    HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    String requestURL = request.getRequestURL().toString();
    if (requestURL.contains("Packing_Home")) {
      service.refreshPINLoadDetail(iWorkstationId);
    }
    else if (requestURL.contains("Packing_CloseCarton")) {
      service.displayCartons(iWorkstationId);
    }
    else { //PACKING NORMAL, reset to normal if
      if (!ADFUtils.isPostback()) {
        init();
      }
    }
  }

  /**
   * This method creates a pop up window for a given URL
   */
  void createPopUpWindows(String url) {
    FacesContext fctx = FacesContext.getCurrentInstance();
    //String taskflowURL = "http://www.google.com";
    ExtendedRenderKitService erks = Service.getRenderKitService(fctx, ExtendedRenderKitService.class);
    StringBuilder script = new StringBuilder();
    script.append("window.open(\"").append(url).append("\");");
    erks.addScript(fctx, script.toString());
  }

  /**
   * Set the pinListSubmit button variable.
   *
   * @param listButton - RichCommandButton variable
   */
  public void setPinListSubmit(RichButton listButton) {
    pinListSubmit = listButton;
  }

  /**
   * Get the pinListSubmit button variable.
   *
   * @return RichCommandButton
   */
  public RichButton getPinListSubmit() {
    return pinListSubmit;
  }

  /**
   * Set the addPin button variable.
   *
   * @param addPinButton - RichCommandButton variable
   */
  public void setAddPin(RichButton addPinButton) {
    addPin = addPinButton;
  }

  /**
   * Get the addPin button variable.
   *
   * @return RichCommandButton
   */
  public RichButton getAddPin() {
    return addPin;
  }

  /**
   * Set the pinListClear button variable.
   *
   * @param pinListClearButton - RichCommandButton variable
   */
  public void setPinListClear(RichButton pinListClearButton) {
    pinListClear = pinListClearButton;
  }

  /**
   * Get the pinListClear button variable.
   *
   * @return RichCommandButton
   */
  public RichButton getPinListClear() {
    return pinListClear;
  }

  /**
   * Set the pinVal Input variable.
   *
   * @param pinValIn - Input value of the PIN
   */
  public void setPinVal(RichInputText pinValIn) {
    pinVal = pinValIn;
  }

  /**
   * Get the pinVal String variable.
   *
   * @return String
   */
  public RichInputText getPinVal() {
    return pinVal;
  }

  /**
   * Set the consolBarcode RichInputText variable.
   *
   * @param consolBarcodeIn - RichInputText value of the consolidation barcode
   */
  public void setConsolBarcode(RichInputText consolBarcodeIn) {
    consolBarcode = consolBarcodeIn;
  }

  /**
   * Get the consolBarcode RichInputText variable.
   *
   * @return RichInputText
   */
  public RichInputText getConsolBarcode() {
    return consolBarcode;
  }

  /**
   * Set the locationBarcode input variable.
   *
   * @param locationBarcodeIn - location of the barcode
   */
  public void setLocationBarcode(RichInputText locationBarcodeIn) {
    locationBarcode = locationBarcodeIn;
  }

  /**
   * Get the locationBarcode String variable.
   *
   * @return RichInputText
   */
  public RichInputText getLocationBarcode() {
    return locationBarcode;
  }

  /**
   * Set the consolBarcode RichInputText variable.
   *
   * @param consolBarcodeIn - RichInputText value of the consolidation barcode
   */
  public void setConsolBarcode2(RichInputText consolBarcodeIn) {
    consolBarcode2 = consolBarcodeIn;
  }

  /**
   * Get the consolBarcode RichInputText variable.
   *
   * @return RichInputText
   */
  public RichInputText getConsolBarcode2() {
    return consolBarcode2;
  }

  /**
   * Set the locationBarcode input variable.
   *
   * @param locationBarcodeIn - location of the barcode
   */
  public void setLocationBarcode2(RichInputText locationBarcodeIn) {
    locationBarcode2 = locationBarcodeIn;
  }

  /**
   * Get the locationBarcode String variable.
   *
   * @return RichInputText
   */
  public RichInputText getLocationBarcode2() {
    return locationBarcode2;
  }

  /**
   * Set the binLocation String variable.
   *
   * @param binLoc - location of the bin
   */
  public void setBinLocation(String binLoc) {
    binLocation = binLoc;
  }

  /**
   * Get the binLocation String variable.
   *
   * @return String
   */
  public String getBinLocation() {
    return binLocation;
  }

  /**
   * Set the dispLocationBarcode String variable.
   *
   * @param barcode - disposition location of the barcode
   */
  public void setDispLocationBarcode(String barcode) {
    dispLocationBarcode = barcode;
  }

  /**
   * Get the dispLocationBarcode String variable.
   *
   * @return String
   */
  public String getDispLocationBarcode() {
    return dispLocationBarcode;
  }

  /**
   * Set the submitConsol button variable.
   *
   * @param submitConsoleButton - RichCommandButton variable
   */
  public void setSubmitConsol(RichButton submitConsoleButton) {
    submitConsol = submitConsoleButton;
  }

  /**
   * Get the submitConsol button variable.
   *
   * @return RichCommandButton
   */
  public RichButton getSubmitConsol() {
    return submitConsol;
  }

  /**
   * Set the submitLocation button variable.
   *
   * @param submitLocationButton - RichCommandButton variable
   */
  public void setSubmitLocation(RichButton submitLocationButton) {
    submitLocation = submitLocationButton;
  }

  /**
   * Get the submitLocation button variable.
   *
   * @return RichCommandButton
   */
  public RichButton getSubmitLocation() {
    return submitLocation;
  }

  /**
   * Set the closeCartonButton button variable.
   *
   * @param button - RichCommandButton variable
   */
  public void setCloseCartonButton(RichButton button) {
    closeCartonButton = button;
  }

  /**
   * Get the closeCartonButton button variable.
   *
   * @return RichCommandButton
   */
  public RichButton getCloseCartonButton() {
    return closeCartonButton;
  }

  /**
   * Set the cartonoutputmessage String variable.
   *
   * @param message - message displayed for carton
   */
  public void setCartonoutputmessage(String message) {
    cartonoutputmessage = message;
  }

  /**
   * Get the cartonoutputmessage String variable.
   *
   * @return message displayed for carton
   */
  public String getCartonoutputmessage() {
    return cartonoutputmessage;
  }

  /**
   * Set the cancelLocation button variable.
   *
   * @param cancelLocationButton - RichCommandButton variable
   */
  public void setCancelLocation(RichButton cancelLocationButton) {
    cancelLocation = cancelLocationButton;
  }

  /**
   * Get the cancelLocation button variable.
   *
   * @return RichCommandButton
   */
  public RichButton getCancelLocation() {
    return cancelLocation;
  }

  /**
   * Set the cancelConsol button variable.
   *
   * @param cancelConsoleButton - RichCommandButton variable
   */
  public void setCancelConsol(RichButton cancelConsoleButton) {
    cancelConsol = cancelConsoleButton;
  }

  /**
   * Get the cancelConsol button variable.
   *
   * @return RichCommandButton
   */
  public RichButton getCancelConsol() {
    return cancelConsol;
  }

  /**
   * Set the singlePack object variable.
   *
   * @param singlePackObj - 0 true and 1 false, default 0
   */
  public void setSinglePack(Object singlePackObj) {
    singlePack = singlePackObj;
  }

  /**
   * Get the singlePack object variable.
   *
   * @return Object (always 0 or 1, default 0)
   */
  public Object getSinglePack() {
    return singlePack;
  }

  /**
   * Set the lastPack object variable.
   *
   * @param lastPackObj - holds the information of the item last packed
   */
  public void setLastPack(Object lastPackObj) {
    lastPack = lastPackObj;
  }

  /**
   * Get the lastPack object variable.
   *
   * @return Object
   */
  public Object getLastPack() {
    return lastPack;
  }

  /**
   * Set the submitLocation2 button variable.
   *
   * @param submitLocation2Button - RichCommandButton variable
   */
  public void setSubmitLocation2(RichButton submitLocation2Button) {
    submitLocation2 = submitLocation2Button;
  }

  /**
   * Get the submitLocation2 button variable.
   *
   * @return RichCommandButton
   */
  public RichButton getSubmitLocation2() {
    return submitLocation2;
  }

  /**
   * Set the cancelLocation2 button variable.
   *
   * @param cancelLocation2Button - RichCommandButton variable
   */
  public void setCancelLocation2(RichButton cancelLocation2Button) {
    cancelLocation2 = cancelLocation2Button;
  }

  /**
   * Get the cancelLocation2 button variable.
   *
   * @return RichCommandButton
   */
  public RichButton getCancelLocation2() {
    return cancelLocation2;
  }

  /**
   * Set the cancelConsol2 button variable.
   *
   * @param cancelConsol2Button - RichCommandButton variable
   */
  public void setCancelConsol2(RichButton cancelConsol2Button) {
    cancelConsol2 = cancelConsol2Button;
  }

  /**
   * Get the cancelConsol2 button variable.
   *
   * @return RichCommandButton
   */
  public RichButton getCancelConsol2() {
    return cancelConsol2;
  }

  /**
   * Set the submitConsol2 button variable.
   *
   * @param submitConsol2Button - RichCommandButton variable
   */
  public void setSubmitConsol2(RichButton submitConsol2Button) {
    submitConsol2 = submitConsol2Button;
  }

  /**
   * Get the submitConsol2 button variable.
   *
   * @return RichCommandButton
   */
  public RichButton getSubmitConsol2() {
    return submitConsol2;
  }

  /**
   * Set the cartonTable table variable.
   *
   * @param table - RichTable variable
   */
  public void setCartonTable(RichTable table) {
    cartonTable = table;
  }

  /**
   * Get the cartonTable table variable.
   *
   * @return RichTable
   */
  public RichTable getCartonTable() {
    return cartonTable;
  }

  /**
   * Override the toString method with custom code.
   *
   * @return String
   */
  public String toString() {
    return "ConsolBean";
  }
}

