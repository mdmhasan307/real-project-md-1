package mil.stratis.view.admin.warehouse;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.WarehouseSetupImpl;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.ELUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.binding.OperationBinding;
import oracle.jbo.Row;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.event.RangeChangeEvent;

import javax.faces.event.ActionEvent;
import java.util.Iterator;

@Slf4j
public class BuildingSetupBacking extends AdminBackingHandler {

  private static final String BUILDING_ITERATOR = "WarehouseView1Iterator";
  public static final String BUILDING_FLAG = "buildingFlag";

  private transient RichTable buildingTable;
  private transient RichInputText buildingNumber;
  private transient RichInputText buildingComplex;
  private transient RichInputText buildingDescription;
  private transient RichSelectOneChoice buildingSite;
  private transient RichInputText buildingLDCONPrefix;
  private transient RichInputText buildingLDCONSuffix;
  private transient RichInputText buildingCarrierName;
  private boolean buildingFlag = false;
  private boolean buildingInUseFlag = false;
  private boolean newFlag = false;
  private String buildingDefaultFloors = "";
  private int buildingDefaultFloorsCOLS = 3;
  private boolean buildingRefresh = false;

  public BuildingSetupBacking() {
    super();

    //* required to show initial route update screen with grey, read only and buttons undisabled
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setInitialBuildingFlag();
    }
  }

  @SuppressWarnings("unused")
  public void submitResetBuilding(ActionEvent event) {
    resetFocusBuilding();
    resetKeepPosition(BUILDING_ITERATOR);
    setBuildingFlag(true);
    newFlag = false;
  }

  @SuppressWarnings("unused")
  public void submitCancelBuilding(ActionEvent event) {
    resetFocusBuilding();
    cancel(BUILDING_ITERATOR);
    setBuildingFlag(false);
    newFlag = false;
  }

  @SuppressWarnings("unused")
  public void submitUpdateBuilding(ActionEvent event) {

    resetFocusBuilding();
    setBuildingFlag(true);
    newFlag = false;
  }

  @SuppressWarnings("unused")
  public void submitSaveBuilding(ActionEvent event) {

    resetFocusBuilding();
    boolean error = false;
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (isEmpty(getBuildingNumber())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Building is required.");
      setFocus(getBuildingNumber());
      error = true;
    }

    if (isEmpty(getBuildingLDCONPrefix())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "LDCON Prefix is required.");
      setFocus(getBuildingLDCONPrefix());
      error = true;
    }

    WarehouseSetupImpl service = getWarehouseSetupModule();
    if ((service.checkBuilding(Util.trimUpperCaseClean(getBuildingNumber().getValue())) > 0) && newFlag) {
      JSFUtils.addFacesErrorMessage("DUPLICATE FIELD", "Building already exists.");
      setFocus(getBuildingNumber());
      error = true;
    }

    if (error)
      return;
    //* handle case requirements
    String buildingNum = Util.trimUpperCaseClean(getBuildingNumber().getValue());
    String prefix = Util.trimUpperCaseClean(getBuildingLDCONPrefix().getValue());
    String complex = Util.trimUpperCaseClean(getBuildingComplex().getValue());
    String carrierName = Util.trimUpperCaseClean(getBuildingCarrierName().getValue());
    String desc = Util.trimUpperCaseClean(getBuildingDescription().getValue());

    ELUtils.set("#{bindings.Building1.inputValue}", buildingNum);
    ELUtils.set("#{bindings.LocalDeliveryPrefix1.inputValue}", prefix);
    ELUtils.set("#{bindings.Complex1.inputValue}", complex);
    ELUtils.set("#{bindings.CarrierName1.inputValue}", carrierName);
    ELUtils.set("#{bindings.WarehouseView1Description.inputValue}", desc);

    if (RegUtils.isNotAlphaNumeric(buildingNum)) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "Building Number must be alphanumeric characters only");
      setFocus(getBuildingNumber());
      error = true;
    }
    if (RegUtils.isNotAlphaNumeric(prefix)) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "Local Delivery Prefix must be alphanumeric characters only");
      setFocus(getBuildingLDCONPrefix());
      error = true;
    }
    if (RegUtils.isNotAlphaNumeric(complex)) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "Building Complex must be alphanumeric characters only");
      setFocus(getBuildingComplex());
      error = true;
    }

    if (isEmpty(getBuildingSite())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Building Site is required.");
      setFocus(getBuildingSite());
      error = true;
    }

    if (error)
      return;

    String function = "Building";
    String dupFields = "building";
    try {
      saveIteratorKeepPosition(BUILDING_ITERATOR, function, dupFields, getBuildingTable(), false);
      setBuildingFlag(false);
      newFlag = false;
    }
    catch (Exception e) {
      setBuildingFlag(true);
    }
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      submitDeleteBuilding(null);
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(buildingTable);
    }
  }

  @SuppressWarnings("unused")
  public void submitDeleteBuilding(ActionEvent event) {

    resetFocusBuilding();
    String deleteKey = "";

    Object oldRowKey = buildingTable.getRowKey();
    Iterator<Object> selection = buildingTable.getSelectedRowKeys().iterator();
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        buildingTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) buildingTable.getRowData();
        Row r = rowData.getRow();
        if (r.getAttribute("WarehouseId") != null)
          deleteKey = r.getAttribute("WarehouseId").toString();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      buildingTable.setRowKey(oldRowKey);
    }

    if (deleteKey == null || deleteKey.equals("")) {
      displayMessage("[ERROR] Unknown Record for deletion.");
    }
    else {
      try {
        int id = Integer.parseInt(deleteKey);
        String message;
        WarehouseSetupImpl service = getWarehouseSetupModule();
        message = service.deleteBuilding(id);
        if (message.equals("")) {
          //* get the iterator used to populate the container table
          DCIteratorBinding iter = ADFUtils.findIterator(BUILDING_ITERATOR);
          iter.executeQuery();
          displaySuccessMessage("Building deleted.");
        }
        else {
          JSFUtils.addFacesInformationMessage(message);
        }
      }
      catch (Exception e) {
        displayMessage("[ERROR] Error occurred while trying to delete ");
      }
    }
  }

  @SuppressWarnings("unused")
  public void submitCreateBuilding(ActionEvent actionEvent) {
    resetFocusBuilding();
    cancel(BUILDING_ITERATOR);

    //* will clean up dirty created rows - also from a manual refresh
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("CreateInsert");
    op1.execute();
  }

  public void resetFocusBuilding() {
    getBuildingNumber().setInlineStyle(uppercase);
    getBuildingLDCONPrefix().setInlineStyle(uppercase);
    getBuildingComplex().setInlineStyle(uppercase);
    getBuildingDescription().setInlineStyle(uppercase);

    getBuildingCarrierName().setInlineStyle("");
  }

  @SuppressWarnings("unused")
  public void buildingRangeChanged(RangeChangeEvent rangeChangeEvent) {
    // get the first table row to obtain the row string
    JUCtrlValueBindingRef ctrlValuDef = (JUCtrlValueBindingRef) getBuildingTable().getRowData(rangeChangeEvent.getNewStart());
    Row firstRow = ctrlValuDef.getRow();

    updateTableRangeChange(firstRow, BUILDING_ITERATOR);
    RequestContext.getCurrentInstance().addPartialTarget(getBuildingTable());
  }

  /**
   * This function was added to handle user's manual refresh.  The table will not
   * keep creating new rows while there is already a new row which is unsaved.
   * The function is used only to call the ExecuteQuery on the iterator;
   * it does not set any variables used by system.
   */
  @SuppressWarnings("unused")
  public void setBuildingRefresh(boolean refresh) {
    //* will clean up dirty created rows - also from a manual refresh
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("ExecuteBuilding");
    op1.execute();
  }

  public final void setInitialBuildingFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get(BUILDING_FLAG);
    if (obj == null) {
      setBuildingFlag(false);
      newFlag = false;
    }
  }

  /**
   * Set building flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Warehouse Setup - Building.
   */
  public void setBuildingFlag(boolean show) {

    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put(BUILDING_FLAG, show);
    this.buildingFlag = show;
  }

  @SuppressWarnings("unused") //called from .jspx
  public boolean getBuildingFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = buildingFlag;
    Object obj = afContext.getPageFlowScope().get(BUILDING_FLAG);
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return buildingFlag || flag;
  }

  public void setNewFlag(boolean show) {
    this.newFlag = show;
  }

  public boolean getNewFlag() {
    return newFlag;
  }

  public void setBuildingTable(RichTable buildingTable) {
    this.buildingTable = buildingTable;
  }

  public RichTable getBuildingTable() {
    return buildingTable;
  }

  public void setBuildingNumber(RichInputText buildingNumber) {
    this.buildingNumber = buildingNumber;
  }

  public RichInputText getBuildingNumber() {
    return buildingNumber;
  }

  public void setBuildingComplex(RichInputText buildingComplex) {
    this.buildingComplex = buildingComplex;
  }

  public RichInputText getBuildingComplex() {
    return buildingComplex;
  }

  public void setBuildingDescription(RichInputText buildingDescription) {
    this.buildingDescription = buildingDescription;
  }

  public RichInputText getBuildingDescription() {
    return buildingDescription;
  }

  public void setBuildingSite(RichSelectOneChoice buildingSite) {
    this.buildingSite = buildingSite;
  }

  public RichSelectOneChoice getBuildingSite() {
    return buildingSite;
  }

  public void setBuildingLDCONPrefix(RichInputText buildingLDCONPrefix) {
    this.buildingLDCONPrefix = buildingLDCONPrefix;
  }

  public RichInputText getBuildingLDCONPrefix() {
    return buildingLDCONPrefix;
  }

  public void setBuildingLDCONSuffix(RichInputText buildingLDCONSuffix) {
    this.buildingLDCONSuffix = buildingLDCONSuffix;
  }

  public RichInputText getBuildingLDCONSuffix() {
    return buildingLDCONSuffix;
  }

  public void setBuildingCarrierName(RichInputText buildingCarrierName) {
    this.buildingCarrierName = buildingCarrierName;
  }

  public RichInputText getBuildingCarrierName() {
    return buildingCarrierName;
  }

  public boolean isBuildingFlag() {
    return buildingFlag;
  }

  public boolean isNewFlag() {
    return getNewFlag();
  }

  public void setBuildingInUseFlag(boolean buildingInUseFlag) {
    this.buildingInUseFlag = buildingInUseFlag;
  }

  @SuppressWarnings("unused") //called from .jspx
  public boolean isBuildingInUseFlag() {
    return buildingInUseFlag;
  }

  public void setBuildingDefaultFloors(String buildingDefaultFloors) {
    this.buildingDefaultFloors = buildingDefaultFloors;
  }

  public String getBuildingDefaultFloors() {
    return buildingDefaultFloors;
  }

  public void setBuildingDefaultFloorsCOLS(int buildingDefaultFloorsCOLS) {
    this.buildingDefaultFloorsCOLS = buildingDefaultFloorsCOLS;
  }

  public int getBuildingDefaultFloorsCOLS() {
    return buildingDefaultFloorsCOLS;
  }

  public void setBuildingRefresh1(boolean buildingRefresh) {
    this.buildingRefresh = buildingRefresh;
  }

  public boolean isBuildingRefresh() {
    return buildingRefresh;
  }
}
