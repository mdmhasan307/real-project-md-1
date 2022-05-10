package mil.stratis.view.shipping;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.ShippingServiceImpl;
import mil.stratis.model.view.ship.*;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.domain.exception.FakeException;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.component.rich.nav.RichCommandButton;
import oracle.adf.view.rich.component.rich.nav.RichLink;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.binding.OperationBinding;
import oracle.jbo.Row;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.myfaces.trinidad.event.PollEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@Slf4j
public class ShippingBackingBean extends ShippingHandler {

  private final int iUserId;

  /**
   * variables for acknowledge
   */
  private transient RichTable acknowledgeTable;
  private transient RichSelectOneChoice acknowledgeModes;
  private transient RichInputText driver;

  /**
   * variables for remark
   */
  private transient RichInputText resetAACText;
  private transient RichSelectOneChoice remarkContainers;
  private List<SelectItem> containers = new ArrayList<>();
  private Map<String, String> aacMap = new HashMap<>();
  private transient RichSelectOneChoice remarkAacs;
  private List<SelectItem> aacs = new ArrayList<>();

  /**
   * variables for the manifest
   */
  private List<SelectItem> allCombineLDCON;
  private transient RichSelectOneChoice combineLDCONNavList;
  private transient RichSelectOneChoice manifestFloors;
  private List<SelectItem> manifestList = new ArrayList<>();
  boolean hasManifestReview = false;
  private transient RichTable tableManifestReview;
  private transient RichCommandButton downloadCommandButton;
  private transient RichInputText downloadManifest;

  /**
   * variables for the manifest by aac
   */
  private transient RichSelectOneChoice manifestAacs;
  private transient RichTable aacFloorTable;
  private List<SelectItem> manifestAACList = new ArrayList<>();

  /**
   * variables for the relocate pallet
   */
  private transient RichTable palletTable;
  private transient RichSelectOneChoice relocateToNavList;
  private List<SelectItem> floorLocations = new ArrayList<>();

  public ShippingBackingBean() {
    init();

    //* Get the current user ID
    iUserId = getUserId();
  }

  public ShippingBackingBean(final int workstationId, final int userId, final String uri) {
    init(workstationId, userId, uri);
    iUserId = userId;
  }

  public void selectAll(@SuppressWarnings("all") ActionEvent event) {
    RowKeySet rks = new RowKeySetImpl();
    CollectionModel model = (CollectionModel) acknowledgeTable.getValue();

    int rowcount = model.getRowCount();
    for (int i = 0; i < rowcount; i++) {
      model.setRowIndex(i);
      Object key = model.getRowKey();
      rks.add(key);
    }
    acknowledgeTable.setSelectedRowKeys(rks);
  }

  public void selectNone(@SuppressWarnings("all") ActionEvent event) {
    RowKeySet rks = new RowKeySetImpl();
    acknowledgeTable.setSelectedRowKeys(rks);
  }

  /**
   * Sets the list used for the shipment modes pull down
   * binding to the form
   */
  public void setAcknowledgeModes(RichSelectOneChoice acknowledgeModes) {
    this.acknowledgeModes = acknowledgeModes;
  }

  /**
   * Returns the list used for the shipment modes pull down
   * binding to the form
   *
   * @return RichSelectOneChoice
   */
  public RichSelectOneChoice getAcknowledgeModes() {
    return acknowledgeModes;
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      acknowledgeShipment(null);
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmSubmit");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(acknowledgeTable);
    }
  }

  public void closeDialog(@SuppressWarnings("all") ActionEvent event) {
    RichPopup popup;
    popup = (RichPopup) JSFUtils.findComponentInRoot("confirmSubmit");
    popup.hide();
  }

  /**
   * This function is the main backing function for the Shipping
   * Acknowledge Shipment page.  On submit, the operation to perform is
   * to acknowledge n number of manifest
   * to delivered or picked up
   */
  public void acknowledgeShipment(@SuppressWarnings("all") ActionEvent event) {
    //* invoke the service to update the data
    ShippingServiceImpl shippingService = getShippingServiceModule();

    String type = "D";
    String i = (String) getAcknowledgeModes().getValue();
    if (i != null && i.equals("0")) type = "P";

    if (Util.isEmpty(driver.getValue())) {
      //* show error
      JSFUtils.addFacesErrorMessage("ERROR", "Driver is required.");
      return;
    }
    else {
      if (RegUtils.isNotAlphaNumeric(driver.getValue().toString())) {
        JSFUtils.addFacesErrorMessage("ERROR", "Driver is required to be alphanumeric only.");
        return;
      }
    }

    //* get the iterator used to populate the acknowledge table
    Object oldRowKey = acknowledgeTable.getRowKey();
    Iterator<Object> selectIter = acknowledgeTable.getSelectedRowKeys().iterator();
    try {

      while (selectIter.hasNext()) {
        Object rowKey = selectIter.next();
        acknowledgeTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) acknowledgeTable.getRowData();
        Row r = rowData.getRow();
        Object shippingManifestId = r.getAttribute("ShippingManifestId");
        Object manifest = r.getAttribute("Manifest");
        val shippingManifestIdStr = shippingManifestId.toString();
        log.debug("row  {}", shippingManifestIdStr);
        //* acknowledge delivery for 1 manifest at a time
        if (shippingService.submitAcknowledgeShipment(shippingManifestIdStr,
            Util.trimUpperCaseClean(driver.getValue()), iUserId, type)) {
          JSFUtils.addFacesInformationMessage("Successfully acknowledged shipment for manifest " + manifest + ".");
          DCIteratorBinding iter = ADFUtils.findIterator("UndeliveredLDCONResultsListIterator");
          iter.executeQuery();
        }
        else {
          //* display message to GUI
          JSFUtils.addFacesErrorMessage("ERROR", "Unable to acknowledge shipment for manifest " + manifest + ".");
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      acknowledgeTable.setRowKey(oldRowKey);
    }
    acknowledgeModes.setValue("");
    driver.setValue("");
  }

  public void setDriver(RichInputText ack) {
    this.driver = ack;
  }

  public RichInputText getDriver() {
    return driver;
  }

  /**
   * Sets the table used to display the manifested containers
   * binding to the form
   */
  public void setAcknowledgeTable(RichTable acknowledgeTable) {
    this.acknowledgeTable = acknowledgeTable;
  }

  /**
   * Returns the table used to display the manifested containers
   * binding to the form
   *
   * @return RichTable
   */
  public RichTable getAcknowledgeTable() {
    return acknowledgeTable;
  }

  /**
   * This function is the main backing function for the Shipping
   * Remark AAC page.  On submit, the operation to perform is
   * to remark AAC for shipping manifest ids
   */
  public void submitRemarkAAC(@SuppressWarnings("all") ActionEvent event) {
    StringBuilder msgOutput = new StringBuilder();
    if (isEmpty(getRemarkContainers().getValue())) {
      msgOutput.append("Required Field - Select a Container ");
      displayMessage(msgOutput);
      return;
    }

    if (isEmpty(getRemarkAacs().getValue())) {
      msgOutput.append("Required Field - Select an AAC to Remark to");
      displayMessage(msgOutput);
      return;
    }
    String customerId = getRemarkAacs().getValue().toString();
    String remarkContainer = getLeadTCNRemarkLabel(getRemarkContainers().getValue().toString());
    log.debug("remarking {} to customerId {} - {}", remarkContainer, customerId, iUserId);

    //* invoke the service to update the data
    ShippingServiceImpl shippingService = getShippingServiceModule();

    try {
      List<String> ldcons = shippingService.remarkShipment(remarkContainer, customerId,
          iUserId);

      msgOutput.append("Successfully Remarked AAC for container ").append(remarkContainer);
      int a = 0;
      //* reprint manifest to reflect new aac
      for (String ldcon : ldcons) {
        if (a == 0) msgOutput.append("\nThe following shipping manifest (LDCON) must be reprinted:");
        msgOutput.append('\n').append(ldcon);
        a++;
      }
      //* display message to GUI
      displaySuccessMessage(msgOutput);
    }
    catch (FakeException e) {
      String aac = getAACRemarkLabel(customerId);
      msgOutput.append("An error occurred while remarking ");
      msgOutput.append("container ").append(remarkContainer).append(" to aac ").append(aac);
      // display message to GUI
      displayMessage(msgOutput);
    }

    resetRemarkForm();
  }

  /**
   * This method resets the Remark form to its default values
   */
  private void resetRemarkForm() {
    getRemarkAacs().resetValue();
    getRemarkContainers().resetValue();
    getResetAACText().resetValue();
  }

  public void changeRemarkContainerValue(ValueChangeEvent valueChangeEvent) {
    if (valueChangeEvent.getNewValue() != null) {
      //* get the selected lead tcn
      String id = valueChangeEvent.getNewValue().toString();
      //* change the aac text value
      if (id != null)
        getResetAACText().setValue(aacMap.get(id));
    }
  }

  /**
   * Returns the associated label of a selected item in the relocate to navigation list
   *
   * @return String
   */
  private String getAACRemarkLabel(String value) {
    List<SelectItem> list = aacs;
    return getListLabel(value, list);
  }

  /**
   * Returns the associated label of a selected item in the relocate to navigation list
   *
   * @return String
   */
  private String getLeadTCNRemarkLabel(String value) {
    List<SelectItem> list = containers;
    return getListLabel(value, list);
  }

  /**
   * Sets the list used for the AAC pull down
   * binding to the form
   */
  public void setRemarkAacs(RichSelectOneChoice remarkAacs) {
    this.remarkAacs = remarkAacs;
  }

  /**
   * Returns the list used for the AAC pull down
   * binding to the form
   *
   * @return RichSelectOneChoice
   */
  public RichSelectOneChoice getRemarkAacs() {
    return remarkAacs;
  }

  /**
   * Sets the list used for the container pull down
   * binding to the form
   */
  public void setRemarkContainers(RichSelectOneChoice remarkContainers) {
    this.remarkContainers = remarkContainers;
  }

  /**
   * Returns the list used for the container pull down
   * binding to the form
   *
   * @return RichSelectOneChoice
   */
  public RichSelectOneChoice getRemarkContainers() {
    return remarkContainers;
  }

  /**
   * Returns the list to populate container pull down
   * binding to the form
   *
   * @return List
   */
  public List<SelectItem> getContainers() {
    // clear the list before sending it out
    containers.clear();
    aacMap.clear();
    UndeliveredContainersListImpl containerslist = (UndeliveredContainersListImpl) getShippingServiceModule().getListContainersRemark();
    containerslist.setEQUIPMENT_NUMBER(JSFUtils.getManagedBeanValue("userbean.workstationId").toString());
    containerslist.setRangeSize(-1);
    containerslist.executeQuery();
    Row[] rows = containerslist.getAllRowsInRange();
    for (Row row : rows) {
      containers.add(new SelectItem(row.getAttribute("ShippingManifestId").toString(), row.getAttribute("LeadTcn").toString()));
      aacMap.put(row.getAttribute("ShippingManifestId").toString(), row.getAttribute("Aac").toString());
    }
    return containers;
  }

  /**
   * Sets the list for containers
   */
  public void setContainers(List<SelectItem> input) {
    containers = input;
  }

  /**
   * Sets the AAC text value displayed to user
   * binding to the form
   */
  public void setResetAACText(RichInputText resetAACText) {
    this.resetAACText = resetAACText;
  }

  /**
   * Returns the AAC text value displayed to user
   * binding to the form
   *
   * @return RichInputText
   */
  public RichInputText getResetAACText() {
    return resetAACText;
  }

  /**
   * Returns the AAC list for dropdown.
   *
   * @return List
   */
  public List<SelectItem> getAacs() {
    // clear the list before sending it out
    aacs.clear();
    UnrestrictedCustomersListImpl customerslist = (UnrestrictedCustomersListImpl) getShippingServiceModule().getRemarkTo();
    customerslist.setRangeSize(-1);
    customerslist.executeQuery();
    Row[] rows = customerslist.getAllRowsInRange();
    for (Row row : rows) {
      aacs.add(new SelectItem(row.getAttribute("CustomerId"), row.getAttribute("Aac").toString()));
    }
    return aacs;
  }

  /**
   * Sets the AAC list
   */
  public void setAacs(List<SelectItem> input) {
    aacs = input;
  }

  /**
   * This function is the main backing function for the Shipping
   * Relocate Pallet page.  On submit, the operation to perform is
   * to relocate n number of shipping manifest ids
   * to a specific floor location id
   * IMPORTANT NOTE:  Pallets being relocated are already manifested, and therefore
   * always will require a reprint of old floor location and new
   * floor location manifests.
   */
  public void relocatePallet(@SuppressWarnings("all") ActionEvent event) {
    boolean relocate = false;

    StringBuilder msgOutput = new StringBuilder();

    String selectedRelocateToValue =
        getRelocateToNavList().getValue().toString();

    if (selectedRelocateToValue != null) {
      List<String> ldcons = new ArrayList<>();

      //* invoke the service to update the data
      ShippingServiceImpl shippingService = getShippingServiceModule();

      log.debug("****update shipping manifest {}", selectedRelocateToValue);

      Object oldRowKey = palletTable.getRowKey();
      Iterator<Object> selection = palletTable.getSelectedRowKeys().iterator();
      int errorCount = 0;

      try {
        while (selection.hasNext()) {
          relocate = true;
          Object rowKey = selection.next();
          palletTable.setRowKey(rowKey);
          JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) palletTable.getRowData();
          Row row = rowData.getRow();
          Object shippingManifestId = row.getAttribute("ShippingManifestId");
          Object customerId = row.getAttribute("CustomerId");
          Object ldcon = row.getAttribute("Manifest");
          val shipManifestIdStr = shippingManifestId.toString();
          log.debug("row {} {} {}", row.toString(),
              shipManifestIdStr,
              customerId.toString());

          //* relocate 1 pallet container to a new pallet container (or floor location) at a time
          //* get label/value pair from relocate to floor location pull down
          String selectedRelocateToLabel =
              getRelocateToLabel(selectedRelocateToValue);
          if (shippingService.relocatePallet(shipManifestIdStr,
              selectedRelocateToValue,
              iUserId)) {

            if (ldcon != null)
              ldcons.add(ldcon.toString());
          }
          else {
            if (errorCount < 1)
              msgOutput.append("An error occurred while relocating floors: ");
            msgOutput.append("floor location ").append(selectedRelocateToLabel);
            errorCount++;
          }
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
      finally {
        palletTable.setRowKey(oldRowKey);
      }
      //* refresh iterator
      OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("ExecuteWithParams");
      op1.execute();

      if (errorCount > 0) {
        //* display message to GUI
        displayMessage(msgOutput);
      }
      else {
        int a = 0;
        for (String ldcon : ldcons) {
          //* reprint manifest to reflect new floor location
          if (a == 0) msgOutput.append("\nThe following shipping manifest (LDCON) must be reprinted:");
          msgOutput.append('\n').append(ldcon);
          a++;
        }

        if (a > 0) {
          //* display message to GUI
          displaySuccessMessage(msgOutput);
        }
        else {
          if (relocate) {
            msgOutput.append("No pallets relocated.");
            displayMessage(msgOutput);
          }
          else {
            msgOutput.append("No pallets selected. Please select a pallet.");
            displayMessage(msgOutput);
          }
        }
      }
    }
    else {
      msgOutput.append("Invalid floor location to relocate to.");
      //* display message to GUI
      displayMessage(msgOutput);
    }
  }

  /**
   * Sets the list used for the floor location pull down
   * binding to the form
   */
  public void setRelocateToNavList(RichSelectOneChoice relocateToNavList) {
    this.relocateToNavList = relocateToNavList;
  }

  /**
   * Returns the list used for the floor location pull down
   * binding to the form
   *
   * @return RichSelectOneChoice
   */
  public RichSelectOneChoice getRelocateToNavList() {
    return relocateToNavList;
  }

  /**
   * This function is used by Shipping Relocate Pallet to create an ADF SelectOneChoice.
   * Returns all empty shipping floors.
   *
   * @return List
   */
  public List<SelectItem> getFloorLocations() {
    // clear the list before sending it out
    floorLocations.clear();
    AllFloorsImpl myfloors = getShippingServiceModule().getAllFloorsPallet();
    myfloors.setEQUIPMENT_NUMBER(JSFUtils.getManagedBeanValue("userbean.workstationId").toString());
    myfloors.setRangeSize(-1);
    myfloors.executeQuery();
    Row[] rows = myfloors.getAllRowsInRange();
    for (Row row : rows) {
      floorLocations.add(new SelectItem(row.getAttribute("FloorLocationId"), row.getAttribute("FloorLocation").toString()));
    }

    return floorLocations;
  }

  public void setFloorLocations(List<SelectItem> input) {
    floorLocations = input;
  }

  /**
   * Sets the table used for display pallets
   * binding to the form
   */
  public void setPalletTable(RichTable palletTable) {
    this.palletTable = palletTable;
  }

  /**
   * Returns the table used for the display pallets
   * binding to the form
   *
   * @return RichTable
   */
  public RichTable getPalletTable() {
    return palletTable;
  }

  /**
   * Returns the associated label of a selected item in the relocate to navigation list
   *
   * @return String
   */
  private String getRelocateToLabel(String value) {
    List<SelectItem> list = floorLocations;
    return getListLabel(value, list);
  }

  /**
   * The main function for manifest a shipment.
   * The floor location and combined local delivery contract number
   * (shortname ldcon) (if available and selected)
   * are sent to the shipping service for database update
   *
   * @param event action event fired by the submit button
   */
  public void submitManifestShipment(@SuppressWarnings("all") ActionEvent event) {
    StringBuilder msgOutput = new StringBuilder();
    if (getManifestFloors().getValue() != null) {
      String manifestFloorId = getManifestFloors().getValue().toString();
      log.debug("manifesting {} - {}", manifestFloorId, iUserId);

      String manifestLDCON = "";

      //* invoke the service to update the data
      ShippingServiceImpl shippingService = getShippingServiceModule();
      List<String> ldcons = shippingService.manifestFloor(getWorkstationId(), manifestFloorId,
          manifestLDCON, iUserId);
      if (CollectionUtils.isNotEmpty(ldcons)) {

        setHasManifestReview(true);

        for (String ldcon : ldcons) {
          //* call on windows print applet
          JSFUtils.setManagedBeanValue("userbean.winPrintManifest", ldcon);
          String url = JSFUtils.getManagedBeanValue("userbean.winPrintURL").toString();
          createPopUpWindows(url);
        }

        //* display message to GUI
        displaySuccessMessage(msgOutput);
        msgOutput.setLength(0);
      }
      else {
        msgOutput.append("Unable to manifest floor id ").append(manifestFloorId);
        //* display message to GUI
        displayMessage(msgOutput);
      }
    }
    else {
      msgOutput.append("Please select a Floor Location");
      displayMessage(msgOutput);
    }

    //* reset manifest form only on successful submit
    resetManifestForm();
  }

  /**
   * This method is used primarily by the Shipping_AACUpdate.jspx to print manifest.
   * It returns a string of HTML.
   */
  public String printManifest(String ldcon, @SuppressWarnings("all") String aac, @SuppressWarnings("all") String leadTcn, @SuppressWarnings("all") String noOfCopies, String autoprint, HttpServletRequest servletRequest) {
    return printManifest(ldcon, aac, leadTcn, noOfCopies, autoprint, servletRequest, getShippingServiceModule(), getStratisUrlContextPath());
  }

  public String printManifest(String ldcon, @SuppressWarnings("all") String aac, @SuppressWarnings("all") String leadTcn,
                              @SuppressWarnings("all") String noOfCopies, String autoprint, HttpServletRequest servletRequest,
                              ShippingServiceImpl shippingService, String stratisUrlContextPath) {
    return shippingService.printManifest(ldcon, autoprint, stratisUrlContextPath);
  }

  /**
   * This method resets the manifest form to its original state,
   * used after the form is submitted successfully.
   */
  private void resetManifestForm() {
    //* reset the floor location list to default
    getManifestFloors().resetValue();

    //* get the iterator used to populate the floor location list
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("ExecuteWithParams");
    op1.execute();
  }

  /**
   * Sets the list used for the floor location pull down
   * binding to the form
   */
  public void setManifestFloors(RichSelectOneChoice manifestFloors) {
    this.manifestFloors = manifestFloors;
  }

  /**
   * Returns the list used for the floor location pull down
   * binding to the form
   *
   * @return RichSelectOneChoice
   */
  public RichSelectOneChoice getManifestFloors() {
    return manifestFloors;
  }

  public void setHasManifestReview(boolean hasManifestReview) {
    this.hasManifestReview = hasManifestReview;
  }

  public boolean getHasManifestReview() {
    return hasManifestReview;
  }

  /**
   * Returns the list to build the floor location pull down
   * binding to the form
   *
   * @return List
   */
  public List<SelectItem> getManifestList() {
    manifestList.clear();
    UnmanifestedFloorsListImpl myfloors = (UnmanifestedFloorsListImpl) getShippingServiceModule().getAllShippingFloorsManifest();
    myfloors.setEQUIPMENT_NUMBER(JSFUtils.getManagedBeanValue("userbean.workstationId").toString());
    myfloors.setRangeSize(-1);
    myfloors.executeQuery();
    Row[] rows = myfloors.getAllRowsInRange();
    for (Row row : rows) {
      manifestList.add(new SelectItem(row.getAttribute("FloorLocationId"), row.getAttribute("FloorLocation").toString()));
    }
    return manifestList;
  }

  /**
   * Sets the list used for the floor location pull down
   * binding to the form
   */
  public void setManifestList(List<SelectItem> input) {
    manifestList = input;
  }

  /**
   * Sets the list used for the combine with pull down
   * binding to the forn
   */
  public void setAllCombineLDCON(List<SelectItem> allCombineLDCON) {
    this.allCombineLDCON = allCombineLDCON;
  }

  /**
   * Returns the list used for the combine with pull down
   * This list is dependent on which floor location is currently selected
   * if none, selected and none available, then displays None Available
   * if none, selected and at least 1 available, then displays Make a Selection
   *
   * @return List
   */
  public List<SelectItem> getAllCombineLDCON() {
    allCombineLDCON = new ArrayList<>();

    //* the combine with list is dependent on a selection from floor location list
    if (getManifestFloors().getValue() != null) {
      String manifestFloor = getManifestFloors().getValue().toString();
      if (StringUtils.isNotEmpty(manifestFloor)) {
        //* invoke the service to fetch the data
        ShippingServiceImpl shippingService = getShippingServiceModule();
        Map<String, String> hm = shippingService.fillCombineWithLDCONLists(getManifestFloors().getValue().toString());

        //* fill the new list (allCombineLDCON)
        if (MapUtils.isNotEmpty(hm)) {
          getCombineLDCONNavList().setUnselectedLabel("<None Available>");
          getCombineLDCONNavList().setRequired(false);
        }
        else {
          getCombineLDCONNavList().setUnselectedLabel("<Make a Selection>");
          for (Map.Entry<String, String> entry : hm.entrySet()) {
            val shippingManifestId = entry.getKey();
            val value = entry.getValue();
            allCombineLDCON.add(new SelectItem(shippingManifestId, value));
          }
        }
      }
    }

    return allCombineLDCON;
  }

  /**
   * Sets the list used for the combine with pull down
   * binding to the form
   */
  public void setCombineLDCONNavList(RichSelectOneChoice combineLDCONNavList) {
    this.combineLDCONNavList = combineLDCONNavList;
  }

  /**
   * Returns the list used for the combine with pull down
   * binding to the form
   *
   * @return RichSelectOneChoice
   */
  public RichSelectOneChoice getCombineLDCONNavList() {
    return combineLDCONNavList;
  }

  /**
   * Sets the table used to display the temporary manifest review
   * binding to the form
   */
  public void setTableManifestReview(RichTable tableManifestReview) {
    this.tableManifestReview = tableManifestReview;
  }

  /**
   * Returns the table used to display the temporary manifest review
   * binding to the form
   *
   * @return RichTable
   */
  @SuppressWarnings("unused") //called from .jspx
  public RichTable getTableManifestReview() {
    return tableManifestReview;
  }

  public void setDownloadCommandButton(RichCommandButton downloadCommandButton) {
    this.downloadCommandButton = downloadCommandButton;
  }

  public RichCommandButton getDownloadCommandButton() {
    return downloadCommandButton;
  }

  public void setDownloadManifest(RichInputText downloadManifest) {
    this.downloadManifest = downloadManifest;
  }

  public RichInputText getDownloadManifest() {
    return downloadManifest;
  }

  public void exportAMSCMOS(ActionEvent event) {
    //get access to the clicked command link
    boolean error = false;
    RichLink comp = (RichLink) event.getComponent();
    //read the added f:attribute value
    String manifest = (String) comp.getAttributes().get("manifest");

    //check that it has been printed
    ShippingServiceImpl shippingService = getShippingServiceModule();
    String checkManifest = shippingService.getShippingManifestIdForLDCON(manifest);
    if (Util.isEmpty(checkManifest)) {
      JSFUtils.addFacesErrorMessage("Shipping Manifest must be printed first.");
      error = true;
    }

    if (!error) {
      RichInputText hiddenManifest = getDownloadManifest();
      hiddenManifest.setValue(manifest);
      FacesContext context = FacesContext.getCurrentInstance();
      ExtendedRenderKitService erks = Service.getService(context.getRenderKit(), ExtendedRenderKitService.class);
      String id = downloadCommandButton.getClientId(context);
      erks.addScript(context, "customHandler(\"" + id + "\");");
    }
  }

  @SuppressWarnings("unused") //called from .jspx
  public void downloadZip(FacesContext context, OutputStream out) {
    String manifest = getDownloadManifest().getValue().toString();
    int workstationId = getWorkstationId();

    ShippingServiceImpl shippingService = getShippingServiceModule();
    String filename = shippingService.exportAMSCMOS(manifest, workstationId, false);
    if (!Util.isEmpty(filename) && !filename.contains("Error")) {
      String path = shippingService.findAMSPath();

      HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
      response.reset();
      response.setContentType("application/x-zip-compressed");
      response.setHeader("Content-Disposition",
          "attachment; filename=\"" + filename + "\"");

      onDownload(path, filename, out);
    }
  }

  public void onDownload(String path, String documentName, OutputStream out) {
    FacesContext fctx = FacesContext.getCurrentInstance();
    File srcdoc = new File(path + documentName);

    if (srcdoc.exists()) {
      byte[] b;

      HttpServletResponse response = (HttpServletResponse) fctx.getExternalContext().getResponse();
      response.reset();
      response.setContentType("application/x-zip-compressed");
      response.setHeader("Content-Disposition",
          "attachment; filename=\"" +
              documentName + "\"");
      response.setContentLength((int) srcdoc.length());

      int n;
      int result;
      try (FileInputStream fis = new FileInputStream(srcdoc)) {
        do {
          n = fis.available();
          b = new byte[n];
          result = fis.read(b);
          out.write(b, 0, b.length);
          if (result == -1) break;
        }
        while (n > 0);
      }
      catch (IOException e) {
        log.trace(e.getLocalizedMessage(), e);
      }
      try {
        out.flush();
      }
      catch (IOException e) {
        AdfLogUtility.logException(e);
      }
    }
  }

  /**
   * The main function for manifest a shipment.
   * The floor location and combined local delivery contract number
   * (shortname ldcon) (if available and selected)
   * are sent to the shipping service for database update
   *
   * @param event action event fired by the submit button
   */
  public void submitManifestShipmentByAac(@SuppressWarnings("all") ActionEvent event) {
    StringBuilder msgOutput = new StringBuilder();
    String manifestFloorLocationId = getManifestAacs().getValue().toString();
    log.debug("manifesting {} -  {}", manifestFloorLocationId, iUserId);

    String manifestLDCON = "";

    //* invoke the service to update the data
    ShippingServiceImpl shippingService = getShippingServiceModule();
    List<String> ldcons = shippingService.manifestFloor(getWorkstationId(), manifestFloorLocationId,
        manifestLDCON, iUserId);
    if (CollectionUtils.isNotEmpty(ldcons)) {

      setHasManifestReview(true);

      for (String ldcon : ldcons) {
        //* call on windows print applet
        JSFUtils.setManagedBeanValue("userbean.winPrintManifest", ldcon);
        String url = JSFUtils.getManagedBeanValue("userbean.winPrintURL").toString();
        createPopUpWindows(url);
      }

      //* display message to GUI
      displaySuccessMessage(msgOutput);
      msgOutput.setLength(0);
    }
    else {
      msgOutput.append("Unable to manifest customer id ").append(manifestFloorLocationId);
      //* display message to GUI
      displayMessage(msgOutput);
    }
  }

  /**
   * Sets the table used to display the manifested containers
   * binding to the form
   */
  public void setAacFloorTable(RichTable aacFloorTable) {
    this.aacFloorTable = aacFloorTable;
  }

  /**
   * Returns the table used to display the manifested containers
   * binding to the form
   *
   * @return RichTable
   */
  public RichTable getAacFloorTable() {
    return aacFloorTable;
  }

  /**
   * Sets the list used for the customer pull down
   * binding to the form
   */
  public void setManifestAacs(RichSelectOneChoice manifestAacs) {
    this.manifestAacs = manifestAacs;
  }

  /**
   * Returns the list used for the customer pull down
   * binding to the form
   *
   * @return RichSelectOneChoice
   */
  public RichSelectOneChoice getManifestAacs() {
    return manifestAacs;
  }

  /**
   * Returns the list used for the manifest AAC pull down
   * binding to the form
   *
   * @return List
   */
  public List<SelectItem> getManifestAACList() {
    manifestAACList.clear();
    UnmanifestedAacsListImpl myfloors = (UnmanifestedAacsListImpl) getShippingServiceModule().getAllShippingAacs_Manifest();
    myfloors.setEQUIPMENT_NUMBER(JSFUtils.getManagedBeanValue("userbean.workstationId").toString());
    myfloors.setRangeSize(-1);
    myfloors.executeQuery();
    Row[] rows = myfloors.getAllRowsInRange();
    for (Row row : rows) {
      manifestAACList.add(new SelectItem(row.getAttribute("FloorLocationId"), row.getAttribute("Aac") + " - " + row.getAttribute("FloorLocation")));
    }
    return manifestAACList;
  }

  /**
   * Sets the list used for the manifest AAC pull down
   * binding to the form
   */
  public void setManifestAACList(List<SelectItem> input) {
    manifestAACList = input;
  }

  public void submitPrintManifest(ActionEvent actionEvent) {

    log.debug("printing  {}", actionEvent.toString());
  }

  /**
   * This method is used to update the iterators reporting on the shipping home page.
   * <p>
   * NOTE:  Cannot use more than one poll per jspx
   */
  public void pollActiveSessions(@SuppressWarnings("all") PollEvent pollEvent) {
  }

  /**
   * This method creates a pop up window for a given URL
   */
  void createPopUpWindows(String url) {
    FacesContext fctx = FacesContext.getCurrentInstance();
    ExtendedRenderKitService erks = Service.getRenderKitService(fctx, ExtendedRenderKitService.class);
    erks.addScript(fctx, "window.open(\"" + url + "\");");
  }
}
