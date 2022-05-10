package mil.stratis.view.admin.warehouse;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.AppModuleImpl;
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
import oracle.adf.view.rich.component.rich.output.RichImage;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.binding.OperationBinding;
import oracle.jbo.Row;
import oracle.jbo.domain.Number;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;
import org.apache.commons.lang3.StringUtils;
import org.apache.myfaces.trinidad.context.RequestContext;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import java.util.Iterator;

@Slf4j
public class StorageSetupBacking extends AdminBackingHandler {

  private static final int MAX_LEN_STORAGEBIN_NAME = 2;
  public static final String LOC_CLASSIFICATION_ID = "LocClassificationId";
  public static final String DIVIDER_TYPE_ID = "DividerTypeId";
  public static final String DIVIDER_TYPE_FLAG = "dividerTypeFlag";
  public static final String STORAGE_BIN_FLAG = "storageBinFlag";

  //* STORAGE BIN TYPE VARIABLES
  private transient RichTable storageBinTable;
  private transient RichInputText storageBinName;
  private transient RichInputText storageBinDescription;
  private transient RichInputText storageBinLength;
  private transient RichInputText storageBinWidth;
  private transient RichInputText storageBinHeight;
  private transient RichInputText storageBinUsableWeight;
  private transient RichInputText storageBinUsableCube;
  private transient RichInputText storageBinSlotCount;
  private transient RichImage binImage;
  private transient RichSelectOneChoice binImageChoice;
  private transient RichImage divImage;
  private transient RichSelectOneChoice divImageChoice;

  private String storageBinDividerName;
  private boolean storageBinFlag = true;
  private boolean storageBinInUseFlag = true;
  private boolean storageBinRefresh = false;

  //* DIVIDER TYPE VARIABLES
  private transient RichTable dividerTable;
  private transient RichTable dividerTypeSubTable;
  private transient RichInputText dividerTypeName;
  private transient RichInputText dividerTypeSlotSelectIndex;
  private transient RichInputText dividerTypeSlotPosition;
  private transient RichInputText dividerTypeSlotRow;
  private transient RichInputText dividerTypeSlotColumn;
  private transient RichInputText dividerTypeSlotLength;
  private transient RichInputText dividerTypeSlotWidth;

  private boolean dividerTypeFlag = false;
  private boolean dividerTypeRefresh = false;

  private static String dividerIterator = "DividerTypeView1Iterator";
  private static String dividerSlotIterator = "DividerSlotsView2Iterator";
  private static String storageBinIterator = "LocationClassification_StorageBinView1Iterator";

  public StorageSetupBacking() {
    super();

    //* required to show initial route update screen with grey, read only and buttons undisabled
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {

      setInitialDividerTypeFlag();
      setInitialStorageBinFlag();
    }
  }

  /**
   * DIVIDER
   **/

  @SuppressWarnings("unused")
  public void submitResetDivider(ActionEvent event) {
    resetKeepPosition(dividerIterator);
    setDividerTypeFlag(false);
  }

  @SuppressWarnings("unused")
  public void submitCancelDivider(ActionEvent event) {
    cancel(dividerIterator);
    setDividerTypeFlag(true);
  }

  @SuppressWarnings("unused")
  public void submitUpdateDivider(ActionEvent event) {
    setDividerTypeFlag(false);
  }

  @SuppressWarnings("unused")
  public void submitSaveDivider(ActionEvent event) {
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (isEmpty(getDividerTypeName())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Divider Type Name is required.");
      return;
    }

    //* handle case requirements
    String localDividerTypeName = Util.trimUpperCaseClean(getDividerTypeName().getValue());
    ELUtils.set("#{bindings.Name.inputValue}", localDividerTypeName);

    if (RegUtils.isNotAlphaNumeric(localDividerTypeName)) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "Divider Type Name must be alphanumeric characters only");
      return;
    }

    String iteratorName = dividerIterator;
    String function = "Divider Type";
    String fields = "name";
    try {
      saveIteratorKeepPosition(iteratorName, function, fields, getDividerTable(), false);
      setDividerTypeFlag(true);
    }
    catch (Exception e) {
      setDividerTypeFlag(false);
    }
  }

  @SuppressWarnings("unused") //called from .jspx
  public void dialogListener1(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      submitDeleteDivider(null);
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(dividerTable);
    }
  }

  @SuppressWarnings("unused")
  public void submitDeleteDivider(ActionEvent event) {
    String deleteKey = "";

    Object oldRowKey = dividerTable.getRowKey();
    Iterator<Object> selection = dividerTable.getSelectedRowKeys().iterator();
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        dividerTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) dividerTable.getRowData();
        Row r = rowData.getRow();
        if (r.getAttribute(DIVIDER_TYPE_ID) != null)
          deleteKey = r.getAttribute(DIVIDER_TYPE_ID).toString();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      dividerTable.setRowKey(oldRowKey);
    }

    if (deleteKey == null || deleteKey.equals("")) {
      displayMessage("[ERROR] Unknown Record for deletion.");
    }
    else {
      try {
        int id = Integer.parseInt(deleteKey);
        String message;
        WarehouseSetupImpl service = getWarehouseSetupModule();
        if (service != null) {
          message = service.deleteDivider(id);
          if (message.equals("")) {
            //* get the iterator used to populate the divider table
            DCIteratorBinding iter = ADFUtils.findIterator(dividerIterator);
            iter.executeQuery();
            displaySuccessMessage("Divider deleted.");
          }
          else {
            displayMessage(message);
          }
        }
        else {
          log.error("Error the service was unavailable.");
          displayMessage("[ERROR] Error the service was unavailable.");
        }
      }
      catch (Exception e) {
        displayMessage("[ERROR] Error occurred while trying to delete "); // +
      }
    }
  }

  /**
   * This function was added to handle user's manual refresh.  The table will not
   * keep creating new rows while there is already a new row which is unsaved.
   * The function is used only to call the ExecuteQuery on the iterator;
   * it does not set any variables used by system.
   */
  @SuppressWarnings("unused")
  public void setDividerTypeRefresh(boolean refresh) {
    //* will clean up dirty created rows - also from a manual refresh
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("ExecuteDividerType");
    if (op1 != null) {
      op1.execute();
    }
  }

  public final void setInitialDividerTypeFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (afContext != null) {
      Object obj = afContext.getPageFlowScope().get(DIVIDER_TYPE_FLAG);
      if (obj == null) {
        setDividerTypeFlag(true);
      }
    }
  }

  /**
   * Set divider type flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Warehouse Setup - Divider Type.
   */
  public void setDividerTypeFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (afContext != null) {
      //* for use in the web, maintaining state
      afContext.getPageFlowScope().put(DIVIDER_TYPE_FLAG, show);
    }
    dividerTypeFlag = show;
  }

  public boolean getDividerTypeFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = dividerTypeFlag;
    Object obj = afContext.getPageFlowScope().get(DIVIDER_TYPE_FLAG);
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return dividerTypeFlag || flag;
  }

  @SuppressWarnings("unused")
  public void submitCreateDividerSlot(ActionEvent event) {
    boolean error = false;
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (isEmpty(getDividerTypeSlotPosition())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Divider Slot Position Label is required.");
      error = true;
    }
    if (isEmpty(getDividerTypeSlotColumn())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Divider Slot Column Start is required.");
      error = true;
    }
    if (isEmpty(getDividerTypeSlotRow())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Divider Slot Row Start is required.");
      error = true;
    }
    if (isEmpty(getDividerTypeSlotLength())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Divider Slot Length is required.");
      error = true;
    }
    if (isEmpty(getDividerTypeSlotWidth())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Divider Slot Width is required.");
      error = true;
    }
    if (error)
      return;

    //* handle case requirements
    getDividerTypeSlotPosition().setValue(Util.trimUpperCaseClean(getDividerTypeSlotPosition().getValue()));
    getDividerTypeSlotRow().setValue(Util.trimClean(getDividerTypeSlotRow().getValue()));
    getDividerTypeSlotColumn().setValue(Util.trimClean(getDividerTypeSlotColumn().getValue()));
    getDividerTypeSlotLength().setValue(Util.trimClean(getDividerTypeSlotLength().getValue()));
    getDividerTypeSlotWidth().setValue(Util.trimClean(getDividerTypeSlotWidth().getValue()));

    if (isNaN(getDividerTypeSlotColumn())) {
      JSFUtils.addFacesErrorMessage("INVALID INTEGER", "Divider Slot Column Start must be an integer.");
      error = true;
    }
    if (isNaN(getDividerTypeSlotRow())) {
      JSFUtils.addFacesErrorMessage("INVALID INTEGER", "Divider Slot Row Start must be an integer.");
      error = true;
    }
    if (isNaD(getDividerTypeSlotLength())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidNumberTag(), "Divider Slot Length must be an integer.");
      error = true;
    }
    if (isNaD(getDividerTypeSlotWidth())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidNumberTag(), "Divider Slot Width must be an integer.");
      error = true;
    }
    if (error)
      return;
    String strKey = "";

    Object oldRowKey = dividerTable.getRowKey();
    Iterator<Object> selection = dividerTable.getSelectedRowKeys().iterator();
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        dividerTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) dividerTable.getRowData();
        Row r = rowData.getRow();
        if (r.getAttribute(DIVIDER_TYPE_ID) != null)
          strKey = r.getAttribute(DIVIDER_TYPE_ID).toString();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      dividerTable.setRowKey(oldRowKey);
    }

    AppModuleImpl service = getAppModule();
    int result = service.createDividerSlot(strKey, getDividerTypeSlotPosition().getValue().toString(), getDividerTypeSlotColumn().getValue().toString(), getDividerTypeSlotRow().getValue().toString(), getDividerTypeSlotLength().getValue().toString(), getDividerTypeSlotWidth().getValue().toString());

    if (result < 0) {
      if (result == -1)
        JSFUtils.addFacesErrorMessage("INVALID POSITION", "Divider Slot cannot have the same POSITION Label. Rename the Position and try again.");
      if (result == -2)
        JSFUtils.addFacesErrorMessage("INVALID POSITION", "Divider Type is in use by a Storage Bin, cannot add divider slots at this time.");
    }
    else {
      //* reset fields
      getDividerTypeSlotPosition().setValue("");
      getDividerTypeSlotColumn().setValue("");
      getDividerTypeSlotRow().setValue("");
      getDividerTypeSlotLength().setValue("");
      getDividerTypeSlotWidth().setValue("");
    }
  }

  @SuppressWarnings("unused")
  public void submitResetStorageBin(ActionEvent event) {
    resetFocusStorageBin();
    resetKeepPosition(storageBinIterator);
    setStorageBinFlag(false);
  }

  @SuppressWarnings("unused")
  public void submitCancelStorageBin(ActionEvent event) {
    resetFocusStorageBin();
    cancel(storageBinIterator);
    setStorageBinFlag(true);
  }

  @SuppressWarnings("unused")
  public void submitUpdateStorageBin(ActionEvent event) {
    resetFocusStorageBin();

    boolean inuse = true;
    String updateKey = "";

    if (getStorageBinTable() != null) {

      Object oldRowKey = storageBinTable.getRowKey();
      Iterator<Object> selection = storageBinTable.getSelectedRowKeys().iterator();
      try {
        while (selection.hasNext()) {
          Object rowKey = selection.next();
          storageBinTable.setRowKey(rowKey);
          JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) storageBinTable.getRowData();
          Row r = rowData.getRow();
          if (r.getAttribute(LOC_CLASSIFICATION_ID) != null)
            updateKey = r.getAttribute(LOC_CLASSIFICATION_ID).toString();
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
      finally {
        storageBinTable.setRowKey(oldRowKey);
      }
    }

    if (StringUtils.isNotBlank(updateKey)) {
      try {
        int id = Integer.parseInt(updateKey);
        WarehouseSetupImpl service = getWarehouseSetupModule();
        inuse = service.inUseStorageBin(id);
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
    setStorageBinInUseFlag(inuse);
    setStorageBinFlag(false);
  }

  @SuppressWarnings("unused")  //called from .jspx
  public void getDataForDiv(ValueChangeEvent e) {
    // you can get index of current row
    int rowIdx = Integer.parseInt(e.getNewValue().toString());

    // get iterator
    DCIteratorBinding divIter = ADFUtils.findIterator("DividerTypeView1Iterator_StorageBin");

    // set iterator to selected row
    divIter.setCurrentRowIndexInRange(rowIdx);

    // get current row
    Row row = divIter.getCurrentRow();
    storageBinDividerName = row.getAttribute(1).toString();
    log.trace("storageBin_DividerName: {}", storageBinDividerName);

    //now you can get value using row.getAttribute("AttributeName")
  }

  @SuppressWarnings("unused")
  public void submitSaveStorageBin(ActionEvent event) {
    boolean error = false;
    resetFocusStorageBin();
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    //* handle required fields
    if (isEmpty(getStorageBinName())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Storage Bin Name is required.");
      setFocus(getStorageBinName());
      error = true;
    }
    if (isEmpty(getStorageBinLength())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Length is required.");
      setFocus(getStorageBinLength());
      error = true;
    }
    if (isEmpty(getStorageBinWidth())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Width is required.");
      setFocus(getStorageBinWidth());
      error = true;
    }
    if (isEmpty(getStorageBinHeight())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Height is required.");
      setFocus(getStorageBinHeight());
      error = true;
    }
    if (isEmpty(getStorageBinUsableWeight())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Usable Weight is required.");
      setFocus(getStorageBinUsableWeight());
      error = true;
    }

    if (error)
      return;

    //* handle case requirements
    String localStorageBinName = Util.trimUpperCaseClean(getStorageBinName().getValue());
    ELUtils.set("#{bindings.Name.inputValue}", localStorageBinName);
    ELUtils.set("#{bindings.Description.inputValue}", Util.trimUpperCaseClean(getStorageBinDescription().getValue()));

    if (localStorageBinName.length() != MAX_LEN_STORAGEBIN_NAME) {
      JSFUtils.addFacesErrorMessage("INVALID FIELD", "Storage Bin Name must be " + MAX_LEN_STORAGEBIN_NAME + " alphanumeric characters");
      setFocus(getStorageBinName());
      error = true;
    }
    else if (RegUtils.isNotAlphaNumeric(localStorageBinName)) {
      JSFUtils.addFacesErrorMessage("INVALID FIELD", "Storage Bin Name must be alphanumeric characters only");
      setFocus(getStorageBinName());
      error = true;
    }

    //* handle invalid number (not a decimal) fields
    if (isNaD(getStorageBinLength())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidNumberTag(), "Length");
      setFocus(getStorageBinLength());
      error = true;
    }
    if (isNaD(getStorageBinWidth())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidNumberTag(), "Width");
      setFocus(getStorageBinWidth());
      error = true;
    }
    if (isNaD(getStorageBinHeight())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidNumberTag(), "Height");
      setFocus(getStorageBinHeight());
      error = true;
    }
    if (isNaD(getStorageBinUsableWeight())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidNumberTag(), "Usable Weight");
      setFocus(getStorageBinUsableWeight());
      error = true;
    }
    AppModuleImpl service = getAppModule();
    if (service != null && !service.checkMaxLocations(Integer.parseInt(getStorageBinWidth().getValue().toString()), storageBinDividerName)) {
      JSFUtils.addFacesErrorMessage("INVALID DIMENSIONS-DIVIDER", "The bin is too small to have that divider type");
      error = true;
    }
    if (error)
      return;
    try {
      double l = Double.parseDouble(getStorageBinLength().getValue().toString());
      double w = Double.parseDouble(getStorageBinWidth().getValue().toString());
      double h = Double.parseDouble(getStorageBinHeight().getValue().toString());
      double weight = Double.parseDouble(storageBinUsableWeight.getValue().toString());
      double total = l * w * h;
      Number totalnum = new Number(total);
      Number lengthnum = new Number(l);
      Number widthnum = new Number(w);
      Number heightnum = new Number(h);
      Number weightnum = new Number(weight);
      ELUtils.set("#{bindings.UsableCube.inputValue}", totalnum);
      ELUtils.set("#{bindings.Length.inputValue}", lengthnum);
      ELUtils.set("#{bindings.Width.inputValue}", widthnum);
      ELUtils.set("#{bindings.Height.inputValue}", heightnum);
      ELUtils.set("#{bindings.UsableWeight.inputValue}", weightnum);
      ELUtils.set("#{bindings.MechanizableStorage.inputValue}", "Y");

      String iteratorName = storageBinIterator;
      String function = "Storage Bin";
      String fields = "name";

      saveIteratorKeepPosition(iteratorName, function, fields, getStorageBinTable(), false);
      setStorageBinFlag(true);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      setStorageBinFlag(false);
    }
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      submitDeleteStorageBin(null);
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(storageBinTable);
    }
  }

  @SuppressWarnings("unused")
  public void submitDeleteStorageBin(ActionEvent event) {
    resetFocusStorageBin();
    String deleteKey = "";
    Object oldRowKey = storageBinTable.getRowKey();
    Iterator<Object> selection = storageBinTable.getSelectedRowKeys().iterator();
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        storageBinTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) storageBinTable.getRowData();
        Row r = rowData.getRow();
        if (r.getAttribute(LOC_CLASSIFICATION_ID) != null)
          deleteKey = r.getAttribute(LOC_CLASSIFICATION_ID).toString();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      storageBinTable.setRowKey(oldRowKey);
    }

    if (deleteKey == null || deleteKey.equals("")) {
      displayMessage("[ERROR] Unknown Record for deletion.");
    }
    else {
      try {
        int id = Integer.parseInt(deleteKey);
        String message;
        WarehouseSetupImpl service = getWarehouseSetupModule();
        message = service.deleteStorageBin(id);
        if (message.equals("")) {

          //* get the iterator used to populate the storage bin table
          DCIteratorBinding iter = ADFUtils.findIterator(storageBinIterator);
          iter.executeQuery();
          displaySuccessMessage("Storage Bin deleted.");
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
  public void submitCreateStorageBin(ActionEvent actionEvent) {
    cancel(storageBinIterator);

    //* will clean up dirty created rows - also from a manual refresh
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("CreateInsert");
    op1.execute();
  }

  /**
   * This function was added to handle user's manual refresh.  The table will not
   * keep creating new rows while there is already a new row which is unsaved.
   * The function is used only to call the ExecuteQuery on the iterator;
   * it does not set any variables used by system.
   */
  @SuppressWarnings("unused")
  public void setStorageBinRefresh(boolean refresh) {
    //* will clean up dirty created rows - also from a manual refresh
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("ExecuteStorageBin");
    op1.execute();
  }

  @SuppressWarnings("unused") //called from .jspx
  public boolean getStorageBinRefresh() {
    return false;
  }

  public final void setInitialStorageBinFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get(STORAGE_BIN_FLAG);
    if (obj == null)
      setStorageBinFlag(true);
  }

  /**
   * Set storage bin flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Warehouse Setup - Storage Bin.
   */
  public void setStorageBinFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put(STORAGE_BIN_FLAG, show);
    this.storageBinFlag = show;
  }

  public boolean getStorageBinFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = storageBinFlag;
    Object obj = afContext.getPageFlowScope().get(STORAGE_BIN_FLAG);
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return storageBinFlag || flag;
  }

  public void setStorageBinInUseFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put("storageBinInUseFlag", show);
    this.storageBinInUseFlag = show;
  }

  public boolean getStorageBinInUseFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = storageBinInUseFlag;
    Object obj = afContext.getPageFlowScope().get("storageBinInUseFlag");
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return storageBinInUseFlag || flag;
  }

  public String getBinSource() {
    String imageURL;
    if (binImageChoice == null || binImageChoice.getValue() == null) {
      imageURL = "../resources/images/bin_images/NoImage.jpg";
    }
    else {
      imageURL = "../resources/images/bin_images/" + binImageChoice.getValue();
    }

    return imageURL;
  }

  public String getDivSource() {
    String imageURL;
    if (divImageChoice == null || divImageChoice.getValue() == null) {
      imageURL = "../resources/images/bin_images/NoImage.jpg";
    }
    else {
      DCIteratorBinding divIter = ADFUtils.findIterator("DividerTypeView1Iterator_StorageBin");

      // get current row
      Row row = divIter.getCurrentRow();
      imageURL = "/SlotImage?type=SLOT&DI=" + row.getAttribute(DIVIDER_TYPE_ID);
    }
    log.debug(imageURL);
    return imageURL;
  }

  public void resetFocusStorageBin() {
    //NO-OP
  }

  public void setStorageBinTable(RichTable storageBinTable) {
    this.storageBinTable = storageBinTable;
  }

  public RichTable getStorageBinTable() {
    return storageBinTable;
  }

  public void setStorageBinName(RichInputText storageBinName) {
    this.storageBinName = storageBinName;
  }

  public RichInputText getStorageBinName() {
    return storageBinName;
  }

  public void setStorageBinDescription(RichInputText storageBinDescription) {
    this.storageBinDescription = storageBinDescription;
  }

  public RichInputText getStorageBinDescription() {
    return storageBinDescription;
  }

  public void setStorageBinLength(RichInputText storageBinLength) {
    this.storageBinLength = storageBinLength;
  }

  public RichInputText getStorageBinLength() {
    return storageBinLength;
  }

  public void setStorageBinWidth(RichInputText storageBinWidth) {
    this.storageBinWidth = storageBinWidth;
  }

  public RichInputText getStorageBinWidth() {
    return storageBinWidth;
  }

  public void setStorageBinHeight(RichInputText storageBinHeight) {
    this.storageBinHeight = storageBinHeight;
  }

  public RichInputText getStorageBinHeight() {
    return storageBinHeight;
  }

  public void setStorageBinUsableWeight(RichInputText storageBinUsableWeight) {
    this.storageBinUsableWeight = storageBinUsableWeight;
  }

  public RichInputText getStorageBinUsableWeight() {
    return storageBinUsableWeight;
  }

  public void setStorageBinUsableCube(RichInputText storageBinUsableCube) {
    this.storageBinUsableCube = storageBinUsableCube;
  }

  public RichInputText getStorageBinUsableCube() {
    return storageBinUsableCube;
  }

  public void setStorageBinSlotCount(RichInputText storageBinSlotCount) {
    this.storageBinSlotCount = storageBinSlotCount;
  }

  public RichInputText getStorageBinSlotCount() {
    return storageBinSlotCount;
  }

  public void setStorageBinDividerName(String storageBinDividerName) {
    this.storageBinDividerName = storageBinDividerName;
  }

  public String getStorageBinDividerName() {
    return storageBinDividerName;
  }

  public void setBinImageChoice(RichSelectOneChoice storageBinImage) {
    this.binImageChoice = storageBinImage;
  }

  public RichSelectOneChoice getBinImageChoice() {
    return binImageChoice;
  }

  public void setBinImage(RichImage storageBinImage) {
    this.binImage = storageBinImage;
  }

  public RichImage getBinImage() {
    return binImage;
  }

  public void setDivImageChoice(RichSelectOneChoice storageDivImage) {
    this.divImageChoice = storageDivImage;
  }

  public RichSelectOneChoice getDivImageChoice() {
    return divImageChoice;
  }

  public void setDivImage(RichImage storageDivImage) {
    this.divImage = storageDivImage;
  }

  public RichImage getDivImage() {
    return divImage;
  }

  public boolean isStorageBinFlag() {
    return storageBinFlag;
  }

  public boolean isStorageBinInUseFlag() {
    return storageBinInUseFlag;
  }

  public void setStorageBinRefresh1(boolean storageBinRefresh) {
    this.storageBinRefresh = storageBinRefresh;
  }

  @SuppressWarnings("unused") //called from .jspx
  public boolean isStorageBinRefresh() {
    return storageBinRefresh;
  }

  public void setDividerTable(RichTable dividerTable) {
    this.dividerTable = dividerTable;
  }

  public RichTable getDividerTable() {
    return dividerTable;
  }

  public void setDividerTypeSubTable(RichTable dividerTypeSubTable) {
    this.dividerTypeSubTable = dividerTypeSubTable;
  }

  public RichTable getDividerTypeSubTable() {
    return dividerTypeSubTable;
  }

  public void setDividerTypeName(RichInputText dividerTypeName) {
    this.dividerTypeName = dividerTypeName;
  }

  public RichInputText getDividerTypeName() {
    return dividerTypeName;
  }

  public void setDividerTypeSlotSelectIndex(RichInputText dividerTypeSlotSelectIndex) {
    this.dividerTypeSlotSelectIndex = dividerTypeSlotSelectIndex;
  }

  public RichInputText getDividerTypeSlotSelectIndex() {
    return dividerTypeSlotSelectIndex;
  }

  public void setDividerTypeSlotPosition(RichInputText dividerTypeSlotPosition) {
    this.dividerTypeSlotPosition = dividerTypeSlotPosition;
  }

  public RichInputText getDividerTypeSlotPosition() {
    return dividerTypeSlotPosition;
  }

  @SuppressWarnings("unused") //called from .jspx
  public void setDividerTypeSlotRow(RichInputText dividerTypeSlotRow) {
    this.dividerTypeSlotRow = dividerTypeSlotRow;
  }

  public RichInputText getDividerTypeSlotRow() {
    return dividerTypeSlotRow;
  }

  public void setDividerTypeSlotColumn(RichInputText dividerTypeSlotColumn) {
    this.dividerTypeSlotColumn = dividerTypeSlotColumn;
  }

  public RichInputText getDividerTypeSlotColumn() {
    return dividerTypeSlotColumn;
  }

  public void setDividerTypeSlotLength(RichInputText dividerTypeSlotLength) {
    this.dividerTypeSlotLength = dividerTypeSlotLength;
  }

  public RichInputText getDividerTypeSlotLength() {
    return dividerTypeSlotLength;
  }

  public void setDividerTypeSlotWidth(RichInputText dividerTypeSlotWidth) {
    this.dividerTypeSlotWidth = dividerTypeSlotWidth;
  }

  public RichInputText getDividerTypeSlotWidth() {
    return dividerTypeSlotWidth;
  }

  public boolean isDividerTypeFlag() {
    return dividerTypeFlag;
  }

  public void setDividerTypeRefresh1(boolean dividerTypeRefresh) {
    this.dividerTypeRefresh = dividerTypeRefresh;
  }

  public boolean isDividerTypeRefresh() {
    return dividerTypeRefresh;
  }

  public static void setDividerIterator(String dividerIterator) {
    StorageSetupBacking.dividerIterator = dividerIterator;
  }

  public static String getDividerIterator() {
    return dividerIterator;
  }

  public static void setDividerSlotIterator(String dividerSlotIterator) {
    StorageSetupBacking.dividerSlotIterator = dividerSlotIterator;
  }

  public static String getDividerSlotIterator() {
    return dividerSlotIterator;
  }

  public static void setStorageBinIterator(String storageBinIterator) {
    StorageSetupBacking.storageBinIterator = storageBinIterator;
  }

  public static String getStorageBinIterator() {
    return storageBinIterator;
  }
}
