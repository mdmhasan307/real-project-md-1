package mil.stratis.view.admin.warehouse;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.WarehouseSetupImpl;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.user.UserInfo;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.ELUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;
import mil.usmc.mls2.stratis.core.domain.event.domain.WorkstationDeletionEvent;
import mil.usmc.mls2.stratis.core.service.EventService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.component.rich.layout.RichPanelBox;
import oracle.adf.view.rich.component.rich.layout.RichPanelLabelAndMessage;
import oracle.adf.view.rich.component.rich.nav.RichButton;
import oracle.adf.view.rich.component.rich.nav.RichCommandToolbarButton;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.jbo.Row;
import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;
import org.apache.myfaces.trinidad.context.RequestContext;

import javax.faces.event.ActionEvent;
import java.util.Iterator;

@Slf4j
public class WorkstationSetupBacking extends AdminBackingHandler {

  private static final String WORKSTATION_ITERATOR = "EquipView1_WorkstationIterator";
  public static final String EQUIP_FLAG = "equipFlag";

  private transient RichCommandToolbarButton workstationCreateButton;
  private transient RichCommandToolbarButton workstationUpdateButton;
  private transient RichCommandToolbarButton workstationDeleteButton;
  private transient RichCommandToolbarButton workstationCancelButton;
  private transient RichPanelBox workstationDetailPanelBox;
  private transient RichPanelBox workstationEditDetailPanelBox;

  private boolean equipFlag = true;

  private boolean editing = false;
  private boolean updating = false;

  private boolean newWorkstationFlag = false;

  private transient RichButton create1Button;

  private transient RichPanelLabelAndMessage namePanelLabelAndMessage;
  private transient RichInputText nameInputText;
  private transient RichInputText nameEditInputText;

  private transient RichInputText shippingAreaEditInputText;
  private transient RichInputText printerNameEditInputText;

  private transient RichTable equipTable;
  private transient RichInputText workStationName;
  private transient RichSelectOneChoice workStationDescription;
  private transient RichSelectOneChoice adminWorkStationSetupWarehouse;
  private transient RichSelectOneChoice workStationPackGroupList;
  private transient RichSelectOneChoice workStationSetupWac;
  private transient RichSelectOneChoice workStationCubiscan;
  private transient RichInputText workStationShippingArea;

  public WorkstationSetupBacking() {
    super();

    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setInitialEquipFlag();
    }
  }

  @SuppressWarnings("unused")
  public void createWorkstationStart(ActionEvent actionEvent) {

    // Disable main buttons
    // toggleWorkStationSetupMainButtons( false )
    // Enable detail buttons and show the panel
    // Set the defaults on the edit detail panel
    toggleWorkStationSetupDetailPanel();
    // Toogle the editing flag
    setEditing(true);
  }

  @SuppressWarnings("unused")
  public void cancelWorkstationEdit(ActionEvent actionEvent) {
    setEditing(false);
    setUpdating(false);
  }

  private void toggleWorkStationSetupDetailPanel() {
    Object zero = "- Select -";
    workStationName.setValue("");
    workStationDescription.setValue(zero);
    adminWorkStationSetupWarehouse.setValue(zero);
    workStationSetupWac.setValue(zero);
    workStationShippingArea.setValue(zero);
    workStationPackGroupList.setValue(zero);
    workStationCubiscan.setValue(zero);
  }

  @SuppressWarnings("unused")
  public void submitResetWorkstation(ActionEvent event) {
    resetKeepPosition(WORKSTATION_ITERATOR);
    setEquipFlag(false);
  }

  @SuppressWarnings("unused")
  public void submitCancelWorkstation(ActionEvent event) {
    cancel(WORKSTATION_ITERATOR);
    setEquipFlag(true);
    setEditing(false);
    setNewWorkstationFlag(false);
  }

  @SuppressWarnings("unused")
  public void submitUpdateWorkstation(ActionEvent event) {
    setEquipFlag(false);
    setEditing(true);
    setNewWorkstationFlag(false);
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      submitDeleteWorkstation(null);
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(equipTable);
    }
  }

  @SuppressWarnings("unused")
  public void submitDeleteWorkstation(ActionEvent event) {
    resetFocusEquip();
    String deleteKey = "";
    Object wacId = null;

    log.trace("1. Find selected row to delete");
    Object oldRowKey = equipTable.getRowKey();
    Iterator<Object> selection = equipTable.getSelectedRowKeys().iterator();
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        equipTable.setRowKey(rowKey);
        JUCtrlHierNodeBinding rowData = (JUCtrlHierNodeBinding) equipTable.getRowData();
        Row r = rowData.getRow();
        if (r.getAttribute("EquipmentNumber") != null) {
          deleteKey = r.getAttribute("EquipmentNumber").toString();
        }
        wacId = r.getAttribute("WacId");
      }
    }
    catch (Exception e) {
      log.error("Error finding seleted row. Message: ", e);
    }
    finally {
      equipTable.setRowKey(oldRowKey);
    }

    log.trace("2. Delete record");
    if (deleteKey == null || deleteKey.equals("")) {
      displayMessage("[ERROR] Unknown Record for deletion.");
    }
    else {
      try {
        int id = Integer.parseInt(deleteKey);
        String message;
        WarehouseSetupImpl service = getWarehouseSetupModule();
        if (service == null) {
          displayMessage("[MODULE] Error occurred while trying to delete ");
          return;
        }
        message = service.deleteWorkstation(id, wacId);
        if (message.equals("")) {
          DCIteratorBinding iter = ADFUtils.findIterator(WORKSTATION_ITERATOR);
          iter.executeQuery();
          displaySuccessMessage("Workstation deleted.");
          auditLogWorkstationDelete("" + wacId, AuditLogTypeEnum.SUCCESS);
        }
        else {
          if (message.equals("Workstation has assigned tasks and cannot be deleted.")) {
            JSFUtils.addFacesInformationMessage(message);
          }
          else {
            displayMessage(message);
          }
          auditLogWorkstationDelete("" + wacId, AuditLogTypeEnum.FAILURE);
        }
      }
      catch (Exception e) {
        displayMessage("[ERROR] Error occurred while trying to delete "); // +
      }
    }
    log.trace("3. Complete");
  }

  private void auditLogWorkstationDelete(String wacId, AuditLogTypeEnum resultStatus) {
    val userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
    WorkstationDeletionEvent auditLogEvent = WorkstationDeletionEvent.builder(resultStatus)
        .userInfo(userInfo)
        .wacId(wacId)
        .build();
    ContextUtils.getBean(EventService.class).publishEvent(auditLogEvent);
  }

  private String wacRequiredStyle = "";

  public void setWacRequiredStyle(String wacRequiredStyle) {
    this.wacRequiredStyle = wacRequiredStyle;
  }

  @SuppressWarnings("unused")
  public void submitSaveWorkstation(ActionEvent event) {
    resetFocusEquip();
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (!isEmpty(getWorkStationName())) {
      if (getWorkStationName().getValue().toString().equalsIgnoreCase("DUMMY")) {
        JSFUtils.addFacesErrorMessage("INVALID", "Users may not modify or delete the DUMMY workstation");
        setFocus(getWorkStationName());
        return;
      }
      else {
        ELUtils.set("#{bindings.Name.inputValue}", workStationName.getValue().toString().trim().toUpperCase());
      }
    }

    if (isEmpty(getWorkStationName()) || isEmpty(getWorkStationDescription()) || isEmpty(getAdminWorkStationSetupWarehouse())) {

      if (isEmpty(getWorkStationName())) {
        JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Workstation Name is required.");
        setFocus(getWorkStationName());
      }

      if (isEmpty(getWorkStationDescription())) {
        JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Description is required.");
        setFocus(getWorkStationDescription());
      }

      if (isEmpty(getAdminWorkStationSetupWarehouse())) {
        JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Building is required.");
        setFocus(getAdminWorkStationSetupWarehouse());
      }

      return;
    }

    String desc = getWorkStationDescription().getValue().toString();
    if ((desc.equals("2") || desc.equals("3")) && isEmpty(getWorkStationPackGroupList())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Packing Group (only required for Packing workstations) is required.");
      setFocus(getWorkStationPackGroupList());
      return;
    }

    if (desc.equals("1") && isEmpty(getWorkStationSetupWac())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "WAC (only required for Stow/Issue workstations) is required.");
      setFocus(getWorkStationSetupWac());
      return;
    }

    if (desc.equals("0") && isEmpty(getWorkStationCubiscan())) {
      ELUtils.set("#{bindings.HasCubiscan.inputValue}", "N");
    }

    if (!isEmpty(getWorkStationShippingArea())) {
      String shippingArea = Util.trimUpperCaseClean(getWorkStationShippingArea().getValue());
      ELUtils.set("#{bindings.ShippingArea.inputValue}", shippingArea);
      if (RegUtils.isNotAlphaNumeric(shippingArea)) {
        JSFUtils.addFacesErrorMessage("INVALID FIELD", "Workstation Shipping Area must be four alphanumeric characters only");
        setFocus(getWorkStationShippingArea());
        return;
      }

      if (shippingArea.length() < 4) {
        JSFUtils.addFacesErrorMessage("INVALID FIELD", "Workstation Shipping Area must be four alphanumeric characters only");
        setFocus(getWorkStationShippingArea());
        return;
      }
    }

    try {
      String iteratorName = WORKSTATION_ITERATOR;
      String function = "Workstation";
      String fields = "workstation name";
      saveIteratorKeepPosition(iteratorName, function, fields, getEquipTable(), false);
      setEquipFlag(true);
      setNewWorkstationFlag(false);
      refreshIterator(ADFUtils.findIterator(iteratorName));
    }
    catch (Exception e) {
      setEquipFlag(false);
      AdfLogUtility.logException(e);
    }
  }

  public void resetFocusEquip() {

    if (getWorkStationName() != null) {
      getWorkStationName().setInlineStyle(uppercase);
    }

    if (getWorkStationDescription() != null) {
      getWorkStationDescription().setInlineStyle("");
    }

    if (getAdminWorkStationSetupWarehouse() != null) {
      getAdminWorkStationSetupWarehouse().setInlineStyle("");
    }

    if (getWorkStationSetupWac() != null) {
      getWorkStationSetupWac().setInlineStyle("");
    }

    if (getWorkStationShippingArea() != null) {
      getWorkStationShippingArea().setInlineStyle(uppercase);
    }

    if (getWorkStationPackGroupList() != null) {
      getWorkStationPackGroupList().setInlineStyle("");
    }
    if (getWorkStationCubiscan() != null) {
      getWorkStationCubiscan().setInlineStyle("");
    }
  }

  /**
   * This function was added to handle user's manual refresh.  The table will not
   * keep creating new rows while there is already a new row which is unsaved.
   * The function is used only to call the ExecuteQuery on the iterator;
   * it does not set any variables used by system.
   */
  @SuppressWarnings("unused")
  public void setEquipRefresh(boolean refresh) {
    //NO-OP called from .jspx
  }

  public final void setInitialEquipFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get(EQUIP_FLAG);
    if (obj == null)
      setEquipFlag(true);
  }

  /**
   * Set equip (workstation) flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Warehouse Setup - Workstation.
   */

  public void setEquipFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put(EQUIP_FLAG, show);
    this.equipFlag = show;
  }

  public boolean getEquipFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = equipFlag;
    Object obj = afContext.getPageFlowScope().get(EQUIP_FLAG);
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return equipFlag || flag;
  }

  public static String getWorkstationIterator() {
    return WORKSTATION_ITERATOR;
  }

  public String getWacRequiredStyle1() {
    return wacRequiredStyle;
  }

  public void setWorkstationDeleteButton(RichCommandToolbarButton workstationDeleteButton) {
    this.workstationDeleteButton = workstationDeleteButton;
  }

  public RichCommandToolbarButton getWorkstationDeleteButton() {
    return workstationDeleteButton;
  }

  public void setWorkstationUpdateButton(RichCommandToolbarButton workstationUpdateButton) {
    this.workstationUpdateButton = workstationUpdateButton;
  }

  public RichCommandToolbarButton getWorkstationUpdateButton() {
    return workstationUpdateButton;
  }

  public void setWorkstationCreateButton(RichCommandToolbarButton workstationCreateButton) {
    this.workstationCreateButton = workstationCreateButton;
  }

  public RichCommandToolbarButton getWorkstationCreateButton() {
    return workstationCreateButton;
  }

  public void setWorkstationCancelButton(RichCommandToolbarButton workstationCancelButton) {
    this.workstationCancelButton = workstationCancelButton;
  }

  public RichCommandToolbarButton getWorkstationCancelButton() {
    return workstationCancelButton;
  }

  public void setUpdating(boolean updating) {
    this.updating = updating;
  }

  public boolean isUpdating() {
    return updating;
  }

  public void setWorkstationDetailPanelBox(RichPanelBox workstationDetailPanelBox) {
    this.workstationDetailPanelBox = workstationDetailPanelBox;
  }

  public RichPanelBox getWorkstationDetailPanelBox() {
    return workstationDetailPanelBox;
  }

  public RichPanelBox getWorkstationEditDetailPanelBox() {
    return workstationEditDetailPanelBox;
  }

  public boolean isEquipFlag() {
    return equipFlag;
  }

  public boolean isEquipRefresh() {
    return false;
  }

  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  public boolean isEditing() {
    return editing;
  }

  public void setCreate1Button(RichButton create1Button) {
    this.create1Button = create1Button;
  }

  public RichButton getCreate1Button() {
    return create1Button;
  }

  public void setNamePanelLabelAndMessage(RichPanelLabelAndMessage namePanelLabelAndMessage) {
    this.namePanelLabelAndMessage = namePanelLabelAndMessage;
  }

  public RichPanelLabelAndMessage getNamePanelLabelAndMessage() {
    return namePanelLabelAndMessage;
  }

  public void setNameInputText(RichInputText nameInputText) {
    this.nameInputText = nameInputText;
  }

  public RichInputText getNameInputText() {
    return nameInputText;
  }

  public void setNameEditInputText(RichInputText nameEditInputText) {
    this.nameEditInputText = nameEditInputText;
  }

  public RichInputText getNameEditInputText() {
    return nameEditInputText;
  }

  public void setShippingAreaEditInputText(RichInputText shippingAreaEditInputText) {
    this.shippingAreaEditInputText = shippingAreaEditInputText;
  }

  public RichInputText getShippingAreaEditInputText() {
    return shippingAreaEditInputText;
  }

  public void setPrinterNameEditInputText(RichInputText printerNameEditInputText) {
    this.printerNameEditInputText = printerNameEditInputText;
  }

  public RichInputText getPrinterNameEditInputText() {
    return printerNameEditInputText;
  }

  public void setEquipTable(RichTable equipTable) {
    this.equipTable = equipTable;
  }

  public RichTable getEquipTable() {
    return equipTable;
  }

  public void setWorkStationName(RichInputText workStationName) {
    this.workStationName = workStationName;
  }

  public RichInputText getWorkStationName() {
    return workStationName;
  }

  public void setWorkStationDescription(RichSelectOneChoice workStationDescription) {
    this.workStationDescription = workStationDescription;
  }

  public RichSelectOneChoice getWorkStationDescription() {
    return workStationDescription;
  }

  @SuppressWarnings("unused") //called from .jspx
  public void setAdminWorkStationSetupWarehouse(RichSelectOneChoice adminWorkStationSetupWarehouse) {
    this.adminWorkStationSetupWarehouse = adminWorkStationSetupWarehouse;
  }

  public RichSelectOneChoice getAdminWorkStationSetupWarehouse() {
    return adminWorkStationSetupWarehouse;
  }

  public void setWorkStationPackGroupList(RichSelectOneChoice workStationPackGroupList) {
    this.workStationPackGroupList = workStationPackGroupList;
  }

  public RichSelectOneChoice getWorkStationPackGroupList() {
    return workStationPackGroupList;
  }

  public void setWorkStationSetupWac(RichSelectOneChoice workStationSetupWac) {
    this.workStationSetupWac = workStationSetupWac;
  }

  public RichSelectOneChoice getWorkStationSetupWac() {
    return workStationSetupWac;
  }

  public void setWorkStationCubiscan(RichSelectOneChoice workStationCubiscan) {
    this.workStationCubiscan = workStationCubiscan;
  }

  public RichSelectOneChoice getWorkStationCubiscan() {
    return workStationCubiscan;
  }

  public void setWorkStationShippingArea(RichInputText workStationShippingArea) {
    this.workStationShippingArea = workStationShippingArea;
  }

  public RichInputText getWorkStationShippingArea() {
    return workStationShippingArea;
  }

  public void setNewWorkstationFlag(boolean newWorkstationFlag) {
    this.newWorkstationFlag = newWorkstationFlag;
  }

  @SuppressWarnings("unused") //called from .jspx
  public boolean isNewWorkstationFlag() {
    return newWorkstationFlag;
  }
}
