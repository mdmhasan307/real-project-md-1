package mil.stratis.view.admin.warehouse;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.model.services.WarehouseSetupImpl;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.util.ADFUtils;
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
import java.util.Iterator;

@Slf4j
public class ConsolidationSetupBacking extends AdminBackingHandler {

  private static final String CONSOLIDATION_ITERATOR = "PackingStationTableView1Iterator_Consolidation";
  public static final String CONSOLIDATION_FLAG = "consolidationFlag";

  private transient RichTable consolidationTable;
  private transient RichSelectOneChoice consolidationWorkstations;
  private transient RichInputText consolidationColumns;
  private transient RichInputText consolidationLevels;
  private boolean consolidationFlag = false;
  private boolean consolidationRefresh = false;

  public ConsolidationSetupBacking() {
    super();
    //* required to show initial route update screen with grey, read only and buttons undisabled
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setInitialConsolidationFlag();
    }
  }

  @SuppressWarnings("unused")
  public void submitCancelConsolidation(ActionEvent event) {
    resetFocusConsolidation();
    cancel(CONSOLIDATION_ITERATOR);
    setConsolidationFlag(true);
  }

  @SuppressWarnings("unused")
  public void submitSaveConsolidation(ActionEvent event) {
    resetFocusConsolidation();
    boolean error = false;

    val globalConstants = ContextUtils.getBean(GlobalConstants.class);
    if (isEmpty(getConsolidationWorkstations())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Workstation is required.");
      setFocus(getConsolidationWorkstations());
      error = true;
    }

    if (isEmpty(getConsolidationColumns())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Columns is required.");
      setFocus(getConsolidationColumns());
      error = true;
    }

    if (isEmpty(getConsolidationLevels())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Levels is required.");
      setFocus(getConsolidationLevels());
      error = true;
    }
    if (error)
      return;

    //* handle case requirements

    if (isNaN(getConsolidationColumns())) {
      JSFUtils.addFacesErrorMessage("INVALID INTEGER", "Columns must be a whole number.");
      setFocus(getConsolidationColumns());
      error = true;
    }

    if (isNaN(getConsolidationLevels())) {
      JSFUtils.addFacesErrorMessage("INVALID INTEGER", "Levels must be a whole number.");
      setFocus(getConsolidationLevels());
      error = true;
    }
    if (error)
      return;

    String function = "Consolidation Triwall";
    String fields = "workstation";
    try {
      saveIteratorKeepPosition(CONSOLIDATION_ITERATOR, function, fields, getConsolidationTable(), false);
      setConsolidationFlag(true);
    }
    catch (Exception e) {
      setConsolidationFlag(false);
    }
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      submitDeleteConsolidation(null);
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(consolidationTable);
    }
  }

  @SuppressWarnings("unused")
  public void submitDeleteConsolidation(ActionEvent event) {
    resetFocusConsolidation();
    String deleteKey = "";

    Object oldRowKey = consolidationTable.getRowKey();
    Iterator<Object> selection = consolidationTable.getSelectedRowKeys().iterator();
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        consolidationTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) consolidationTable.getRowData();
        Row r = rowData.getRow();
        if (r.getAttribute("PackingStationId") != null)
          deleteKey = r.getAttribute("PackingStationId").toString();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      consolidationTable.setRowKey(oldRowKey);
    }

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
        message = service.deletePackingStation(id);
        if (message.equals("")) {
          //* get the iterator used to populate the wac table
          DCIteratorBinding iter = ADFUtils.findIterator(CONSOLIDATION_ITERATOR);
          iter.executeQuery();

          displaySuccessMessage("Consolidation Triwall deleted.");
        }
        else {
          displayMessage(message);
        }
      }
      catch (Exception e) {
        displayMessage("[ERROR] Error occurred while trying to delete " + getConsolidationWorkstations().getValue());
      }
    }
  }

  public void resetFocusConsolidation() {
    getConsolidationColumns().setInlineStyle("");
    getConsolidationLevels().setInlineStyle("");
    getConsolidationWorkstations().setInlineStyle("");
  }

  /**
   * This function was added to handle user's manual refresh.  The table will not
   * keep creating new rows while there is already a new row which is unsaved.
   * The function is used only to call the ExecuteQuery on the iterator;
   * it does not set any variables used by system.
   */
  @SuppressWarnings("unused")
  public void setConsolidationRefresh(boolean refresh) {
    //* will clean up dirty created rows - also from a manual refresh
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("ExecuteConsolidation");
    op1.execute();
  }

  public final void setInitialConsolidationFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get(CONSOLIDATION_FLAG);
    if (obj == null)
      setConsolidationFlag(true);
  }

  /**
   * Set consolidation flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Warehouse Setup - Consolidation Triwall.
   */
  public void setConsolidationFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put(CONSOLIDATION_FLAG, show);
    this.consolidationFlag = show;
  }

  public boolean getConsolidationFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = consolidationFlag;
    Object obj = afContext.getPageFlowScope().get(CONSOLIDATION_FLAG);
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return consolidationFlag || flag;
  }

  public void setConsolidationTable(RichTable consolidationTable) {
    this.consolidationTable = consolidationTable;
  }

  public RichTable getConsolidationTable() {
    return consolidationTable;
  }

  public void setConsolidationWorkstations(RichSelectOneChoice consolidationWorkstations) {
    this.consolidationWorkstations = consolidationWorkstations;
  }

  public RichSelectOneChoice getConsolidationWorkstations() {
    return consolidationWorkstations;
  }

  public void setConsolidationColumns(RichInputText consolidationColumns) {
    this.consolidationColumns = consolidationColumns;
  }

  public RichInputText getConsolidationColumns() {
    return consolidationColumns;
  }

  public void setConsolidationLevels(RichInputText consolidationLevels) {
    this.consolidationLevels = consolidationLevels;
  }

  public RichInputText getConsolidationLevels() {
    return consolidationLevels;
  }

  @SuppressWarnings("unused") //called from .jspx
  public boolean isConsolidationFlag() {
    return consolidationFlag;
  }

  public void setConsolidationRefresh1(boolean consolidationRefresh) {
    this.consolidationRefresh = consolidationRefresh;
  }

  public boolean isConsolidationRefresh() {
    return consolidationRefresh;
  }
}
