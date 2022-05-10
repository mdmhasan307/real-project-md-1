package mil.stratis.view.inventory;

import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.user.UserInfo;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.infrastructure.util.UserSiteSelectionTracker;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputDate;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectBooleanCheckbox;
import oracle.adf.view.rich.component.rich.input.RichSelectManyShuttle;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.binding.OperationBinding;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.event.PollEvent;
import org.apache.myfaces.trinidad.model.RowKeySet;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class InventoryMgmtBacking extends AdminBackingHandler {

  private List selectedValues = new ArrayList();
  private List allItems = new ArrayList();
  private List selectedNIINValues = new ArrayList();
  private List allNIINItems = new ArrayList();
  private List currentList = new ArrayList();
  transient Object InventorybuttonState = 0;
  transient Object InventoryscreenState = 0;

  private transient RichInputText startniintext, endniintext, startloctext, endloctext;
  private transient RichInputDate byDateExpirationDate;
  private transient RichInputText txtByStartLocation;
  private transient RichInputText txtByEndLocation;
  private transient RichSelectManyShuttle shuttleLastCounted;
  private transient RichSelectManyShuttle shuttleExpiration;
  private transient RichSelectManyShuttle shuttleNIINs;
  private transient RichSelectManyShuttle shuttleWACs;

  //* Variables for Inventory Review
  private transient Object InventoryreleasebuttonState = 0;

  private int iUserId;
  private int iWorkstationId;

  private boolean pollFlag = false;
  private String pollMessage = "";
  private transient RichTable inventoryTable;
  private transient RichTable inventoryItemTable;
  private transient RichSelectBooleanCheckbox chkLast360;

  // Injected user info bean

  private UserInfo userInfo;

  public InventoryMgmtBacking() {

    Object id = JSFUtils.getManagedBeanValue("userbean.userId");
    if (id != null) {
      try {
        //* Convert the id object to an int
        iUserId = Integer.parseInt(id.toString());
      }
      catch (Exception e) {
        iUserId = 0;
      }
    }

    Object wid = JSFUtils.getManagedBeanValue("userbean.workstationId");
    if (wid != null) {
      try {
        //* Convert the id object to an int
        iWorkstationId = Integer.parseInt(wid.toString());
      }
      catch (Exception e) {
        iWorkstationId = 0;
      }
    }
    log.trace("user id: {}", iUserId);
    log.trace("workstation id: {}", iWorkstationId);
  }

  // Get the user info bean through injection.

  public void setUserInfo(UserInfo bean) {
    userInfo = bean;
    iUserId = userInfo.getUserId();
    iWorkstationId = userInfo.getWorkstationId();
    log.trace("user id: {}", iUserId);
    log.trace("workstation id: {}", iWorkstationId);
  }

  public void setUserId(int id) {
    log.trace("workstation id: {}", id);
    iUserId = id;
  }

  public void setWorkstationId(int id) {
    log.trace("workstation id: {}", id);
    iWorkstationId = id;
  }

  /**
   * This function was added to handle manual refresh.  The table will not
   * keep creating new rows while there is already a new row which is unsaved.
   * The function is used only to call the ExecuteQuery on the inventory iterator;
   * it does not set any variables used by system.
   */
  public void setInventoryRefresh(boolean inventoryRefresh) {
    if (inventoryRefresh) {}

    //* will clean up dirty created rows - also from a manual refresh
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("Execute");
    op1.execute();

    OperationBinding op2 = ADFUtils.getDCBindingContainer().getOperationBinding("ExecuteItem");
    op2.execute();
  }

  public boolean getInventoryRefresh() { return false; }

  /**
   * function to check search for locs to fill the search list.
   */
  @SuppressWarnings("unused") //called from Admin_InventorySchedule.jspx
  public void findLOCMatches(@SuppressWarnings("all") ActionEvent event) {
    // ensure that all needed fields are populated
    if ((startloctext.getValue() != null) &&
        (endloctext.getValue() != null)) {

      String start = Util.trimUpperCaseClean(startloctext.getValue());
      String end = Util.trimUpperCaseClean(endloctext.getValue());
      if (start.length() != 9 || end.length() != 9) {
        if (start.length() != 9) JSFUtils.addFacesErrorMessage("Start Location", "Must be 9 characters");
        if (end.length() != 9) JSFUtils.addFacesErrorMessage("End Location", "Must be 9 characters");
        return;
      }

      if (RegUtils.isNotAlphaNumeric(start) || RegUtils.isNotAlphaNumeric(end)) {
        JSFUtils.addFacesErrorMessage("Start/End Location", "Must be 9 alphanumeric characters");
        return;
      }
      getInventorySetupService().addLocationSurveyList(startloctext.getValue().toString().trim().toUpperCase(),
          endloctext.getValue().toString().trim().toUpperCase());

      // clear the values
      startloctext.setValue(null);
      endloctext.setValue(null);
    }
  }

  public void submitCreateLocSurveyRecord(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    int result =
        getInventorySetupService().createLocSurveyRecords(iUserId, false, UserSiteSelectionTracker.getUserSiteSelection());
    if (result < 0) {
      // display an error
    }
    getInventorySetupService().clearLocSurveyRecords();
    //* refresh the inventory management home page
    setInventoryRefresh(true);
  }

  public void submitCreateLocSurveyRecords(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    int result =
        getInventorySetupService().createLocSurveyRecords(iUserId, true, UserSiteSelectionTracker.getUserSiteSelection());
    if (result < 0) {
      // display an error
    }
    getInventorySetupService().clearLocSurveyRecords();
    //* refresh the inventory management home page
    setInventoryRefresh(true);
  }

  public void submitClearLocSurveyRecords(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    getInventorySetupService().clearLocSurveyRecords();
    //* refresh the inventory management home page
    setInventoryRefresh(true);
  }

  public void submitCreateInventoryByDate(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    if (getByDateExpirationDate().getValue() != null) {
      int result =
          getInventorySetupService().createInventoryByDate(getByDateExpirationDate().getValue(),
              iUserId, UserSiteSelectionTracker.getUserSiteSelection());
      if (result < 1) {
        //* display error
      }
    }
    else {
      //* display error
    }
    //* refresh the inventory management home page
    setInventoryRefresh(true);
  }

  /**
   * function to build inventories based on when the item will expire.
   * NOTE: this is called when an action on the page takes place NOT when values are moved
   */
  public void refreshExpireSelectedList(ValueChangeEvent event) {
    if (event.getNewValue() == null)
      return;
    // send the list to be update in the current row
    List newinventory = (List) event.getNewValue();
    if (newinventory.size() < 1) {
      return;
    }
    currentList = newinventory;
  }

  public void submitCreateInventoryBySelectedDates(ActionEvent event) {
    if (currentList.size() >= 1) {
      int result =
          getInventorySetupService().addInventoryExpirationDays(currentList,
              iUserId, UserSiteSelectionTracker.getUserSiteSelection());
      if (result < 1) {
        JSFUtils.addFacesErrorMessage("ERROR: ", "There was an issue saving the inventory");
        log.warn("Failed to save Inventory by expiration date");
      }
    }
    getShuttleExpiration().resetValue();
    //* refresh the inventory management home page
    setInventoryRefresh(true);
  }

  //** End Create Inventory - By Expiration Date

  //** Create Inventory - By Range Locations

  public void submitByRangeLocations(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    // ensure that all needed fields are populated
    if ((getTxtByStartLocation().getValue() != null) &&
        (getTxtByEndLocation().getValue() != null)) {

      String start = getTxtByStartLocation().getValue().toString().trim().toUpperCase();
      String end = getTxtByEndLocation().getValue().toString().trim().toUpperCase();
      if (start.length() != 9 || end.length() != 9) {
        if (start.length() != 9) JSFUtils.addFacesErrorMessage("Start Location", "Must be 9 characters");
        if (end.length() != 9) JSFUtils.addFacesErrorMessage("End Location", "Must be 9 characters");
        return;
      }

      if (RegUtils.isNotAlphaNumeric(start) || RegUtils.isNotAlphaNumeric(end)) {
        JSFUtils.addFacesErrorMessage("Start/End Location", "Must be 9 alphanumeric characters");
        return;
      }

      getInventorySetupService().createInventoryByRangeLocations(getTxtByStartLocation().getValue().toString().trim().toUpperCase(),
          getTxtByEndLocation().getValue().toString().trim().toUpperCase(),
          iUserId, UserSiteSelectionTracker.getUserSiteSelection());

      // clear the values
      getTxtByStartLocation().setValue(null);
      getTxtByEndLocation().setValue(null);

      //* refresh the inventory management home page
      setInventoryRefresh(true);
    }
  }

  public void setSelectedValues(List selectedValues) {
    this.selectedValues = selectedValues;
  }

  public List getSelectedValues() {
    return selectedValues;
  }

  public void setAllItems(List allItems) {
    this.allItems = allItems;
  }

  public List getAllItems() {
    // create lists to fill from the service
    List WACName = new ArrayList(), WACID = new ArrayList();
    getInventorySetupService().fillWACLists(WACName, WACID);

    // clear the list first, then fill
    allItems.clear();
    for (int i = 0; i < WACName.size(); i++) {
      allItems.add(new SelectItem(WACID.get(i),
          WACName.get(i).toString()));
    }

    return allItems;
  }

  /**
   * NOTE: this is called when an action on the page takes place NOT when values are moved
   */
  public void refreshSelectedList(ValueChangeEvent event) {

    // if we are going to change this
    if (this.InventorybuttonState.toString().equals("2")) {
      // send the list to be update in the current row
      List newinventory = (List) event.getNewValue();
      if (newinventory.size() < 1) {
        return;
      }
      currentList = newinventory;
    }
  }

  public void createWacInventory(ActionEvent e) {
    if (this.InventorybuttonState.toString().equals("2")) {
      boolean last360 = getChkLast360().isSelected();
      // call the service to add all the locations in the given wacs to the inventory
      int result = getInventorySetupService().addInventoryWACs(currentList, last360, iUserId, UserSiteSelectionTracker.getUserSiteSelection());

      if (result < 1) {
        JSFUtils.addFacesErrorMessage("ERROR: ", "There was an issue saving the inventory");
        log.warn("Failed to save Inventory by wac");
      }

      // clear the selected list
      this.selectedValues.clear();
      getShuttleWACs().resetValue();

      //* refresh the inventory management home page
      setInventoryRefresh(true);
    }
  }

  public void setSelectedNIINValues(List selectedNIINValues) {
    this.selectedNIINValues = selectedNIINValues;
  }

  public List getSelectedNIINValues() {
    return selectedNIINValues;
  }

  public void setAllNIINItems(List allNIINItems) {
    this.allNIINItems = allNIINItems;
  }

  public List getAllNIINItems() {
    // clear the list before use, then fill
    allNIINItems.clear();
    List Name = new ArrayList(), Id = new ArrayList();
    getInventorySetupService().returnNIINFilterList(Name, Id);

    for (int i = 0; i < Name.size(); i++) {
      allNIINItems.add(new SelectItem(Id.get(i),
          Name.get(i).toString()));
    }

    return allNIINItems;
  }

  /**
   * NOTE: this is called when an action on the page takes place NOT when values are moved
   */
  public void refreshNIINSelectedList(ValueChangeEvent event) {

    if (this.InventorybuttonState.toString().equals("1")) {
      // send the list to be update in the current row
      List newinventory = (List) event.getNewValue();
      if (newinventory.size() < 1) {
        return;
      }
      currentList = newinventory;
    }
  }

  public void createNIINInventory(ActionEvent event) {
    String cc = "*";
    // call the service to add all the locations in the given wacs to the inventory
    int result =
        getInventorySetupService().createNIINInventories(currentList, iUserId, UserSiteSelectionTracker.getUserSiteSelection());
    if (result < 1) {
      JSFUtils.addFacesErrorMessage("ERROR: ", "There was an issue saving the inventory");
      log.warn("Failed to save inventory by niin");
    }
    else {

      startniintext.setValue(null);
      endniintext.setValue(null);
    }

    // clear the selected list
    this.selectedNIINValues.clear();
    getShuttleNIINs().resetValue();

    //* refresh the inventory management home page
    setInventoryRefresh(true);
  }

  // function to check search for niins to fill the search list

  @SuppressWarnings("unused") //called from Admin_InventorySchedule.jspx
  public void findNIINMatches(@SuppressWarnings("all") ActionEvent event) {
    getShuttleNIINs().resetValue();
    // ensure that all needed fields are popuplated
    if ((startniintext.getValue() != null) && (endniintext.getValue() != null)) {

      String start = Util.trimUpperCaseClean(startniintext.getValue());
      String end = Util.trimUpperCaseClean(endniintext.getValue());

      if (RegUtils.isAlphaNumeric(start) && RegUtils.isAlphaNumeric(end)) {
        String cc = "*";
        getInventorySetupService().filterNIINList(startniintext.getValue().toString(), endniintext.getValue().toString(), cc);
      }
    }
  }

  /**
   * NOTE: this is called when an action on the page takes place NOT when values are moved
   */
  public void refreshCountSelectedList(ValueChangeEvent event) {
    if (event.getNewValue() == null)
      return;

    // send the list to be update in the current row
    List newinventory = (List) event.getNewValue();
    if (newinventory.size() < 1) {
      //* return error
      return;
    }
    currentList = newinventory;
  }

  @SuppressWarnings("unused") //called from Admin_InventorySchedule.jspx
  public void createLastCountInventory(ActionEvent e) {
    int result =
        getInventorySetupService().createCountInventory(currentList,
            iUserId, UserSiteSelectionTracker.getUserSiteSelection());
    if (result < 1) {
      JSFUtils.addFacesErrorMessage("ERROR: ", "There was an issue saving the inventory");
      log.warn("Failed to save Inventory by wac");
    }
    else {
      getShuttleLastCounted().resetValue();
    }

    //* refresh the inventory management home page
    setInventoryRefresh(true);
  }

  public void setInventorybuttonState(Object inventorybuttonState) {
    this.InventorybuttonState = inventorybuttonState;
  }

  public Object getInventorybuttonState() {
    return InventorybuttonState;
  }

  public void setInventoryscreenState(Object inventoryScreenState) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put("inventoryScreenState",
        inventoryScreenState);
    this.InventoryscreenState = inventoryScreenState;
  }

  public Object getInventoryscreenState() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get("inventoryScreenState");
    if (obj != null)
      InventoryscreenState = obj.toString();
    return InventoryscreenState;
  }

  public void modifyInventoryState(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    setInventoryscreenState(0);
  }

  public void surveyInventoryState(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    setInventoryscreenState(1);
  }

  public void createInventoryState(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    setInventoryscreenState(2);
  }

  public void dialogListener(DialogEvent dialogEvent) {
    String result = "";
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      //submitDeleteWAC(null);
      result = getInventorySetupService().deleteSelectedInventory(iUserId);
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      // Clear selected row from the table (defaults to the first row)
      // This prevents an issue when the first row is RUNNING
      // but the cancel button is not properly updated
      RowKeySet rowKeys = inventoryTable.getSelectedRowKeys();
      rowKeys.clear();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(inventoryTable);
      if (!result.equals("ERROR")) {
        JSFUtils.addFacesInformationMessage("Deleted: " + result);
      }
      else {
        JSFUtils.addFacesErrorMessage("An error has occurred.");
      }
    }
  }

  @SuppressWarnings("unused") //called from Admin_InventoryManagement.jspx
  public void dialogListener2(DialogEvent dialogEvent) {
    String result = "";
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      result = getInventorySetupService().deleteSelectedInventoryItem(iUserId);
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete2");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(inventoryItemTable);
      if (!result.equals("ERROR")) {
        JSFUtils.addFacesInformationMessage("Deleted Inventory Item " + result);
      }
      else {
        JSFUtils.addFacesErrorMessage("An error has occurred.");
      }
    }
  }

  public void setInventoryreleasebuttonState(Object inventoryreleasebuttonState) {
    this.InventoryreleasebuttonState = inventoryreleasebuttonState;
  }

  public boolean getInventoryreleasebuttonState() {
    return (getInventorySetupService().getInventoryCompletedType());
  }

  public void submitInventoryItemToHost(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    int result =
        getInventorySetupService().submitInventoryItemToHost(iUserId);
    if (result < 0) {
      if (result == -9)
        JSFUtils.addFacesErrorMessage("Please complete the pending picks of this NIIN before trying to release this inventory.");
    }
  }

  public void submitInventoryItemForRecount(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    int result =
        getInventorySetupService().submitInventoryItemForRecount(iUserId);
    if (result < 0) {
      // display an error
    }
  }

  public void submitCreateInventoryForSelectedInventoryItem(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    int result =
        getInventorySetupService().submitCreateInventoryForSelectedInventoryItem(iUserId);
    if (result < 0) {
      // display an error
    }
  }

  public void submitRemoveSurvey(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    int result =
        getInventorySetupService().submitInventoryItemRemoveSurvey(iUserId);
    if (result < 0) {
      // display an error
    }
  }

  public void setPollFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put("inventoryPollFlag", show);
    this.pollFlag = show;
  }

  public boolean getPollFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = pollFlag;
    Object obj = afContext.getPageFlowScope().get("inventoryPollFlag");
    if (obj != null)
      flag = Boolean.valueOf(obj.toString());

    return pollFlag || flag;
  }

  public void setPollMessage(String msg) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put("inventoryPollMessage", msg);
    this.pollMessage = msg;
  }

  public String getPollMessage() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get("inventoryPollMessage");
    if (obj != null)
      pollMessage = obj.toString();

    return pollMessage;
  }

  public void refreshInventoryItems(ActionEvent event) {
    DCIteratorBinding inventoryIter = ADFUtils.findIterator("InventoryItemFilterView1Iterator");
    if (inventoryIter != null) {
      inventoryIter.executeQuery();
      log.debug("refreshInventoryItems just called executeQuery");
    }
  }

  public void pollRunningInventory(PollEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    log.debug("polling inventory management");
    DCIteratorBinding inventoryIter = ADFUtils.findIterator("InventoryView1Iterator");
    refreshIteratorKeepPosition(inventoryIter, getInventoryTable());

    // execute the inventory item query also
    DCIteratorBinding inventoryItemIter = ADFUtils.findIterator("InventoryItemView2Iterator");
    refreshIteratorKeepPosition(inventoryItemIter, getInventoryItemTable());
  }

  public void setStartloctext(RichInputText startloctext) {
    this.startloctext = startloctext;
  }

  public RichInputText getStartloctext() {
    return startloctext;
  }

  public void setEndloctext(RichInputText endloctext) {
    this.endloctext = endloctext;
  }

  public RichInputText getEndloctext() {
    return endloctext;
  }

  public void setByDateExpirationDate(RichInputDate byDateExpirationDate) {
    this.byDateExpirationDate = byDateExpirationDate;
  }

  public RichInputDate getByDateExpirationDate() {
    return byDateExpirationDate;
  }

  public Object getInventoryreleasebuttonState1() {
    return InventoryreleasebuttonState;
  }

  public int getIUserId() {
    return iUserId;
  }

  public int getIWorkstationId() {
    return iWorkstationId;
  }

  public boolean isPollFlag() {
    return pollFlag;
  }

  public UserInfo getUserInfo() {
    return userInfo;
  }

  public void setShuttleExpiration(RichSelectManyShuttle shuttleExpiration) {
    this.shuttleExpiration = shuttleExpiration;
  }

  public RichSelectManyShuttle getShuttleExpiration() {
    return shuttleExpiration;
  }

  public void setTxtByStartLocation(RichInputText txtByStartLocation) {
    this.txtByStartLocation = txtByStartLocation;
  }

  public RichInputText getTxtByStartLocation() {
    return txtByStartLocation;
  }

  public void setTxtByEndLocation(RichInputText txtByEndLocation) {
    this.txtByEndLocation = txtByEndLocation;
  }

  public RichInputText getTxtByEndLocation() {
    return txtByEndLocation;
  }

  public void setShuttleWACs(RichSelectManyShuttle shuttleWACs) {
    this.shuttleWACs = shuttleWACs;
  }

  public RichSelectManyShuttle getShuttleWACs() {
    return shuttleWACs;
  }

  public void setStartniintext(RichInputText startniintext) {
    this.startniintext = startniintext;
  }

  public RichInputText getStartniintext() {
    return startniintext;
  }

  public void setEndniintext(RichInputText endniintext) {
    this.endniintext = endniintext;
  }

  public RichInputText getEndniintext() {
    return endniintext;
  }

  public void setShuttleLastCounted(RichSelectManyShuttle shuttleLastCounted) {
    this.shuttleLastCounted = shuttleLastCounted;
  }

  public RichSelectManyShuttle getShuttleLastCounted() {
    return shuttleLastCounted;
  }

  public void setShuttleNIINs(RichSelectManyShuttle shuttleNIINs) {
    this.shuttleNIINs = shuttleNIINs;
  }

  public RichSelectManyShuttle getShuttleNIINs() {
    return shuttleNIINs;
  }

  public void setInventoryTable(RichTable inventoryTable) {
    this.inventoryTable = inventoryTable;
  }

  public RichTable getInventoryTable() {
    return inventoryTable;
  }

  public void setInventoryItemTable(RichTable inventoryItemTable) {
    this.inventoryItemTable = inventoryItemTable;
  }

  public RichTable getInventoryItemTable() {
    return inventoryItemTable;
  }

  public void setChkLast360(RichSelectBooleanCheckbox chkLast360) {
    this.chkLast360 = chkLast360;
  }

  public RichSelectBooleanCheckbox getChkLast360() {
    return chkLast360;
  }
}
