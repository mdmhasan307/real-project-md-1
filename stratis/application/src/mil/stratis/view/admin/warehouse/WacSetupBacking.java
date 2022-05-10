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

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class WacSetupBacking extends AdminBackingHandler {

  private static final int MIN_LEN_WAC_NUMBER = 3;
  private static final int MIN_LEN_WAC_PACKAREA = 4;
  private static final String WAC_ITERATOR = "WacTableView1Iterator";
  public static final String WAC_FLAG = "wacFlag";

  private transient RichTable wacTable;
  private transient RichInputText wacNumber;
  private transient RichSelectOneChoice wacWarehouse;
  private transient RichSelectOneChoice wacMechanizedFlag;
  private transient RichSelectOneChoice wacSecureFlag;
  private transient RichInputText wacCarouselController;
  private transient RichInputText wacCarouselOffset;
  private transient RichSelectOneChoice wacCarouselModel;
  private transient RichInputText wacPackArea;
  private transient RichInputText wacTasksPerTrip;
  private transient RichInputText wacSidsPerTrip;
  private boolean wacFlag = false;
  private boolean wacRefresh = false;
  private boolean wacNewFlag = false;
  private transient Object sessionCarouselModel = null;
  private transient Object sessionSecureFlag = null;
  private List<SelectItem> wacSecureFlagList = new ArrayList<>();
  private List<SelectItem> wacCarouselModelList = new ArrayList<>();

  private transient RichSelectOneChoice wacSecureFlagNavList;
  private List<SelectItem> wacNumberByBuildingList = new ArrayList<>(); // This gets dynamically refreshed.

  public WacSetupBacking() {
    super();
    //* required to show initial route update screen with grey, read only and buttons undisabled
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setInitialWacFlag();
    }
  }

  @SuppressWarnings("unused")
  public void submitResetWAC(ActionEvent event) {
    resetFocusWac();
    resetKeepPosition(WAC_ITERATOR);
    setWacFlag(false);
  }

  @SuppressWarnings("unused")
  public void submitCancelWAC(ActionEvent event) {
    resetFocusWac();
    cancel(WAC_ITERATOR);
    setWacFlag(true);
    setWacNewFlag(false);
  }

  @SuppressWarnings("unused")
  public void submitUpdateWAC(ActionEvent event) {
    resetFocusWac();
    setWacFlag(false);
    setWacNewFlag(false);
  }

  @SuppressWarnings("unused")
  public void submitSaveWAC(ActionEvent event) {
    resetFocusWac();
    boolean error = false;
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (isEmpty(getWacNumber())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "WAC is required.");
      setFocus(getWacNumber());
      error = true;
    }
    if (isEmpty(getWacWarehouse())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Warehouse is required.");
      setFocus(getWacWarehouse());
      error = true;
    }
    if (isEmpty(getWacMechanizedFlag())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Mechanized is required.");
      setFocus(getWacMechanizedFlag());
      error = true;
    }
    if (isEmpty(getWacSecureFlag())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Secure is required.");
      setFocus(getWacSecureFlag());
      error = true;
    }
    else {
      if (getWacSecureFlag().getValue().toString().equals("")) {
        JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Secure is required.");
        setFocus(getWacSecureFlag());
        error = true;
      }
    }
    if (isEmpty(getWacPackArea())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Pack Area is required.");
      setFocus(getWacPackArea());
      error = true;
    }
    if (error)
      return;

    //* handle case requirements
    String wacNum = Util.trimUpperCaseClean(getWacNumber().getValue());
    String mechFlag = Util.trimClean(getWacMechanizedFlag().getValue());
    String packArea = Util.trimUpperCaseClean(getWacPackArea().getValue());

    if (mechFlag.equals("0") || mechFlag.equals("1") || mechFlag.equals("V") || mechFlag.equals("H")) {
      if (isEmpty(getWacCarouselModel())) {
        JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(),
            "Carousel Model is required for a Mechanized (Vertical or Horizontal) WAC");
        setFocus(getWacCarouselModel());
        return;
      }
      else {
        if (getWacCarouselModel().getValue().toString().equals("")) {
          JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(),
              "Carousel Model is required for a Mechanized (Vertical or Horizontal) WAC");
          setFocus(getWacCarouselModel());
          return;
        }
      }
    }

    ELUtils.set("#{bindings.WacNumber.inputValue}", wacNum);
    ELUtils.set("#{bindings.PackArea.inputValue}", packArea);

    if (wacNum.length() != MIN_LEN_WAC_NUMBER) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(),
          "WAC Number must be " + MIN_LEN_WAC_NUMBER + " alphanumeric characters");
      setFocus(getWacNumber());
      error = true;
    }
    else if (RegUtils.isNotAlphaNumeric(wacNum)) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(),
          "WAC Number must be alphanumeric characters only (no spaces)");
      setFocus(getWacNumber());
      error = true;
    }

    if (packArea.length() != MIN_LEN_WAC_PACKAREA) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(),
          "WAC Pack Area must be " + MIN_LEN_WAC_PACKAREA + " alphanumeric characters");
      setFocus(getWacPackArea());
      error = true;
    }
    else if (RegUtils.isNotAlphaNumeric(packArea)) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(),
          "WAC Pack Area must be alphanumeric characters only (no spaces)");
      setFocus(getWacPackArea());
      error = true;
    }
    if (!isEmpty(getWacTasksPerTrip()) && isNaN(getWacTasksPerTrip())) {
      JSFUtils.addFacesErrorMessage("INVALID INTEGER", "Tasks Per Trip must be an integer.");
      setFocus(getWacTasksPerTrip());
      error = true;
    }
    if (!isEmpty(getWacSidsPerTrip()) && isNaN(getWacSidsPerTrip())) {
      JSFUtils.addFacesErrorMessage("INVALID INTEGER", "SIDs Per Trip must be an integer.");
      setFocus(getWacSidsPerTrip());
      error = true;
    }
    if (error)
      return;

    String function = "WAC";
    String fields = "wac number";
    try {
      saveIteratorKeepPosition(WAC_ITERATOR, function, fields, getWacTable(), false);
      setWacFlag(true);
      setWacNewFlag(false);
    }
    catch (Exception e) {
      setWacFlag(false);
    }
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      submitDeleteWAC(null);
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(wacTable);
    }
  }

  @SuppressWarnings("unused")
  public void submitDeleteWAC(ActionEvent event) {
    resetFocusWac();
    String deleteKey = "";

    Object oldRowKey = wacTable.getRowKey();
    Iterator<Object> selection = wacTable.getSelectedRowKeys().iterator();
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        wacTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) wacTable.getRowData();
        Row r = rowData.getRow();
        if (r.getAttribute("WacId") != null)
          deleteKey = r.getAttribute("WacId").toString();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      wacTable.setRowKey(oldRowKey);
    }

    if (deleteKey == null || deleteKey.equals("")) {
      displayMessage("[ERROR] Unknown Record for deletion.");
    }
    else {
      try {
        int id = Integer.parseInt(deleteKey);
        String message;
        WarehouseSetupImpl service = getWarehouseSetupModule();
        message = service.deleteWAC(id);
        if (message.equals("")) {
          DCIteratorBinding iter = ADFUtils.findIterator(WAC_ITERATOR);
          iter.executeQuery();
          displaySuccessMessage("WAC deleted.");
        }
        else {
          JSFUtils.addFacesInformationMessage("WAC currently in use and cannot be deleted.");
        }
      }
      catch (Exception e) {
        displayMessage("[ERROR] Error occurred while trying to delete ");
      }
    }
  }

  public void resetFocusWac() {

    if (getWacNumber() != null) {
      getWacNumber().setInlineStyle(uppercase);
    }
    if (getWacWarehouse() != null) {
      getWacWarehouse().setInlineStyle("");
    }
    if (getWacMechanizedFlag() != null) {
      getWacMechanizedFlag().setInlineStyle("");
    }
    if (getWacSecureFlag() != null) {
      getWacSecureFlag().setInlineStyle("");
    }
    if (getWacCarouselController() != null) {
      getWacCarouselController().setInlineStyle("");
    }
    if (getWacCarouselOffset() != null) {
      getWacCarouselOffset().setInlineStyle("");
    }
    if (getWacCarouselModel() != null) {
      getWacCarouselModel().setInlineStyle("");
    }
    if (getWacPackArea() != null) {
      getWacPackArea().setInlineStyle(uppercase);
    }
    if (getWacTasksPerTrip() != null) {
      getWacTasksPerTrip().setInlineStyle("");
    }
    if (getWacSidsPerTrip() != null) {
      getWacSidsPerTrip().setInlineStyle("");
    }
  }

  public final void setInitialWacFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get(WAC_FLAG);
    if (obj == null)
      setWacFlag(true);
  }

  /**
   * This function was added to handle user's manual refresh.  The table will not
   * keep creating new rows while there is already a new row which is unsaved.
   * The function is used only to call the ExecuteQuery on the iterator;
   * it does not set any variables used by system.
   */

  @SuppressWarnings("unused") //called from .jspx
  public void setWacRefresh(boolean refresh) {
    //* will clean up dirty created rows - also from a manual refresh
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("ExecuteWac");
    op1.execute();
  }

  /**
   * Set wac flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Warehouse Setup - WAC.
   */
  public void setWacFlag(boolean show) {

    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put(WAC_FLAG, show);
    this.wacFlag = show;
  }

  public boolean getWacFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = wacFlag;
    Object obj = afContext.getPageFlowScope().get(WAC_FLAG);
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return wacFlag || flag;
  }

  public void setWacSecureFlagList(List<SelectItem> wacSecureFlagList) {
    this.wacSecureFlagList = wacSecureFlagList;
  }

  /**
   * This function is used by the WAC SETUP web to create an ADF SelectOneChoice.
   * Returns all wac secure flag list items.
   */
  public List<SelectItem> getWacSecureFlagList() {
    if (!wacRefresh) {

      wacSecureFlagList.clear();
      wacSecureFlagList.add(new SelectItem("Y", "Yes"));
      wacSecureFlagList.add(new SelectItem("N", "No"));
    }

    return wacSecureFlagList;
  }

  public void setWacCarouselModelList(List<SelectItem> wacCarouselModelList) {
    this.wacCarouselModelList = wacCarouselModelList;
  }

  /**
   * This function is used by the WAC SETUP web to create an ADF SelectOneChoice.
   * Returns all wac carousel model list items.
   */
  public List<SelectItem> getWacCarouselModelList() {
    if (!wacRefresh) {
      wacCarouselModelList.clear();

      boolean verticalCarousel = false;
      if (!isEmpty(getWacMechanizedFlag())) {

        verticalCarousel =
            getWacMechanizedFlag().getValue().toString().equals("0") ||
                getWacMechanizedFlag().getValue().toString().equals("V"); //V
      }

      if (verticalCarousel) {
        wacCarouselModelList.add(new SelectItem("", ""));
        wacCarouselModelList.add(new SelectItem("HANEL", "HANEL"));
      }
      else {
        wacCarouselModelList.add(new SelectItem("WHITE", "WHITE"));
        wacCarouselModelList.add(new SelectItem("REMSTAR", "REMSTAR"));
      }
      wacCarouselModelList.add(new SelectItem("OTHER", "OTHER"));
    }
    return wacCarouselModelList;
  }

  public void setWacTable(RichTable wacTable) {
    this.wacTable = wacTable;
  }

  public RichTable getWacTable() {
    return wacTable;
  }

  public void setWacNumber(RichInputText wacNumber) {
    this.wacNumber = wacNumber;
  }

  public RichInputText getWacNumber() {
    return wacNumber;
  }

  public void setWacWarehouse(RichSelectOneChoice wacWarehouse) {
    this.wacWarehouse = wacWarehouse;
  }

  public RichSelectOneChoice getWacWarehouse() {
    return wacWarehouse;
  }

  public void setWacMechanizedFlag(RichSelectOneChoice wacMechanizedFlag) {
    this.wacMechanizedFlag = wacMechanizedFlag;
  }

  public RichSelectOneChoice getWacMechanizedFlag() {
    return wacMechanizedFlag;
  }

  public void setWacSecureFlag(RichSelectOneChoice wacSecureFlag) {
    this.wacSecureFlag = wacSecureFlag;
  }

  public RichSelectOneChoice getWacSecureFlag() {
    return wacSecureFlag;
  }

  public void setWacCarouselController(RichInputText wacCarouselController) {
    this.wacCarouselController = wacCarouselController;
  }

  public RichInputText getWacCarouselController() {
    return wacCarouselController;
  }

  public void setWacCarouselOffset(RichInputText wacCarouselOffset) {
    this.wacCarouselOffset = wacCarouselOffset;
  }

  public RichInputText getWacCarouselOffset() {
    return wacCarouselOffset;
  }

  public void setWacCarouselModel(RichSelectOneChoice wacCarouselModel) {
    this.wacCarouselModel = wacCarouselModel;
  }

  public RichSelectOneChoice getWacCarouselModel() {
    return wacCarouselModel;
  }

  public void setWacPackArea(RichInputText wacPackArea) {
    this.wacPackArea = wacPackArea;
  }

  public RichInputText getWacPackArea() {
    return wacPackArea;
  }

  public void setWacTasksPerTrip(RichInputText wacTasksPerTrip) {
    this.wacTasksPerTrip = wacTasksPerTrip;
  }

  public RichInputText getWacTasksPerTrip() {
    return wacTasksPerTrip;
  }

  @SuppressWarnings("unused") //called from .jspx
  public void setWacSidsPerTrip(RichInputText wacSidsPerTrip) {
    this.wacSidsPerTrip = wacSidsPerTrip;
  }

  public RichInputText getWacSidsPerTrip() {
    return wacSidsPerTrip;
  }

  public boolean isWacFlag() {
    return wacFlag;
  }

  public void setWacRefresh1(boolean wacRefresh) {
    this.wacRefresh = wacRefresh;
  }

  @SuppressWarnings("unused") //called from .jspx
  public boolean isWacRefresh() {
    return wacRefresh;
  }

  public void setWacNewFlag(boolean wacNewFlag) {
    this.wacNewFlag = wacNewFlag;
  }

  public boolean isWacNewFlag() {
    return wacNewFlag;
  }

  public void setSessionCarouselModel(Object sessionCarouselModel) {
    this.sessionCarouselModel = sessionCarouselModel;
  }

  public Object getSessionCarouselModel() {
    return sessionCarouselModel;
  }

  public void setSessionSecureFlag(Object sessionSecureFlag) {
    this.sessionSecureFlag = sessionSecureFlag;
  }

  public Object getSessionSecureFlag() {
    return sessionSecureFlag;
  }

  public void setWacSecureFlagNavList(RichSelectOneChoice wacSecureFlagNavList) {
    this.wacSecureFlagNavList = wacSecureFlagNavList;
  }

  public RichSelectOneChoice getWacSecureFlagNavList() {
    return wacSecureFlagNavList;
  }

  public void setWacNumberByBuildingList(List<SelectItem> wacNumberByBuildingList) {
    this.wacNumberByBuildingList = wacNumberByBuildingList;
  }

  public List<SelectItem> getWacNumberByBuildingList() {
    return wacNumberByBuildingList;
  }
}
