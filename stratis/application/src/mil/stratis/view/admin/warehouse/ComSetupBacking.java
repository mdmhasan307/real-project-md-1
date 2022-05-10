package mil.stratis.view.admin.warehouse;

import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.WarehouseSetupImpl;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.ELUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
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
import org.apache.myfaces.trinidad.event.SortEvent;

import javax.faces.event.ActionEvent;
import java.util.Iterator;

@Slf4j
public class ComSetupBacking extends AdminBackingHandler {

  private static final String COM_PORT_ITERATOR = "ComPortView1Iterator";

  private transient RichTable comPortTable;
  private transient RichInputText comPortName;
  private transient RichInputText comPortCOMName;
  private transient RichSelectOneChoice comPortFParity;
  private transient RichSelectOneChoice comPortParity;
  private transient RichSelectOneChoice comPortCOMNameList;
  private boolean comPortFlag = true;
  private boolean comPortRefresh = false;

  public ComSetupBacking() {
    super();

    //* required to show initial route update screen with grey, read only and buttons undisabled
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setInitialComPortFlag();
    }
  }

  public void submitResetCOMPort(@SuppressWarnings("all") ActionEvent event) {
    resetFocusComPort();
    resetKeepPosition(COM_PORT_ITERATOR);
    setComPortFlag(false);
  }

  public void submitCancelCOMPort(@SuppressWarnings("all") ActionEvent event) {
    resetFocusComPort();
    cancel(COM_PORT_ITERATOR);
    setComPortFlag(true);
  }

  public void submitUpdateCOMPort(@SuppressWarnings("all") ActionEvent event) {
    resetFocusComPort();
    setComPortFlag(false);
  }

  public void submitSaveCOMPort(@SuppressWarnings("all") ActionEvent event) {
    resetFocusComPort();
    if (isEmpty(getComPortName()) || isEmpty(getComPortCOMNameList()) || isEmpty(getComPortFParity())) {

      if (isEmpty(getComPortCOMNameList())) {
        JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Name is required.");
        setFocus(getComPortCOMNameList());
      }

      if (isEmpty(getComPortName())) {
        JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Description is required.");
        setFocus(getComPortName());
      }

      if (isEmpty(getComPortFParity())) {
        JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "FParity is required.");
        setFocus(getComPortFParity());
      }

      return;
    }

    if (!isEmpty(getComPortFParity())) {
      String flag = getComPortFParity().getValue().toString();
      if (flag.equals("0") && isEmpty(getComPortParity())) {
        JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Parity is required if FParity is True.");
        setFocus(getComPortParity());
        return;
      }
    }

    //* handle case requirements
    ELUtils.set("#{bindings.Name.inputValue}", Util.trimUpperCaseClean(getComPortName().getValue())); // description

    String function = "COM Port";
    String fields = "name";
    try {
      saveIteratorKeepPosition(COM_PORT_ITERATOR, function, fields, getComPortTable(), false);
      setComPortFlag(true);
    }
    catch (Exception e) {
      setComPortFlag(false);
    }
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      submitDeleteCOMPort(null);
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(comPortTable);
    }
  }

  public void submitDeleteCOMPort(@SuppressWarnings("all") ActionEvent actionEvent) {
    String deleteKey = "";
    Object oldRowKey = comPortTable.getRowKey();
    Iterator<Object> selection = comPortTable.getSelectedRowKeys().iterator();
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        comPortTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) comPortTable.getRowData();
        Row r = rowData.getRow();
        if (r.getAttribute("ComPortId") != null)
          deleteKey = r.getAttribute("ComPortId").toString();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      comPortTable.setRowKey(oldRowKey);
    }
    log.debug("deleting com port  {}", deleteKey);

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
        message = service.deleteComPort(id);
        if (message.equals("")) {
          DCIteratorBinding iter = ADFUtils.findIterator(COM_PORT_ITERATOR);
          iter.executeQuery();
          displaySuccessMessage("COM Port deleted.");
        }
        else {
          displayMessage(message);
        }
      }
      catch (Exception e) {
        displayMessage("[ERROR] Error occurred while trying to delete " + getComPortCOMName());
      }
    }
  }

  public void submitCreateCOMPort(@SuppressWarnings("all") ActionEvent actionEvent) {
    resetFocusComPort();
    setComPortRefresh(false);

    //* will clean up dirty created rows - also from a manual refresh
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("CreateInsert");
    op1.execute();
  }

  /**
   * This function was added to handle user's manual refresh.  The table will not
   * keep creating new rows while there is already a new row which is unsaved.
   * The function is used only to call the ExecuteQuery on the com port iterator;
   * it does not set any variables used by system.
   */
  public void setComPortRefresh(@SuppressWarnings("all") boolean refresh) {
    //* will clean up dirty created rows - also from a manual refresh
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("ExecuteComPort");
    op1.execute();
  }

  public void comPortSort(@SuppressWarnings("all") SortEvent sortEvent) {
  }

  public final void setInitialComPortFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get("comPortFlag");
    if (obj == null)
      setComPortFlag(true);
  }

  /**
   * Set com port flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Warehouse Setup - Com Port.
   */
  public void setComPortFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put("comPortFlag", show);
    this.comPortFlag = show;
  }

  public boolean getComPortFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = comPortFlag;
    Object obj = afContext.getPageFlowScope().get("comPortFlag");
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return comPortFlag || flag;
  }

  public void resetFocusComPort() {
    getComPortCOMNameList().setInlineStyle("");
    getComPortName().setInlineStyle(uppercase);
    getComPortFParity().setInlineStyle("");
    getComPortParity().setInlineStyle("");
  }

  /************************************************************
   *
   * END COM PORT
   * **********************************************************/
  public void setComPortTable(RichTable comPortTable) {
    this.comPortTable = comPortTable;
  }

  public RichTable getComPortTable() {
    return comPortTable;
  }

  public void setComPortName(RichInputText comPortName) {
    this.comPortName = comPortName;
  }

  public RichInputText getComPortName() {
    return comPortName;
  }

  public void setComPortCOMName(RichInputText comPortCOMName) {
    this.comPortCOMName = comPortCOMName;
  }

  public RichInputText getComPortCOMName() {
    return comPortCOMName;
  }

  public void setComPortFParity(RichSelectOneChoice comPortFParity) {
    this.comPortFParity = comPortFParity;
  }

  public RichSelectOneChoice getComPortFParity() {
    return comPortFParity;
  }

  public void setComPortParity(RichSelectOneChoice comPortParity) {
    this.comPortParity = comPortParity;
  }

  public RichSelectOneChoice getComPortParity() {
    return comPortParity;
  }

  public void setComPortCOMNameList(RichSelectOneChoice comPortCOMNameList) {
    this.comPortCOMNameList = comPortCOMNameList;
  }

  public RichSelectOneChoice getComPortCOMNameList() {
    return comPortCOMNameList;
  }

  public boolean isComPortFlag() {
    return comPortFlag;
  }

  public void setComPortRefresh1(boolean comPortRefresh) {
    this.comPortRefresh = comPortRefresh;
  }

  public boolean isComPortRefresh() {
    return comPortRefresh;
  }
}
